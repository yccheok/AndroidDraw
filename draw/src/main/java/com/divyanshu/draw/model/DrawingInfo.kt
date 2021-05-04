package com.divyanshu.draw.model

import android.os.Parcel
import android.os.Parcelable

data class DrawingInfo(val directoryCode: Int, val name: String, val path: String, val size: Long, val width: Int, val height: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readLong(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(directoryCode)
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeLong(size)
        parcel.writeInt(width)
        parcel.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DrawingInfo> {
        override fun createFromParcel(parcel: Parcel): DrawingInfo {
            return DrawingInfo(parcel)
        }

        override fun newArray(size: Int): Array<DrawingInfo?> {
            return arrayOfNulls(size)
        }
    }
}