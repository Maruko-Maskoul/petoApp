package com.example.android.peto;

import com.google.firebase.firestore.Exclude;

import io.reactivex.annotations.NonNull;

public class PetoPostId {

    @Exclude
    public String PetoPostId;

    public <T extends PetoPostId> T withId(@NonNull final String id) {
        this.PetoPostId = id;

        return (T) this;
    }
}
