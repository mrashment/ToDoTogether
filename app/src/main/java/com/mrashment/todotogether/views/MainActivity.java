package com.mrashment.todotogether.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.mrashment.todotogether.R;
import com.mrashment.todotogether.viewmodels.TaskViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;

/**
 * The parent view for most views in this app. Sets up bottom navigation and toolbar functionality.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static final String PROFILE_FRAGMENT = "ProfileFragment";
    public static final String LOGIN_FRAGMENT = "LoginFragment";
    public static final String TASK_LIST_FRAGMENT = "TaskListFragment";
    public static final String COLLAB_LIST_FRAGMENT = "CollabListFragment";
    public static final String TASK_DETAILS_FRAGMENT = "TaskDetailsFragment";
    public static final String PRIVACY_POLICY_FRAGMENT = "PrivacyPolicyFragment";


    private TaskViewModel mTaskViewModel;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mTaskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        setUpToolbar();
        setUpBottomNavigation();
        progressBar = findViewById(R.id.progressBar);

        TaskListFragment taskListFragment = new TaskListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.midRelativeLayout,taskListFragment,TASK_LIST_FRAGMENT)
                .commitNow();
    }

    @Override
    protected void onResume() {
        // display privacy policy first time they open the app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.commit();
            displayPrivacyPolicy();
        }
        if (mAuth.getCurrentUser() != null) {
            mTaskViewModel.sync();
        }
        super.onResume();
    }

    public void displayPrivacyPolicy() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.midRelativeLayout,new PrivacyPolicyFragment(),PRIVACY_POLICY_FRAGMENT)
                .addToBackStack(null)
                .commit();
    }

    public void signOut() {
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signOut();
        GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                .signOut()
                .addOnCompleteListener(task -> {
                    mTaskViewModel.deleteAllTasks().subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            MainActivity.this.getViewModelStore().clear();
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.midRelativeLayout,new TaskListFragment(),TASK_LIST_FRAGMENT)
                            .commit();
                    bottomNavigationView.setSelectedItemId(R.id.optionHome);
                });
    }


    //---------------------------------------Bottom Navigation---------------------------------------------
    public void setUpBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.optionHome);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment current = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.midRelativeLayout);
            if (current == null) { current = new Fragment(); }
            Log.d(TAG, "onNavigationItemSelected: current fragment = " + current.getClass());
            FragmentTransaction transaction = MainActivity.this.getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.optionProfile:
                    if (Objects.equals(current.getTag(), PROFILE_FRAGMENT)) return true;
                    if (mAuth.getCurrentUser() == null) {
                        LoginFragment loginFragment = LoginFragment.getInstance(LoginFragment.PROFILE_INTENT);
                        transaction.replace(R.id.midRelativeLayout,loginFragment,LOGIN_FRAGMENT);
                    } else {
                        transaction.replace(R.id.midRelativeLayout, new ProfileFragment(), PROFILE_FRAGMENT);
                    }
                    break;
                case R.id.optionHome:
                    if (Objects.equals(current.getTag(), TASK_LIST_FRAGMENT)) return true;
                    transaction.replace(R.id.midRelativeLayout,new TaskListFragment(),TASK_LIST_FRAGMENT);
                    break;
                case R.id.optionSocial:
                    if (Objects.equals(current.getTag(), COLLAB_LIST_FRAGMENT)) return true;
                    if (mAuth.getCurrentUser() == null) {
                        LoginFragment loginFragment = LoginFragment.getInstance(LoginFragment.SOCIAL_INTENT);
                        transaction.replace(R.id.midRelativeLayout,loginFragment,LOGIN_FRAGMENT);
                    } else {
                        transaction.replace(R.id.midRelativeLayout, new CollabListFragment(), COLLAB_LIST_FRAGMENT);
                    }
                    break;
                default:
                    break;

            }
            transaction.commit();
            return true;
        });
    }

    // ---------------------------------------------------Toolbar------------------------------------------------------
    public void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        if (mAuth.getCurrentUser() == null) {
            menu.findItem(R.id.optionSignOut).setTitle(R.string.sign_in);
        } else {
            menu.findItem(R.id.optionSignOut).setTitle(R.string.sign_out);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
//            case R.id.optionDeleteAll:
//                AlertDialog confirmDialog = new AlertDialog.Builder(this)
//                        .setTitle(R.string.are_you_sure)
//                        .setMessage("This will delete all tasks created by you.")
//                        .setPositiveButton(R.string.delete_all, (dialog, which) -> mTaskViewModel.deleteAllTasks())
//                        .setNegativeButton(R.string.cancel,null)
//                        .create();
//                confirmDialog.show();
//                return true;
            case R.id.optionContact:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:mrashment1@gmail.com"));

                this.startActivity(intent);
                break;
            case R.id.optionPrivacyPolicy:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.midRelativeLayout,new PrivacyPolicyFragment(),PRIVACY_POLICY_FRAGMENT)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.optionSignOut:
                if (mAuth.getCurrentUser() != null) {
                    signOut();
                    item.setTitle(R.string.sign_in);
                } else {
                    Fragment cur = getSupportFragmentManager().findFragmentById(R.id.midRelativeLayout);
                    int loginIntent;
                    if (cur instanceof LoginFragment) loginIntent = ((LoginFragment)cur).getFragmentIntent();
                    else loginIntent = LoginFragment.MAIN_PAGE_INTENT;
                    LoginFragment loginFragment = LoginFragment.getInstance(loginIntent);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.midRelativeLayout,loginFragment,LOGIN_FRAGMENT)
                            .commitNow();
                    loginFragment.executeSignIn();
                    item.setTitle(R.string.sign_out);
                }
                break;
            default:
                break;
        }
        return false;
    }
}
