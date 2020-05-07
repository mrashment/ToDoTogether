package com.example.todotogether.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.todotogether.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private TextView tvUsername,tvBio,tvEmail;
    private ImageView ivProfileImage;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            sendToLogin();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile,container,false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvUsername = view.findViewById(R.id.tvUsername);
        tvUsername.setText(user.getDisplayName());
        tvEmail = view.findViewById(R.id.tvUserEmail);
        tvEmail.setText(user.getEmail());
        tvBio = view.findViewById(R.id.tvBio);
        tvBio.setHint("You can create a bio to display here.");
    }

    public void sendToLogin() {
        getParentFragmentManager().beginTransaction().replace(R.id.midRelativeLayout,LoginFragment.getInstance(LoginFragment.PROFILE_INTENT)).commit();
    }
}
