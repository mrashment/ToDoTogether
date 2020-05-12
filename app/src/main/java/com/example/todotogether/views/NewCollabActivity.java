package com.example.todotogether.views;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todotogether.R;
import com.example.todotogether.utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class NewCollabActivity extends InsertTaskActivity {

    private TextInputEditText etName,etDescription,etCollaborators;
    private int contentView = R.layout.activity_new_collab;
    private FirebaseDatabase fbDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        fbDatabase.getReference().child(FirebaseHelper.USERS_NODE).orderByChild("email").limitToFirst(5).startAt("ma");
    }

    @Override
    public void initViews() {
        super.initViews();
    }
}
