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

import org.reactivestreams.Subscription;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TaskViewModel mTaskViewModel;
    private Button btnCreateTasks;
    private Flowable<List<Task>> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        mTaskViewModel = new TaskViewModel(this.getApplication());
        mTaskViewModel.init();

        mTaskViewModel.insertTask(new Task("Make code work","Get Room and Rxjava to work together","Mason"));

        mTasks = mTaskViewModel.getTasks();
        mTasks.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Task>>() {
                    @Override
                    public void accept(List<Task> tasks) throws Exception {
                        Log.d(TAG, "accept: " + tasks.size());
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
                mTaskViewModel.getTasks();
            }
        });
    }

}
