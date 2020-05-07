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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TaskViewModel mTaskViewModel;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mTaskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        mTaskViewModel.init();

        mTaskViewModel.insertTask(new com.example.todotogether.models.Task("Do something","Do something", "Mason"));
        mTaskViewModel.insertTask(new com.example.todotogether.models.Task("Do something else","Do something else", "Mason"));
        getString(R.string.default_web_client_id);

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

    public void signOut() {
        mAuth.signOut();
        GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                .signOut()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.midRelativeLayout,new TaskListFragment(),"TaskListFragment")
                                .commit();
                        bottomNavigationView.setSelectedItemId(R.id.optionHome);

                    }
                });
    }

    //---------------------------------------Bottom Navigation---------------------------------------------
    public void setUpBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigation);
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
        if (mAuth.getCurrentUser() == null) {

        }
        switch(item.getItemId()) {
            case R.id.optionDeleteAll:
                mTaskViewModel.deleteAllTasks();
                return true;
            case R.id.optionSignOut:
                signOut();
                break;
            default:
                break;
        }
        return false;
    }
}
