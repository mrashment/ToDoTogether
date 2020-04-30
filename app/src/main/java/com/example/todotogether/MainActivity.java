package com.example.todotogether;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.example.todotogether.models.Task;
import com.example.todotogether.viewmodels.TaskViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaskViewModel mTaskViewModel;
    private LiveData<List<Task>> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mTaskViewModel = new TaskViewModel(this.getApplication());

        mTaskViewModel.insertTask(new Task("Make code work","Get Room and Rxjava to work together","Mason"));

        TaskListFragment taskListFragment = new TaskListFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.midRelativeLayout,taskListFragment)
                .commitNow();
    }

    private void initViews() {
    }
}
