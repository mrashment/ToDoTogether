package com.example.todotogether.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.todotogether.R;
import com.example.todotogether.models.Task;
import com.example.todotogether.viewmodels.TaskViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TaskViewModel mTaskViewModel;
    private Button btnCreateTasks;
    private LiveData<List<Task>> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mTaskViewModel = new TaskViewModel(this.getApplication());
        mTaskViewModel.init();

        mTaskViewModel.insertTask(new Task("Make code work","Get Room and Rxjava to work together","Mason"));

        mTaskViewModel.getTasks().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                Log.d(TAG, "onChanged: tasks size = " + tasks.size());
            }
        });
//        TaskListFragment taskListFragment = new TaskListFragment();
//        getSupportFragmentManager().beginTransaction()
//                .add(R.id.midRelativeLayout,taskListFragment)
//                .commitNow();
    }

    private void initViews() {
        btnCreateTasks = findViewById(R.id.btnCreateTasks);
        btnCreateTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTaskViewModel.insertTask(new Task("Do something","Get Room and Rxjava to work together","Mason"));
                mTaskViewModel.insertTask(new Task("Do something else","Get Room and Rxjava to work together","Mason"));
            }
        });
    }

}
