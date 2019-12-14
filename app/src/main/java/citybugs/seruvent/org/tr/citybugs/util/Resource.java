package citybugs.seruvent.org.tr.citybugs.util;

public class Resource {

    //public static String DOMAIN_NAME = "http://161.9.195.122:8080";
    public static String DOMAIN_NAME = "http://192.168.1.2:8080";
    public static String DOMAIN_API = DOMAIN_NAME + "/api";
    public static String DOMAIN_API_AUTHENTICATION = DOMAIN_API + "/authenticate";
    public static String DOMAIN_API_EVENT_CREATE = DOMAIN_API + "/event";
    public static String DOMAIN_API_EVENT_LIST = DOMAIN_API + "/events";

    public static String TAG_LOG_ERROR = "SERUVENT_ERROR";
    public static String TAG_LOG_INFO = "SERUVENT_INFO";

    public static String SHARED_PREF_NAME = "SERUVENT_CITYBUGS";

    public static String STATIC_ANDROID_API_TOKEN = "izcfpXu74i6YgIxSG712AEaohc0FfYpC";
    public static String VALUE_API_TOKEN = "";

    public static String FIREBASE_DOMAIN_NAME = "https://firebasestorage.googleapis.com/";

    public static void setDefaultAPITOKEN(){
        VALUE_API_TOKEN = STATIC_ANDROID_API_TOKEN;
    }

}
