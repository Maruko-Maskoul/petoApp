package com.example.android.peto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {


    private Button mShareNewPostBt;
    private EditText mPostTextEt;
    private Toolbar mToolbar;
    private ImageView mPostImage;
    private ProgressBar mNewPostPr;

    private Uri mCropResultUri = null;
    private String mUserId;
    private Bitmap mCompressedImageFile;
    private Uri mDownloadUri;

    private StorageReference mStorageRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mShareNewPostBt = (Button) findViewById(R.id.share_new_post);
        mPostTextEt = (EditText) findViewById(R.id.post_text);
        mToolbar = (Toolbar) findViewById(R.id.new_post_toolbar);
        mPostImage = (ImageView) findViewById(R.id.post_new_image);
        mNewPostPr = (ProgressBar) findViewById(R.id.new_post_br);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mUserId = mFirebaseAuth.getCurrentUser().getUid();

        // Save the post in database
        mShareNewPostBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String getPostText = mPostTextEt.getText().toString();

                if (mCropResultUri != null && !TextUtils.isEmpty(getPostText)) {
                    mNewPostPr.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    //compressed Image File
                    File newImageFile = new File(mCropResultUri.getPath());
                    try {
                        mCompressedImageFile = new Compressor(NewPostActivity.this)
                                .setMaxHeight(500)
                                .setMaxWidth(500)
                                .setQuality(50)
                                .compressToBitmap(newImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    mCompressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();
                    final StorageReference image_path = mStorageRef.child("image_post").child(randomName + ".jpg");


                    // Upload image post
                    final UploadTask uploadTask = image_path.putBytes(imageData);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                String errorMsg = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return image_path.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                mDownloadUri = task.getResult();


                            } else {
                                // Handle unsuccessful uploads
                                String errorMsg = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    // Make a Thumbnails
                    File newThumbFile = new File(mCropResultUri.getPath());

                    try {
                        mCompressedImageFile = new Compressor(NewPostActivity.this)
                                .setMaxHeight(100)
                                .setMaxWidth(100)
                                .setQuality(1)
                                .compressToBitmap(newThumbFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                    mCompressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos2);
                    byte[] thumbData = baos2.toByteArray();

                    final StorageReference thumb_image_path = mStorageRef.child("image_post/thumbs")
                            .child(randomName + ".jpg");

                    // Upload the thumb to database
                    final UploadTask uploadTask1 = thumb_image_path.
                            putBytes(thumbData);
                    Task<Uri> thumUrlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                String errorMsg = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                                throw task.getException();
                            }
                            return thumb_image_path.getDownloadUrl();

                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri mThumbDownloadUri = task.getResult();
                                saveFirebaseStrore(mDownloadUri, mThumbDownloadUri, getPostText);
                            }
                        }
                    });
                }


            }
        });

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check if the permission is GRANTED for android version >= marshmello
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(NewPostActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();

                        ActivityCompat.requestPermissions(NewPostActivity.this,
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
                mPostImage.setImageURI(mCropResultUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("Error with get image : ", error.getMessage());
            }
        }
    }

    private void corpImage() {
        // start picker to get image for cropping and then use the image in cropping activity
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(NewPostActivity.this);
    }

    private void saveFirebaseStrore(Uri imagePostUri, Uri thumbPostUri, String postDesc) {

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("user_id", mUserId);
        postMap.put("image_post_url", String.valueOf(imagePostUri));
        postMap.put("post_desc", postDesc);
        postMap.put("timestamp", FieldValue.serverTimestamp());
        postMap.put("image_thumb", String.valueOf(thumbPostUri));

        // add the name and the image to firebase store
        mFirebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(NewPostActivity.this, "The post uploaded successfully!", Toast.LENGTH_LONG).show();
                    sendToMainActivity();
                } else {
                    String errorMsg = task.getException().getMessage();
                    Toast.makeText(NewPostActivity.this, "(FireStore Error) : " + errorMsg, Toast.LENGTH_LONG).show();
                    sendToMainActivity();
                }
                mNewPostPr.setVisibility(View.INVISIBLE);

            }

        });
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}

