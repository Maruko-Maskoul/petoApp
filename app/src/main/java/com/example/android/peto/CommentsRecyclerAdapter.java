package com.example.android.peto;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {
    private List<CommentsPost> mCommentsList;
    private Context mContext;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mFirebaseAuth;

    public CommentsRecyclerAdapter(Context context, List<CommentsPost> commentsPostList) {
        this.mContext = context;
        this.mCommentsList = commentsPostList;

    }

    @NonNull
    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.comments_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentsRecyclerAdapter.ViewHolder holder, int position) {

        String comment = mCommentsList.get(position).getMessage();
        holder.setComment(comment);

        String userId = mCommentsList.get(position).getUser_id();

        mFirebaseFirestore.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    String name = task.getResult().getString("name");
                    holder.setUserName(name);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (!mCommentsList.isEmpty()) {
            return mCommentsList.size();
        }
        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTV;
        TextView commentTV;

        public ViewHolder(View itemView) {
            super(itemView);
            userNameTV = itemView.findViewById(R.id.name_commenter);
            commentTV = itemView.findViewById(R.id.comment);
        }

        public void setUserName(String name) {
            userNameTV.setText(name);
        }

        public void setComment(String comment) {
            commentTV.setText(comment);
        }
    }
}
