package com.example.android.peto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private CircleImageView mCircleImageView;
    private EditText mUserNameEt;
    private Button mSaveSettingsBt;
    private ProgressBar mPrograssBar;

    private Uri mCropResultUri = null;

    private StorageReference mStorageRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;

    private Uri mDownloadUri;
    private boolean isImageChange = false;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_setup);
        mCircleImageView = findViewById(R.id.profile_image);
        mUserNameEt = (EditText) findViewById(R.id.et_name);
        mSaveSettingsBt = (Button) findViewById(R.id.save_settings_bt);
        mPrograssBar = (ProgressBar) findViewById(R.id.setup_progress);

        //  initialize firebase instance
        mFirebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        mUserId = mFirebaseAuth.getCurrentUser().getUid();


        setSupportActionBar(mToolbar);


        // Retrieve the data from firebase
        mPrograssBar.setVisibility(View.VISIBLE);
        mSaveSettingsBt.setEnabled(false);
        mFirebaseFirestore.collection("Users").document(mUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mUserNameEt.setText(name);
                        mCropResultUri = Uri.parse(image);

                        RequestOptions placeHolderRequest = new RequestOptions();


                            placeHolderRequest.placeholder(R.drawable.default_image);

                            Glide.with(SetupActivity.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(mCircleImageView);

                    }
                } else {
                    String errorMsg = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "(FireStore Retrieve Error) : " + errorMsg, Toast.LENGTH_LONG).show();
                }
                mPrograssBar.setVisibility(View.INVISIBLE);
                mSaveSettingsBt.setEnabled(true);

            }
        });


        // save the data in firebase
        mSaveSettingsBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String userName = mUserNameEt.getText().toString();


                if (!TextUtils.isEmpty(userName)) {
                    mPrograssBar.setVisibility(View.VISIBLE);

                    if (mCropResultUri != null) {
                        if (isImageChange) {

                            final StorageReference image_path = mStorageRef.child("profile_images").child(mUserId + ".jpg");


                            UploadTask uploadTask = image_path.putFile(mCropResultUri);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }

                                    // Continue with the task to get the download URL
                                    return image_path.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();

                                        saveFirebaseStrore(downloadUri, userName);

                                    } else {
                                        // Handle unsuccessful uploads
                                        String errorMsg = task.getException().getMessage();
                                        Toast.makeText(SetupActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                        } else {
                            saveFirebaseStrore(null, userName);
                        }


                    } else
                        saveFirebaseStrore(mCropResultUri, userName);
                }
            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the permission is GRANTED for android version >= marshmello
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(SetupActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();

                        ActivityCompat.requestPermissions(SetupActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        // start picker to get image for cropping and then use the image in cropping activity
                        corpImage();
                    }
                } else {
                    corpImage();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mCropResultUri = result.getUri();
                //mCircleImageView.setImageURI(mCropResultUri);
                Glide.with(SetupActivity.this).load(mCropResultUri).into(mCircleImageView);
                isImageChange = true;


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("Error with get image : ", error.getMessage());
                isImageChange = false;
            }
        }
    }

    private void corpImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetupActivity.this);
    }

    private void saveFirebaseStrore(Uri uri, String userName) {
        Uri downloadUri;
        if (uri != null) {
            downloadUri = uri;
        } else {
            downloadUri = mCropResultUri;
        }
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", String.valueOf(downloadUri));


        // add the name and the image to firebase store
        mFirebaseFirestore.collection("Users").document(mUserId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(SetupActivity.this, "The user settings updated!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SetupActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "(FireStore Error) : " + errorMsg, Toast.LENGTH_LONG).show();

                }
                mPrograssBar.setVisibility(View.INVISIBLE);

            }
        });
    }
}
