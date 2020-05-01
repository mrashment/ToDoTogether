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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TaskViewModel mTaskViewModel;
    private Button btnCreateTasks;
    private CompositeDisposable disposable;
    private Flowable<List<Task>> mTasksFlowable;
    private List<Task> mTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        disposable = new CompositeDisposable();
        mTasks = new ArrayList<>();
        mTaskViewModel = new TaskViewModel(this.getApplication());
        mTaskViewModel.init();

        mTasksFlowable = mTaskViewModel.getTasks();
        disposable.add(mTasksFlowable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Task>>() {
                    @Override
                    public void accept(List<Task> tasks) throws Exception {
                        Log.d(TAG, "accept: " + tasks.size());
                        ArrayList<Task> newTasks = new ArrayList<>(tasks);
                        mTasks = newTasks;
                    }
                }));

        ArrayList<Task> testList = new ArrayList<>();
        testList.add(new Task("Stuff","Do Stuff","Mason"));
        testList.add(new Task("More Stuff","Do more Stuff","Mason"));

        TaskListFragment taskListFragment = new TaskListFragment(testList);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.midRelativeLayout,taskListFragment)
                .commitNow();
    }

    private void initViews() {
//        btnCreateTasks = findViewById(R.id.btnCreateTasks);
//        btnCreateTasks.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mTaskViewModel.insertTask(new Task("Do something","Get Room and Rxjava to work together","Mason"));
//                mTaskViewModel.insertTask(new Task("Do something else","Get Room and Rxjava to work together","Mason"));
//                mTaskViewModel.getTasks();
//            }
//        });
    }

}
