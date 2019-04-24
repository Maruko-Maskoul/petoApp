package com.example.android.peto;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class PetoRecyclerAdapter extends RecyclerView.Adapter<PetoRecyclerAdapter.ViewHolder> {

    private List<PetoPost> postList;
    private Context mContext;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mFirebaseAuth;

    public PetoRecyclerAdapter(Context context, List<PetoPost> list) {
        this.mContext = context;
        this.postList = list;
    }

    @NonNull
    @Override
    public PetoRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.posts_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PetoRecyclerAdapter.ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);
        final String petoPostId = postList.get(position).PetoPostId;
        final String currentUserID = mFirebaseAuth.getCurrentUser().getUid();

        String descText = postList.get(position).getPost_desc();
        holder.setDescText(descText);

        String imagePost = postList.get(position).getImage_post_url();
        String thumbImage = postList.get(position).getImage_thumb();
        holder.setPostImage(imagePost, thumbImage);

        long millsec = postList.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MM/dd/yyyy", new Date(millsec)).toString();
        holder.setPostDate(dateString);

        final String userId = postList.get(position).getUser_id();
        mFirebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    String imageProfile = task.getResult().get("image").toString();
                    String userName = task.getResult().get("name").toString();

                    holder.setUserData(userName, imageProfile);

                } else {
                    // Handle the error
                }
            }
        });

        // Get likes number on the post
        mFirebaseFirestore.collection("Posts/" + petoPostId + "/Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d("HomeFragmentRecycler : ", "Error:" + e.getMessage());
                        } else {

                            if (!queryDocumentSnapshots.isEmpty()) {
                                int count = queryDocumentSnapshots.size();
                                holder.setLikesCount(count);
                            } else {
                                holder.setLikesCount(0);
                            }
                        }
                    }
                });


        // Change the like image if the user likes the post or not
        mFirebaseFirestore.collection("Posts/" + petoPostId + "/Likes")
                .document(currentUserID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.d("HomeFragmentRecycler : ", "Error:" + e.getMessage());
                } else {
                    if (documentSnapshot.exists()) {
                        holder.likePostImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_like));
                    } else {
                        holder.likePostImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_not_like));
                    }
                }
            }
        });

        // Likes feature
        holder.likePostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFirebaseFirestore.collection("Posts/" + petoPostId + "/Likes").document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        // current user add like
                        if (!task.getResult().exists()) {
                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());
                            mFirebaseFirestore.collection("Posts/" + petoPostId + "/Likes").document(currentUserID).set(likesMap);

                            // current user Delete the Like form the post
                        } else {
                            mFirebaseFirestore.collection("Posts/" + petoPostId + "/Likes").
                                    document(currentUserID).delete();
                        }
                    }
                });

            }
        });

        // Comments feature
        holder.commentsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postId", petoPostId);
                mContext.startActivity(intent);
            }
        });
        // Get Comments number on the post
        mFirebaseFirestore.collection("Posts/" + petoPostId + "/Comments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.d("HomeFragmentRecycler : ", "Error:" + e.getMessage());
                        } else {

                            if (!queryDocumentSnapshots.isEmpty()) {
                                int count = queryDocumentSnapshots.size();
                                holder.setCommentCounter(count);
                            } else {
                                holder.setCommentCounter(0);
                            }
                        }
                    }
                });

        // Delete Post
        final String post_user = postList.get(position).getUser_id();
        if (post_user.equals(currentUserID)) {
            holder.deletPostBt.setEnabled(true);
            holder.deletPostBt.setVisibility(View.VISIBLE);
            holder.deletPostBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFirebaseFirestore.collection("Posts").document(petoPostId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                postList.remove(position);
                                PetoRecyclerAdapter.this.notifyDataSetChanged();

                            }
                        }
                    });
                }
            });
        } else {
            holder.deletPostBt.setVisibility(View.INVISIBLE);
            holder.deletPostBt.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View view;

        private TextView descText;
        private ImageView postImage;
        private TextView postDateText;
        private CircleImageView postProfileImage;
        private TextView userPostNameText;
        private ImageView likePostImage;
        private TextView likePostCount;
        private Button deletPostBt;
        private ImageView commentsImage;
        private TextView commentsCounter;


        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            likePostImage = view.findViewById(R.id.like_comment_image);

            commentsImage = view.findViewById(R.id.comment_image);

            deletPostBt = view.findViewById(R.id.delete_post_bt);
        }

        public void setDescText(String text) {
            descText = view.findViewById(R.id.post_comment_disc_tv);
            descText.setText(text);
        }

        public void setPostImage(String image, String thumbImage) {
            postImage = view.findViewById(R.id.post_comment_image);
            RequestOptions requestOptions = new RequestOptions();

            requestOptions.placeholder(R.drawable.image_placeholder);
            Glide.with(mContext).applyDefaultRequestOptions(requestOptions).load(image)
                    .thumbnail(Glide.with(mContext).load(thumbImage))
                    .into(postImage);
        }

        public void setPostDate(String date) {
            postDateText = view.findViewById(R.id.post_date_tv);
            postDateText.setText(date);
        }

        public void setUserData(String userName, String userImage) {

            postProfileImage = view.findViewById(R.id.post_comment_profile_image);
            userPostNameText = view.findViewById(R.id.post_comment_username_tv);

            userPostNameText.setText(userName);


            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.profile_placeholder);
            Glide.with(mContext).applyDefaultRequestOptions(requestOptions).load(userImage).into(postProfileImage);
        }

        public void setLikesCount(int count) {
            likePostCount = view.findViewById(R.id.likes_counter_tv);
            likePostCount.setText(count + " Likes");
        }
        public void setCommentCounter(int count){
            commentsCounter = view.findViewById(R.id.comment_post_counter_tv);
            commentsCounter.setText(count + " Comments");
        }

    }
}
