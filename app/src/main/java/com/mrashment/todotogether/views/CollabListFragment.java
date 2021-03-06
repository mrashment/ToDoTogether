package com.mrashment.todotogether.views;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.mrashment.todotogether.R;
import com.mrashment.todotogether.adapters.TaskAdapter;
import com.mrashment.todotogether.models.Task;
import com.mrashment.todotogether.viewmodels.CollabViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.mrashment.todotogether.viewmodels.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * An extension of TaskListFragment for displaying tasks on the collaboration screen. Retrieves data
 * only from Firebase rather than from the local database.
 */
public class CollabListFragment extends TaskListFragment{

    private FirebaseAuth mAuth;
    private LiveData<List<Task>> mCollabsLive;
    private CollabViewModel mCollabViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // listener for fab
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()) {
                    case R.id.fab:
                        // add a new task
                        Intent intent = new Intent(getActivity(),NewCollabActivity.class);
                        intent.putExtra("requestCode",INSERT_TASK_REQUEST);
                        startActivityForResult(intent,INSERT_TASK_REQUEST);
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onResume() {
        if (mAuth.getCurrentUser() == null) {
            sendToLogin();
        }
        super.onResume();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(listener);
        setupRecyclerView(view);
    }

    @Override
    protected void setUpObserver() {
        mTasks = new ArrayList<>();
        adapter = new TaskAdapter(mTasks,this,getActivity());
        disposable = new CompositeDisposable();

        mTaskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        mTaskViewModel.sync();
        mCollabViewModel = new ViewModelProvider(requireActivity()).get(CollabViewModel.class);
        mCollabsLive = mCollabViewModel.getCollabs();

        mCollabsLive.observe(getActivity(), new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                parseDifferences(tasks,mTasks);

                adapter.setTasks(mTasks);
            }
        });
    }

    private void sendToLogin() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.midRelativeLayout, LoginFragment.getInstance(LoginFragment.SOCIAL_INTENT))
                .commit();
    }

    @Override
    public void onCheckBoxesClicked(List<Task> tasksToDelete) {
        for (Task t: tasksToDelete) {
            mCollabViewModel.delete(t);
        }
    }
}
