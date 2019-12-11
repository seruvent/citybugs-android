package citybugs.seruvent.org.tr.citybugs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import citybugs.seruvent.org.tr.citybugs.util.Resource;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };


    // UI references.

    private View mProgressView;
    private View mLoginFormView;

    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private Context context=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        SharedPreferences sharedPref = this.getSharedPreferences(Resource.SHARED_PREF_NAME , Context.MODE_PRIVATE);
        final SharedPreferences.Editor sharedEditor = sharedPref.edit();

        context = this.getApplicationContext();
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginFacebookButton = (LoginButton) findViewById(R.id.login_facebook_button);

        // https://developers.facebook.com/docs/facebook-login/permissions#reference
        //loginFacebookButton.setReadPermissions(Arrays.asList(EMAIL));
        loginFacebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Log.i(Resource.TAG_LOG_INFO , "TOKEN :: " + loginResult.getAccessToken().getToken());
                Log.i(Resource.TAG_LOG_INFO , "APPLICATION ID :: " + loginResult.getAccessToken().getApplicationId());
                Log.i(Resource.TAG_LOG_INFO , "USER ID :: " + loginResult.getAccessToken().getUserId());
                Log.i(Resource.TAG_LOG_INFO , "SOURCE NAME :: " + loginResult.getAccessToken().getSource().name());

                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("appId", "-");
                    jsonBody.put("appUserId", "-");
                    jsonBody.put("accessToken", loginResult.getAccessToken().getToken());

                    JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, Resource.DOMAIN_API_AUTHENTICATION,  jsonBody ,  new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(Resource.TAG_LOG_INFO, response.toString());


                            try{

                                Log.i(Resource.TAG_LOG_INFO, response.get("token").toString());
                                sharedEditor.putString("TOKEN" , response.get("token").toString());
                                sharedEditor.commit();

                                // Redirect to MainActivity
                                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                                myIntent.putExtra("seruvent-key", "seruvent-value"); //Optional parameters
                                LoginActivity.this.startActivity(myIntent);

                            } catch (Exception e){
                                Log.e(Resource.TAG_LOG_ERROR, Resource.DOMAIN_API_AUTHENTICATION + " - JSON exception - " + e.getMessage());
                            }


                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(Resource.TAG_LOG_ERROR, error.toString());
                        }
                    }) ;

                    Volley.newRequestQueue(context).add(stringRequest);

                } catch (Exception e){
                    Log.e( Resource.TAG_LOG_ERROR , e.getMessage());
                }
            }

            @Override
            public void onCancel() {
                Log.i( Resource.TAG_LOG_INFO , "CANCELLED");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e( Resource.TAG_LOG_ERROR , "ERROR :: " + error.getLocalizedMessage());
                Log.e( Resource.TAG_LOG_ERROR , "ERROR :: " + error.getMessage());
            }
        });

    }


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    // FACEBOOK
    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {

        }
    };

    private void loadUserProfile(AccessToken accessToken){

        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                try{
                    String firstName = object.getString("first_name");
                    String lastName = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    String imgUrl = "https://graph.facebook.com/" + id + "/picture?type=normal";

                    Log.i("CITYBUGS" , "name::" + firstName);
                    Log.i("CITYBUGS" , "lastname::" + lastName);
                    Log.i("CITYBUGS" , "email::" + email);
                    Log.i("CITYBUGS" , "id::" + id);
                    Log.i("CITYBUGS" , "image::" + imgUrl);

                }catch (Exception e){
                    Log.e("CITYBUGS", e.getMessage());
                }


            }
        });

        Bundle bundleParameters = new Bundle();
        bundleParameters.putString("field" , "first_name,last_name,email,id");
        graphRequest.setParameters(bundleParameters);
        graphRequest.executeAsync();
    }

    /** Called when the user taps the Send button */
    public void startMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

}

