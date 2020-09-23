package com.example.audiocutter.objects

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import java.io.File
import java.io.Serializable

class AudioFile(
    val file: File,
    val fileName: String,
    val size: Long,
    val bitRate: Int = 128,
    val time: Long = 0,
    var uri: Uri? = null,
    val bitmap: Bitmap? = null,
    val title: String? = "",
    val alBum: String? = "",
    val artist: String? = "",
    val dateAdded: String? = "",
    val genre: String? = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        File(parcel.readString()!!),
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readParcelable(Bitmap::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun equals(other: Any?): Boolean {
        if (other is AudioFile) {
            return file.absolutePath == other.file.absolutePath
        }
        return super.equals(other)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(file.absolutePath)
        parcel.writeString(fileName)
        parcel.writeLong(size)
        parcel.writeInt(bitRate)
        parcel.writeLong(time)
        parcel.writeParcelable(uri, flags)
        parcel.writeParcelable(bitmap, flags)
        parcel.writeString(title)
        parcel.writeString(alBum)
        parcel.writeString(artist)
        parcel.writeString(dateAdded)
        parcel.writeString(genre)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioFile> {
        override fun createFromParcel(parcel: Parcel): AudioFile {
            return AudioFile(parcel)
        }

        override fun newArray(size: Int): Array<AudioFile?> {
            return arrayOfNulls(size)
        }
    }

}

