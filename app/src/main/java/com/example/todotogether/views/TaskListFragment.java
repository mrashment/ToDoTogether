package com.example.todotogether.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.todotogether.R;
import com.example.todotogether.adapters.TaskAdapter;
import com.example.todotogether.models.Task;

import java.util.List;

import io.reactivex.Flowable;

public class TaskListFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Task> mTasks;

    public TaskListFragment(List<Task> tasks) {
        this.mTasks = tasks;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        TaskAdapter adapter = new TaskAdapter(mTasks);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }
}
