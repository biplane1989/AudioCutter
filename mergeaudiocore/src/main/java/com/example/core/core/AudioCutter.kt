package com.example.core.core

import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData

enum class Effect(val time: Int) {
    OFF(0), AFTER_1_S(1), AFTER_2_S(2), AFTER_3_S(3), AFTER_4_S(4), AFTER_5_S(5), AFTER_6_S(6)
}

enum class BitRate(val value: Int) {
    _32kb(32), _64kb(64), _128kb(128), _192kb(192), _256kb(256), _320kb(320)
}

enum class AudioFormat(val type: String) {
    MP3(".mp3"), AAC(".m4a")
}

enum class MixSelector(val type: String) {
    LONGEST("longest"), SHORTEST("shortest")
}

enum class FFMpegState {
    IDE, RUNNING, CANCEL, FAIL, SUCCESS
}

data class AudioCutConfig(
    var startPosition: Float,
    var endPosition: Float,
    var volumePercent: Int = 300,
    var fileName: String,
    var inEffect: Effect = Effect.OFF,
    var outEffect: Effect = Effect.OFF,
    var bitRate: BitRate = BitRate._128kb,
    var format: AudioFormat = AudioFormat.MP3,
    var pathFolder: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readFloat(), parcel.readFloat(), parcel.readInt(), parcel.readString()
            .toString(), Effect.valueOf(
            parcel.readString()
                .toString()
        ), Effect.valueOf(
            parcel.readString()
                .toString()
        ), BitRate.valueOf(
            parcel.readString()
                .toString()
        ), AudioFormat.valueOf(parcel.readString().toString()), parcel.readString()
            .toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(startPosition)
        parcel.writeFloat(endPosition)
        parcel.writeInt(volumePercent)
        parcel.writeString(fileName)
        parcel.writeString(inEffect.name)
        parcel.writeString(outEffect.name)
        parcel.writeString(bitRate.name)
        parcel.writeString(format.name)
        parcel.writeString(pathFolder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioCutConfig> {
        override fun createFromParcel(parcel: Parcel): AudioCutConfig {
            return AudioCutConfig(parcel)
        }

        override fun newArray(size: Int): Array<AudioCutConfig?> {
            return arrayOfNulls(size)
        }
    }
}

data class AudioMixConfig(
    val fileName: String,
    val pathFolder: String,
    val selector: MixSelector = MixSelector.LONGEST,
    val volumePercent1: Int = 100,
    val volumePercent2: Int = 100,
    val format: AudioFormat = AudioFormat.MP3,
    var bitRate: BitRate = BitRate._128kb

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(), parcel.readString()
            .toString(), MixSelector.valueOf(
            parcel.readString()
                .toString()
        ), parcel.readInt(), parcel.readInt(), AudioFormat.valueOf(
            parcel.readString()
                .toString()
        ), BitRate.valueOf(parcel.readString().toString())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(fileName)
        parcel.writeString(selector.name)
        parcel.writeInt(volumePercent1)
        parcel.writeInt(volumePercent2)
        parcel.writeString(format.name)
        parcel.writeString(bitRate.name)
        parcel.writeString(pathFolder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioMixConfig> {
        override fun createFromParcel(parcel: Parcel): AudioMixConfig {
            return AudioMixConfig(parcel)
        }

        override fun newArray(size: Int): Array<AudioMixConfig?> {
            return arrayOfNulls(size)
        }
    }
}

data class AudioMergingConfig(
    val audioFormat: AudioFormat,
    var fileName: String,
    var pathFolder: String,
    var bitRate: BitRate = BitRate._128kb
) : Parcelable {
    constructor(parcel: Parcel) : this(
        AudioFormat.valueOf(
            parcel.readString()
                .toString()
        ), parcel.readString().toString(), parcel.readString()
            .toString(), BitRate.valueOf(parcel.readString().toString())
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(audioFormat.name)
        parcel.writeString(fileName)
        parcel.writeString(pathFolder)
        parcel.writeString(bitRate.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioMergingConfig> {
        override fun createFromParcel(parcel: Parcel): AudioMergingConfig {
            return AudioMergingConfig(parcel)
        }

        override fun newArray(size: Int): Array<AudioMergingConfig?> {
            return arrayOfNulls(size)
        }
    }

}


data class OutputAudioInfo(val audioFile: AudioCore, var percent: Int)
data class AudioMergingInfo(var audioFile: AudioCore?, var percent: Int, var state: FFMpegState)
interface AudioCutter {
    suspend fun cut(audioFile: AudioCore, audioCutConfig: AudioCutConfig): AudioCore
    suspend fun merge(
        listAudioFile: List<AudioCore>,
        fileName: String,
        audioFormat: AudioFormat,
        pathFolder: String
    ): AudioCore

    suspend fun mix(
        audioFile1: AudioCore,
        audioFile2: AudioCore,
        audioMixConfig: AudioMixConfig
    ): AudioCore

    suspend fun cancelTask(): Boolean
    fun getAudioMergingInfo(): LiveData<AudioMergingInfo>
}