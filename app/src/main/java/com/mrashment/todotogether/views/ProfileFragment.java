package com.mrashment.todotogether.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.mrashment.todotogether.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Simple fragment for showing the currently signed in user.
 */
public class ProfileFragment extends Fragment {

    private TextView tvUsername,tvEmail;
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
        return inflater.inflate(R.layout.fragment_profile,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvUsername = view.findViewById(R.id.tvUsername);
        tvUsername.setText(user.getDisplayName());
        tvEmail = view.findViewById(R.id.tvUserEmail);
        tvEmail.setText(user.getEmail());
        ivProfileImage = view.findViewById(R.id.ivProfile);
        Glide.with(getActivity()).load(user.getPhotoUrl()).circleCrop().into(ivProfileImage);
    }

    private void sendToLogin() {
        getParentFragmentManager().beginTransaction().replace(R.id.midRelativeLayout,LoginFragment.getInstance(LoginFragment.PROFILE_INTENT)).commit();
    }
}
