package citybugs.seruvent.org.tr.citybugs.ui.home;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        view = inflater.inflate(R.layout.fragment_home, container, false);
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

        TextView eventCreate = view.findViewById(R.id.create_event_button);
        eventCreate.setOnClickListener(new View.OnClickListener() {
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

        return view;
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

                    //Uri file = Uri.fromFile(new File(data.getData().getPath()));
                    Uri uri = data.getData();
                    StorageReference fileRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
                    UploadTask uploadTask = fileRef.putFile(uri);

                    // Register observers to listen for when the download is done or if it fails
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            Log.e(Resource.TAG_LOG_INFO , "CITYBUGS file error");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            // ...
                            Log.i(Resource.TAG_LOG_INFO , "CITYBUGS file uploaded :: " + taskSnapshot.getMetadata().getPath());
                        }
                    });

                    break;
            }
    }
}