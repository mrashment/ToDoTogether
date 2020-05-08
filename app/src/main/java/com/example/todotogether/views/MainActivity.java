package com.example.todotogether.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.todotogether.R;
import com.example.todotogether.viewmodels.TaskViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TaskViewModel mTaskViewModel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mTaskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        mTaskViewModel.init();

        setUpToolbar();
        setUpBottomNavigation();

        TaskListFragment taskListFragment = new TaskListFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.midRelativeLayout,taskListFragment,"TaskListFragment")
                .commitNow();
    }

    public void toast(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public TaskViewModel getTaskViewModel() {return this.mTaskViewModel;}



    //---------------------------------------Bottom Navigation---------------------------------------------
    public void setUpBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.optionHome);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment current = MainActivity.this.getSupportFragmentManager().findFragmentById(R.id.midRelativeLayout);
                Log.d(TAG, "onNavigationItemSelected: current fragment = " + current.getClass());
                FragmentTransaction transaction = MainActivity.this.getSupportFragmentManager().beginTransaction();

                switch (item.getItemId()) {
                    case R.id.optionProfile:
                        if (current instanceof ProfileFragment) return true;
                        if (mAuth.getCurrentUser() == null) {
                            LoginFragment loginFragment = LoginFragment.getInstance(LoginFragment.PROFILE_INTENT);
                            transaction.replace(R.id.midRelativeLayout,loginFragment,"LoginFragment");
                        } else {
                            transaction.replace(R.id.midRelativeLayout, new ProfileFragment(), "ProfileFragment");
                        }
                        break;
                    case R.id.optionHome:
                        if (current instanceof TaskListFragment) return true;
                        transaction.replace(R.id.midRelativeLayout,new TaskListFragment(),"TaskListFragment");
                        break;
                    case R.id.optionSocial:
                        if (mAuth.getCurrentUser() == null) {
                            LoginFragment loginFragment = LoginFragment.getInstance(LoginFragment.SOCIAL_INTENT);
                            transaction.replace(R.id.midRelativeLayout,loginFragment,"LoginFragment");
                        } else {

                        }
                        break;
                    default:
                        break;

                }
                transaction.commit();
                return true;
            }
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.optionDeleteAll:
                mTaskViewModel.deleteAllTasks();
                return true;
            default:
                break;
        }
        return false;
    }
}
