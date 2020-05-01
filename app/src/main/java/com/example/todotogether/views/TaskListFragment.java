package com.example.todotogether.views;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.todotogether.R;
import com.example.todotogether.adapters.TaskAdapter;
import com.example.todotogether.models.Task;
import com.example.todotogether.viewmodels.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.disposables.CompositeDisposable;

public class TaskListFragment extends Fragment {
    private static final String TAG = "TaskListFragment";

    private RecyclerView recyclerView;
    private TaskViewModel mTaskViewModel;
    private Flowable<List<Task>> mTasksFlowable;
    private ArrayList<Task> mTasks;
    private TaskAdapter adapter;
    private CompositeDisposable disposable;

    public TaskListFragment(Application application) {
        this.mTasks = new ArrayList<>();
        disposable = new CompositeDisposable();

        mTaskViewModel = new TaskViewModel(application);
        mTaskViewModel.init();

        mTasksFlowable = mTaskViewModel.getTasks();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disposable.add(mTasksFlowable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Task>>() {
                    @Override
                    public void accept(List<Task> tasks) throws Exception {
                        Log.d(TAG, "accept: updating list");
                        adapter.setTasks(tasks);
                    }
                }));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupRecyclerView(view);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new TaskAdapter(mTasks);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }
}
