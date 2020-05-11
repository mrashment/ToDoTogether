package com.example.todotogether.utils;

import android.content.Context;
import android.renderscript.Sampler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.todotogether.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import io.reactivex.Completable;


/**
 * Helper class for performing Firebase related tasks
 */
public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";

    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseUser fbUser;
    private DatabaseReference mRef;
    private FirebaseStorage mStorage;
    private User mUser;

    public FirebaseHelper(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.mRef = FirebaseDatabase.getInstance().getReference();
        this.fbUser = mAuth.getCurrentUser();
    }


    public void insertUser() {
        this.fbUser = mAuth.getCurrentUser();
        if (fbUser == null) {
            Log.d(TAG, "insertUser: fbUser not instantiated");
            return;
        }

        mRef.child("users").child(fbUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUser = dataSnapshot.getValue(User.class);
                User toInsert;
                if (mUser == null) {
                    Log.d(TAG, "onDataChange: new user being inserted");
                    toInsert = new User(fbUser.getUid(),
                            fbUser.getDisplayName(),
                            fbUser.getEmail(),
                            fbUser.getPhotoUrl() == null ? Filepaths.ANON_USER_IMAGE : fbUser.getPhotoUrl().toString());
                    mRef.child("users").child(fbUser.getUid()).setValue(toInsert)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Log.d(TAG, "onComplete: " + task.getException());
                                    } else Log.d(TAG, "onComplete: Successfully inserted user");
                                }
                            });
                } else {
                    Log.d(TAG, "onDataChange: Found uid in database");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }

}

