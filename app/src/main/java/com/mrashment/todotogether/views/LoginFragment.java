package com.mrashment.todotogether.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.mrashment.todotogether.R;
import com.mrashment.todotogether.utils.FirebaseHelper;
import com.mrashment.todotogether.viewmodels.TaskViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Displayed whenever the user is trying to enter a fragment or activity that requires them to be
 * signed into Google/Firebase.
 */
public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    private SignInButton btnLogin;
    private final int RC_SIGN_IN = 7;
    private int fragmentIntent = MAIN_PAGE_INTENT;
    public static final String FRAGMENT_INTENT = "Intent";
    public static final int PROFILE_INTENT = 100;
    public static final int MAIN_PAGE_INTENT = 200;
    public static final int SOCIAL_INTENT = 300;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView agreement;
    private ImageView link;

    public static LoginFragment getInstance(int intent) {
        Bundle bundle = new Bundle();
        bundle.putInt(FRAGMENT_INTENT,intent);
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentIntent = getArguments().getInt(FRAGMENT_INTENT);
        setUpSignIn();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeSignIn();
            }
        });
        agreement = view.findViewById(R.id.tvAgreement);
        link = view.findViewById(R.id.ivLink);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.midRelativeLayout,new PrivacyPolicyFragment(),MainActivity.PRIVACY_POLICY_FRAGMENT)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }


    public void setUpSignIn() {
        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getActivity().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(),gso);
    }

    public void executeSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.d(TAG, "onActivityResult: sign-in failed" + e.getStatusCode());
            }
        }
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("Login", "firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            new FirebaseHelper().insertUser();
                            TaskViewModel mTaskViewModel = new ViewModelProvider(getActivity()).get(TaskViewModel.class);
                            mTaskViewModel.migrateToFirebase();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getActivity(), "Auth Failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public int getFragmentIntent() {
        return fragmentIntent;
    }

    public void updateUI() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        switch (fragmentIntent) {
            case PROFILE_INTENT:
                transaction.replace(R.id.midRelativeLayout,new ProfileFragment(),MainActivity.PROFILE_FRAGMENT);
                break;
            case MAIN_PAGE_INTENT:
                transaction.replace(R.id.midRelativeLayout,new TaskListFragment(),MainActivity.TASK_LIST_FRAGMENT);
                break;
            case SOCIAL_INTENT:
                transaction.replace(R.id.midRelativeLayout, new CollabListFragment(), MainActivity.COLLAB_LIST_FRAGMENT);
                break;
            default:
                break;
        }
        transaction.commit();
    }
}
