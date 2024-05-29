package android.window;

import android.graphics.ColorSpace;
import android.hardware.HardwareBuffer;
import android.os.Parcel;
import android.os.Parcelable;

public class TaskSnapshot implements Parcelable {

    public HardwareBuffer getHardwareBuffer() {
        throw new RuntimeException("Stub!");
    }

    public ColorSpace getColorSpace() {
        throw new RuntimeException("Stub!");
    }


    protected TaskSnapshot(Parcel in) {}

    @Override
    public void writeToParcel(Parcel dest, int flags) {}

    @Override
    public int describeContents() {
        return 0;
    }


    public static final Creator<TaskSnapshot> CREATOR = new Creator<TaskSnapshot>() {
        @Override
        public TaskSnapshot createFromParcel(Parcel in) {
            return new TaskSnapshot(in);
        }

        @Override
        public TaskSnapshot[] newArray(int size) {
            return new TaskSnapshot[size];
        }
    };
}
