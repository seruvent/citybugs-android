package citybugs.seruvent.org.tr.citybugs.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SeruventToken {


    /**
     * Getter for the access token that is current for the application.
     *
     * @return The access token that is current for the application.
     */
    public static SharedPreferences getCurrentAccessToken(Context context) {
        return context.getSharedPreferences(Resource.SHARED_PREF_NAME , Context.MODE_PRIVATE);
    }

}
