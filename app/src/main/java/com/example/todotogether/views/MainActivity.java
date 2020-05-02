package com.example.todotogether.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.todotogether.R;
import com.example.todotogether.viewmodels.TaskViewModel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Application application;
    private TaskViewModel mTaskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpActionBar();

        application = getApplication();
        mTaskViewModel = new TaskViewModel(application);
        mTaskViewModel.init();

        TaskListFragment taskListFragment = new TaskListFragment(application);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.midRelativeLayout,taskListFragment)
                .commitNow();
    }

    public void setUpActionBar() {
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
                break;
            default:
                break;
        }
        return false;
    }
}
