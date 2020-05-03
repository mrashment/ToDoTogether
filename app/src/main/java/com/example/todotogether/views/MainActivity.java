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

    private TaskViewModel mTaskViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpToolbar();

        TaskListFragment taskListFragment = new TaskListFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.midRelativeLayout,taskListFragment,"ListFragment")
                .commitNow();
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
