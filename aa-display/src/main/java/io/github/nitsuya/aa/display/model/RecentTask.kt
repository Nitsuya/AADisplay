package io.github.nitsuya.aa.display.model

import android.os.Parcel
import android.os.Parcelable

data class RecentTask(
    val mainDisplay: List<RecentTaskInfo>,
    val virtualDisplay: List<RecentTaskInfo>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(RecentTaskInfo)!!,
        parcel.createTypedArrayList(RecentTaskInfo)!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(mainDisplay)
        parcel.writeTypedList(virtualDisplay)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecentTask> {
        override fun createFromParcel(parcel: Parcel): RecentTask {
            return RecentTask(parcel)
        }

        override fun newArray(size: Int): Array<RecentTask?> {
            return arrayOfNulls(size)
        }
    }
}
