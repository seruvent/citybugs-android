package citybugs.seruvent.org.tr.citybugs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import citybugs.seruvent.org.tr.citybugs.util.Resource;
import citybugs.seruvent.org.tr.citybugs.util.SeruventToken;


/**
 * @author kemalsamikaraca
 * @version 0.0.1
 * @desc A login screen that offers login via facebook
 */
public class LoginActivity extends AppCompatActivity{

    private View mProgressView;
    private View mLoginFormView;
    private CallbackManager callbackManager;
    private Context context=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // -1- Initialization
        setContentView(R.layout.activity_login);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        context = this.getApplicationContext();
        callbackManager = CallbackManager.Factory.create();

        // -2- Event Listeners
        // https://developers.facebook.com/docs/facebook-login/permissions#reference
        LoginButton loginFacebookButton = (LoginButton) findViewById(R.id.login_facebook_button);
        loginFacebookButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i( Resource.TAG_LOG_INFO , "FacebookCallback.onSuccess :: " + loginResult.toString());
                requestAuthentication(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                Log.w( Resource.TAG_LOG_WARNING, "CANCELLED");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e( Resource.TAG_LOG_ERROR , "ERROR :: " + error.getLocalizedMessage());
                Log.e( Resource.TAG_LOG_ERROR , "ERROR :: " + error.getMessage());
            }
        });

    }

    @Override
    public void onResume () {
        super.onResume();

        showProgress(true);
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean hasFacebookAccessToken = accessToken != null && !accessToken.isExpired();
        if(hasFacebookAccessToken){
            requestAuthentication(accessToken.getToken());
        }else{
            showProgress(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        Log.i(Resource.TAG_LOG_INFO , "LoginActivity.onSaveInstanceState :: called");
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


/*
 ***************************************************************************************************
 ***************************************************************************************************
 ****                           ACTIVITY METHODS
 ***************************************************************************************************
 ***************************************************************************************************
 */

    private void requestAuthentication(String accessToken){
        try{
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("appId", "-");
            jsonBody.put("appUserId", "-");
            jsonBody.put("accessToken", accessToken);
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Resource.DOMAIN_API_AUTHENTICATION,  jsonBody ,  new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i(Resource.TAG_LOG_INFO, Resource.DOMAIN_API_AUTHENTICATION + " :: onResponse :: ["+response.toString()+"]");
                    try{
                        context.getSharedPreferences(Resource.SHARED_PREF_NAME , Context.MODE_PRIVATE).edit().putString("TOKEN" , response.get("token").toString()).apply();
                        Log.i(Resource.TAG_LOG_INFO , "[URL:"+Resource.DOMAIN_API_AUTHENTICATION+"][Response"+ response.toString() +"]");

                        // Redirect to MainActivity
                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                        myIntent.putExtra("seruvent-key", "seruvent-value"); //Optional parameters
                        myIntent.putExtra("TOKEN", response.get("token").toString()); //Optional parameters
                        startActivity(myIntent);

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
            Volley.newRequestQueue(context).add(jsonRequest);
        }catch (Exception error){
            Log.e(Resource.TAG_LOG_ERROR, error.getMessage());
        }
    }


    // FACEBOOK
    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            String oldAccessTokenStr = oldAccessToken!=null?oldAccessToken.getToken():"NULL";
            String currentAccessTokenStr = currentAccessToken!=null?currentAccessToken.getToken():"NULL";
            Log.w(Resource.TAG_LOG_WARNING , "onCurrentAccessTokenChanged called :: [oldAccessToken="+oldAccessTokenStr+"][currentAccessToken="+currentAccessTokenStr+"]");
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


}

