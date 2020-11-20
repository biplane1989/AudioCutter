package com.example.audiocutter.activities.acttest.testnm

import android.app.Activity
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import com.example.audiocutter.R
import kotlin.experimental.and


class TestAct : Activity() {

//    private val duration = 3 // seconds
//    private val sampleRate = 8000
//    private val numSamples = duration * sampleRate
//    private val sample = DoubleArray(numSamples)
//    private val freqOfTone = 440.0 // hz
//    private val generatedSnd = ByteArray(2 * numSamples)
//    var handler: Handler = Handler()
//
//
//    public override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.act_test)
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        val thread = Thread {
//            genTone()
//            handler.post(Runnable { playSound() })
//        }
//        thread.start()
//    }
//
//    fun genTone() {
//        // fill out the array
//        for (i in 0 until numSamples) {
//            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone))
//        }
//
//        // convert to 16 bit pcm sound array
//        // assumes the sample buffer is normalised.
//        var idx = 0
//        for (dVal in sample) {
//            // scale to maximum amplitude
//            val `val` = (dVal * 32767).toShort()
//            // in 16 bit wav PCM, first byte is the low order byte
//            generatedSnd[idx++] = (`val` and 0x00ff).toByte()
//            generatedSnd[idx++] = (`val` and 0xff00.toShort() >>> 8).toByte()
//        }
//    }
//
//    fun playSound() {
//        val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, numSamples, AudioTrack.MODE_STATIC)
//        audioTrack.write(generatedSnd, 0, generatedSnd.size)
//        audioTrack.play()
//    }
}

