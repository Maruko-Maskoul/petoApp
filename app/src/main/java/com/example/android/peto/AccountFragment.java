package com.example.android.peto;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mFirebaseAuth;
    private String mUserId;

    private ImageView profileImage;
    private TextView userNameTV;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        mUserId = mFirebaseAuth.getCurrentUser().getUid();

        profileImage = (ImageView) view.findViewById(R.id.profile);
        userNameTV = (TextView) view.findViewById(R.id.userNameProfile);

        mFirebaseFirestore.collection("Users").document(mUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    if (task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");


                        RequestOptions placeHolderRequest = new RequestOptions();
                        userNameTV.setText(name);

                        placeHolderRequest.placeholder(R.drawable.default_image);

                        Glide.with(AccountFragment.this).setDefaultRequestOptions(placeHolderRequest).load(image).into(profileImage);

                    }
                } else {
                    String errorMsg = task.getException().getMessage();
                    Toast.makeText(container.getContext(), "(FireStore Retrieve Error) : " + errorMsg, Toast.LENGTH_LONG).show();
                }

            }
        });

        return view;
    }

}
