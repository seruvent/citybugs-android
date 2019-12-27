package citybugs.seruvent.org.tr.citybugs.util;

import android.os.Parcel;
import android.os.Parcelable;

public class SeruventToken implements Parcelable {


    private String jwtToken;


    protected SeruventToken(Parcel in) {
        this.setJwtToken(in.readString());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.getJwtToken());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SeruventToken> CREATOR = new Creator<SeruventToken>() {
        @Override
        public SeruventToken createFromParcel(Parcel in) {
            return new SeruventToken(in);
        }

        @Override
        public SeruventToken[] newArray(int size) {
            return new SeruventToken[size];
        }
    };

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
