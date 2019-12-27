package citybugs.seruvent.org.tr.citybugs.ui.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import citybugs.seruvent.org.tr.citybugs.R;
import citybugs.seruvent.org.tr.citybugs.util.Resource;

public class HomeFragment extends Fragment {

    private StorageReference mStorageRef;
    private HomeViewModel homeViewModel;
    private ImageView imageView;
    private final int GALLERY_REQUEST_CODE = 106;
    private View view;
    private Button bugApplicationButton;
    private EditText bugApplicationDecsription;
    private Uri imageUri;
    private String imageFirebasePath;
    private View mProgressView;
    private View mFormView;
    private FusedLocationProviderClient fusedLocationClient;
    private String locationStr;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        view = inflater.inflate(R.layout.fragment_home, container, false);

        // -1- Initialization
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
        mFormView = view.findViewById(R.id.event_application_main);
        mProgressView = view.findViewById(R.id.event_application_progressbar);
        final TextView textView = view.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        imageView = view.findViewById(R.id.event_image_1);
        mStorageRef = FirebaseStorage.getInstance().getReference("citybugs");

        final Activity activity = this.getActivity();
        final Fragment fragment = this;

        ImageView imageView = view.findViewById(R.id.event_image_1);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                // Sets the type as image/*. This ensures only components of type image are selected
                intent.setType("image/*");
                //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
                String[] mimeTypes = {"image/jpeg", "image/png"};
                intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
                startActivityForResult(intent,GALLERY_REQUEST_CODE);
            }
        });


        bugApplicationDecsription = view.findViewById(R.id.event_application_desc);
        bugApplicationButton = view.findViewById(R.id.create_event_button);
        bugApplicationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("UYARI ");
                builder.setMessage("Başvuruyu yapmak istediğinize emin misiniz?");
                builder.setNegativeButton("Hayır", null);
                builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        uploadImagesFirebaseAndBugApplication(imageUri);
                    }
                });
                builder.show();

            }
        });

        return view;
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

            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // https://firebase.google.com/docs/storage/android/create-reference
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    Glide.with(view).load(data.getData()).into(imageView);
                    bugApplicationButton.setVisibility(View.VISIBLE);
                    bugApplicationDecsription.setVisibility(View.VISIBLE);
                    imageUri = data.getData();
                    break;
            }
    }

    public void uploadImagesFirebaseAndBugApplication(final Uri uri){

        showProgress(true);

        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    // -1- Got last known location. In some rare situations this can be null.
                    if (location != null) {

                        // -1.1- GET LOCATION - Logic to handle location object
                        locationStr = location.getLatitude() + ":" + location.getLongitude();
                        Log.i(Resource.TAG_LOG_INFO , "Latitude::" + location.getLatitude() + " - Longitude::" + location.getLongitude());

                        // -1.2- START TO UPLOAD IMAGE
                        StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
                        UploadTask uploadTask = fileRef.putFile(uri);
                        imageFirebasePath = null;

                        // -1.3- Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Log.e(Resource.TAG_LOG_INFO , "CITYBUGS file error");
                                showProgress(false);

                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                Log.i(Resource.TAG_LOG_INFO , "CITYBUGS file uploaded :: " + taskSnapshot.getMetadata().getPath());
                                imageFirebasePath = taskSnapshot.getMetadata().getPath();
                                requestEventApplication(imageFirebasePath);

                            }
                        });

                    // -2- Location null
                    }else{
                        showProgress(false);
                        Log.e(Resource.TAG_LOG_ERROR , "LOCATION NULL");
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showProgress(false);
                    Log.e(Resource.TAG_LOG_ERROR , "LOCATION ERROR");
                }
            });

    }


    private void requestEventApplication(String imgUrl){
        try{
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", bugApplicationDecsription.getText());
            jsonBody.put("description", "MOBILE TEST - description");
            jsonBody.put("gpsLocation", locationStr);

            JSONObject eventResource = new JSONObject();
            eventResource.put("url", imgUrl);

            JSONArray eventResources = new JSONArray();
            eventResources.put(eventResource);

            jsonBody.put("eventResources", eventResources);

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, Resource.DOMAIN_API_EVENT_CREATE,  jsonBody ,  new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        Log.i(Resource.TAG_LOG_INFO , response.toString());
                    } catch (Exception e){
                        Log.e(Resource.TAG_LOG_ERROR, Resource.DOMAIN_API_EVENT_CREATE + " - JSON exception - " + e.getMessage());
                    } finally {
                        showProgress(false);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(Resource.TAG_LOG_ERROR, error.toString());
                    showProgress(false);
                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    SharedPreferences sharedPref = getActivity().getSharedPreferences(Resource.SHARED_PREF_NAME , Context.MODE_PRIVATE);
                    String authorizationValue = "Bearer " + (sharedPref.getString("TOKEN", ""));
                    Log.i(Resource.TAG_LOG_INFO , "----- ----- HEADER ----- -----");
                    Log.i(Resource.TAG_LOG_INFO , "Authorization " + authorizationValue);
                    params.put("Authorization", authorizationValue);
                    return params;
                }
            } ;
            Volley.newRequestQueue(getContext()).add(jsonRequest);
        }catch (Exception error){
            Log.e(Resource.TAG_LOG_ERROR, error.getMessage());
            showProgress(false);
        }
    }

}