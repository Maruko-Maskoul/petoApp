package com.example.android.peto;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView mPetoListView;
    private PetoRecyclerAdapter mPetoRecyclerAdapter;

    private DocumentSnapshot lastVisible;
    private List<PetoPost> mPostList;


    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mFirebaseAuth;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mPostList = new ArrayList<>();

        mPetoRecyclerAdapter = new PetoRecyclerAdapter(getContext(), mPostList);

        mPetoListView = view.findViewById(R.id.main_rec_view);
        mPetoListView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        mPetoListView.setHasFixedSize(true);
        mPetoListView.setAdapter(mPetoRecyclerAdapter);

        mFirebaseAuth = FirebaseAuth.getInstance();

        if (mFirebaseAuth.getCurrentUser() != null) {

            mPetoListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean isReachedBottom = !recyclerView.canScrollVertically(1);

                    if (isReachedBottom) {

                        String desc = lastVisible.getString("post_desc");
                        loadMorePost();
                    }
                }
            });

            mFirebaseFirestore = FirebaseFirestore.getInstance();

            Query firstQuery = mFirebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d("HomeFragment : ", "Error:" + e.getMessage());
                    } else {
                        // Get the last visible document
                        assert queryDocumentSnapshots != null;
                        if (!queryDocumentSnapshots.getDocumentChanges().isEmpty()) {
                            lastVisible = queryDocumentSnapshots.getDocuments()
                                    .get(queryDocumentSnapshots.size() - 1);
                        }

                        // Check if the item in document change

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String petoPostId = doc.getDocument().getId();
                                PetoPost petoPost = doc.getDocument().toObject(PetoPost.class).withId(petoPostId);

                                String userId = doc.getDocument().getString("user_id");

                                mPostList.add(petoPost);
                                mPetoRecyclerAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }

            });
        }
        return view;
    }


    private void loadMorePost() {
        Query nextQuery = mFirebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.d("HomeFragment : ", "Error:" + e.getMessage());
                } else {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the last visible document
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        // Check if the item in document change
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String petoPostId = doc.getDocument().getId();

                                PetoPost petoPost = doc.getDocument().toObject(PetoPost.class).withId(petoPostId);
                                mPostList.add(petoPost);
                                mPetoRecyclerAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }
            }

        });
    }
}
