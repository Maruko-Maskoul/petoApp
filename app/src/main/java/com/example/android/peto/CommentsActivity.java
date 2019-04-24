package com.example.android.peto;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentsActivity extends AppCompatActivity {

    private Toolbar mCommentsToolbar;

    private EditText mInputComment;
    private ImageView mSendComment;
    private RecyclerView mCommentsRecyclerView;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;

    private CommentsRecyclerAdapter mCommentsRecyclerAdapter;
    private String currentUserId;

    private String postId;
    private List<CommentsPost> mCommentList;

    public static final String TAG = CommentsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mCommentsToolbar = (Toolbar) findViewById(R.id.comment_toolbar);
        setSupportActionBar(mCommentsToolbar);
        mCommentsToolbar.setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        postId = getIntent().getStringExtra("postId");

        mCommentList = new ArrayList<>();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = mFirebaseAuth.getCurrentUser().getUid();

        mInputComment = (EditText) findViewById(R.id.comment_input);
        mSendComment = (ImageView) findViewById(R.id.send_comment);

        mCommentsRecyclerView = (RecyclerView) findViewById(R.id.comment_list_item);

        mCommentsRecyclerAdapter = new CommentsRecyclerAdapter(CommentsActivity.this, mCommentList);
        mCommentsRecyclerView.setHasFixedSize(true);
        mCommentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentsRecyclerView.setAdapter(mCommentsRecyclerAdapter);

        mFirebaseFirestore.collection("Posts/" + postId + "/Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d("HomeFragment : ", "Error:" + e.getMessage());
                        } else {
                            // Get the last visible document
                            assert queryDocumentSnapshots != null;
                            if (!queryDocumentSnapshots.getDocumentChanges().isEmpty()) {
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    if (doc.getType() == DocumentChange.Type.ADDED) {


                                        CommentsPost petoPost = doc.getDocument().toObject(CommentsPost.class);
                                        mCommentList.add(petoPost);
                                        mCommentsRecyclerAdapter.notifyDataSetChanged();
                                    }


                                }


                            }
                        }
                    }
                });

        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = mInputComment.getText().toString().trim();
                if (!TextUtils.isEmpty(comment)) {

                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message", comment);
                    commentMap.put("user_id", currentUserId);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());
                    mFirebaseFirestore.collection("Posts/" + postId + "/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if (task.isSuccessful()) {
                                mInputComment.setText("");

                            } else {
                                String errorMsg = task.getException().getMessage();
                                Toast.makeText(CommentsActivity.this, "Error posting comment : " + errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }


}
