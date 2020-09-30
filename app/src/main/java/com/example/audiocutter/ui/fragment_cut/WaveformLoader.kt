package com.example.audiocutter.ui.fragment_cut

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer

class WaveformLoader {
    private var mWaveformLoaderAsyncTask: WaveformLoaderAsyncTask? = null
    fun extractor(filePath: String?, callBack: OnWaveformLoaderCallBack?) {
        if (mWaveformLoaderAsyncTask != null) mWaveformLoaderAsyncTask!!.cancel(true)
        mWaveformLoaderAsyncTask = WaveformLoaderAsyncTask(filePath, callBack)
        mWaveformLoaderAsyncTask!!.execute()
    }

    fun cancel() {
        if (mWaveformLoaderAsyncTask != null) mWaveformLoaderAsyncTask!!.cancel(true)
    }

    private class WaveformLoaderAsyncTask(
        val filePath: String?,
        private val callBack: OnWaveformLoaderCallBack?
    ) : AsyncTask<Void?, Progress?, Void?>() {
        private var extractor: MediaExtractor? = null
        private var decoder: MediaCodec? = null
        var chanel = 0
        var sampleRate = 0
        var durationUs: Long = 0
        var previousSize = 0
        lateinit var previousByte: ByteArray
        override fun doInBackground(vararg params: Void?): Void? {
            createMediaCodec()
            if (decoder != null) readWaveform()
            return null
        }

        private fun createMediaCodec() {
            try {
                extractor = MediaExtractor()
                extractor!!.setDataSource(filePath!!)
                val numTracks = extractor!!.trackCount
                for (i in 0 until numTracks) {
                    val format = extractor!!.getTrackFormat(i)
                    val mime = format.getString(MediaFormat.KEY_MIME)
                    if (mime != null && mime.startsWith("audio/")) {
                        extractor!!.selectTrack(i)
                        Log.d(TAG, "Select Audio Track $mime")
                        decoder = MediaCodec.createDecoderByType(mime)
                        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 274576)
                        durationUs = format.getLong(MediaFormat.KEY_DURATION)
                        decoder!!.configure(format, null, null, 0)
                        break
                    }
                }
                if (decoder == null) {
                    Log.e(TAG, "Can't find audio info!")
                    return
                }
                decoder!!.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun readWaveform() {
            val inputBuffers = decoder!!.inputBuffers
            var outputBuffers = decoder!!.outputBuffers
            val info = MediaCodec.BufferInfo()
            chanel = decoder!!.outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            sampleRate = decoder!!.outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            previousSize = 0
            previousByte = ByteArray((sampleRate * (PERIOD_IN_FRAMES / 1000f)).toInt() * chanel * 2)
            publishProgress(null)
            var isEOS = false
            while (!isCancelled) {
                if (!isEOS) {
                    val inIndex = decoder!!.dequeueInputBuffer(10000)
                    if (inIndex >= 0) {
                        val buffer = inputBuffers[inIndex]
                        val sampleSize = extractor!!.readSampleData(buffer, 0)
                        if (sampleSize < 0) {
                            decoder!!.queueInputBuffer(
                                inIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isEOS = true
                        } else {
                            decoder!!.queueInputBuffer(
                                inIndex,
                                0,
                                sampleSize,
                                extractor!!.sampleTime,
                                0
                            )
                            extractor!!.advance()
                        }
                    }
                }
                val outIndex = decoder!!.dequeueOutputBuffer(info, 10000)
                when (outIndex) {
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> outputBuffers =
                        decoder!!.outputBuffers
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        chanel = decoder!!.outputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                        sampleRate = decoder!!.outputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        previousByte =
                            ByteArray((sampleRate * (PERIOD_IN_FRAMES / 1000f)).toInt() * chanel * 2)
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    }
                    else -> {
                        val buffer = outputBuffers[outIndex]
                        if (info.size > 0) {
                            publishProgress(Progress(getDB(buffer, info.size)))
                        }
                        decoder!!.releaseOutputBuffer(outIndex, true)
                    }
                }
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    break
                }
            }
        }

        private fun getDB(buffer: ByteBuffer, size: Int): DoubleArray? { //50ms for one sample
            val periodSize = (sampleRate * (PERIOD_IN_FRAMES / 1000f)).toInt() * chanel * 2
            val totalSize = previousSize + size
            return if (periodSize < totalSize) {
                val numSample = totalSize / periodSize
                val calSize = numSample * periodSize
                val calByte = ByteArray(calSize)
                val db = DoubleArray(numSample * chanel)
                for (index in 0 until totalSize) {
                    if (index < previousSize) {
                        calByte[index] = previousByte[index]
                    } else if (index < calSize) {
                        calByte[index] = buffer[index - previousSize]
                    } else {
                        previousByte[index - calSize] = buffer[index - previousSize]
                    }
                }
                previousSize = totalSize - calSize
                for (index in 0 until numSample) {
                    if (chanel == 2) {
                        val tmp = getStereoDB(calByte, index * periodSize, (index + 1) * periodSize)
                        db[index] =
                            if (java.lang.Double.isInfinite(tmp[0])) DB_MIN.toDouble() else tmp[0]
                        db[index + 1] =
                            if (java.lang.Double.isInfinite(tmp[1])) DB_MIN.toDouble() else tmp[1]
                    } else {
                        val tmp = getMonoDB(calByte, index * periodSize, (index + 1) * periodSize)
                        db[index] = if (java.lang.Double.isInfinite(tmp)) DB_MIN.toDouble() else tmp
                    }
                }
                db
            } else {
                for (index in 0 until size) {
                    previousByte[index + previousSize] = buffer[index]
                }
                previousSize += size
                null
            }
        }

        override fun onProgressUpdate(vararg values: Progress?) {
            callBack?.onWaveformUpdate(values[0]?.values, chanel)
        }

        override fun onPostExecute(aVoid: Void?) {
            callBack?.onWaveformFinish()
        }
    }

    interface OnWaveformLoaderCallBack {
        fun onWaveformUpdate(db: DoubleArray?, chanelCount: Int)
        fun onWaveformFinish()
    }

    class Progress(var values: DoubleArray?)
    companion object {
        private val TAG = WaveformLoader::class.java.name
        const val PERIOD_IN_FRAMES = 50 //ms
        const val DB_MIN = -90f
        private var sInstance: WaveformLoader? = null

        @JvmStatic
        fun get(): WaveformLoader? {
            if (sInstance == null) sInstance = WaveformLoader()
            return sInstance
        }

        private fun getLeftRightAmplitude(
            buffer: ByteArray,
            start: Int,
            end: Int
        ): DoubleArray { //LLRRLLRRLLRR
            var leftSum = 0.0
            var rightSum = 0.0
            var i = start
            while (i < end) {
                val index = 4 * i
                if (index + 3 < end) {
                    val left: Int = (buffer[index] + (buffer[index + 1]) shl 8)
                    leftSum += left * left.toDouble()
                    val right: Int = (buffer[index + 2] + (buffer[index + 3]) shl 8)
                    rightSum += right * right.toDouble()
                }
                i += 4
            }
            return doubleArrayOf(
                Math.sqrt(leftSum / ((end - start) / 4f)),
                Math.sqrt(rightSum / ((end - start) / 4f))
            )
        }

        fun getStereoDB(buffer: ByteArray, start: Int, end: Int): DoubleArray {
            val stereoDB = getLeftRightAmplitude(buffer, start, end)
            val leftDB = getDB(stereoDB[0])
            val rightDB = getDB(stereoDB[1])
            return doubleArrayOf(leftDB, rightDB)
        }

        private fun getMonoAmplitude(
            buffer: ByteArray,
            start: Int,
            end: Int
        ): Double { // MM,MM,MM,MM,MM
            var sum = 0.0
            var i = start
            while (i < end) {
                val index = 2 * i
                if (index + 1 < end) {
                    val mono: Int = (buffer[index] + (buffer[index + 1]) shl 8)
                    sum += mono * mono.toDouble()
                }
                i += 2
            }
            return Math.sqrt(sum / ((end - start) / 2f))
        }

        fun getMonoDB(buffer: ByteArray, start: Int, end: Int): Double {
            return getDB(getMonoAmplitude(buffer, start, end))
        }

        fun getDB(amplitude: Double): Double {
            return 20.0 * Math.log10(amplitude / Short.MAX_VALUE)
        }
    }
}