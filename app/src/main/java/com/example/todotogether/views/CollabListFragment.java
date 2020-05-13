package com.example.todotogether.views;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.todotogether.R;
import com.example.todotogether.adapters.TaskAdapter;
import com.example.todotogether.adapters.UserAdapter;
import com.example.todotogether.models.Task;
import com.example.todotogether.views.TaskListFragment;
import com.example.todotogether.viewmodels.CollabViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class CollabListFragment extends TaskListFragment{

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private LiveData<List<Task>> mCollabsLive;
    private ArrayList<Task> mCollabs;
    private TaskAdapter adapter;
    private CollabViewModel mCollabViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            sendToLogin();
        }

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(listener);
        setupRecyclerView(view);
    }

    @Override
    protected void setUpObserver() {
        mCollabs = new ArrayList<>();
        adapter = new TaskAdapter(mCollabs,this);
        disposable = new CompositeDisposable();

        mCollabViewModel = new ViewModelProvider(requireActivity()).get(CollabViewModel.class);
        mCollabsLive = mCollabViewModel.getCollabs();

        mCollabsLive.observe(getActivity(), new Observer<List<Task>>() {
            @Override
            public void onChanged(List<Task> tasks) {
                parseDifferences(tasks,mCollabs);

                adapter.setTasks(mCollabs);
            }
        });
    }

    @Override
    public void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new TaskAdapter(mCollabs,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    private void sendToLogin() {
        getParentFragmentManager().beginTransaction().replace(R.id.midRelativeLayout,LoginFragment.getInstance(LoginFragment.PROFILE_INTENT)).commit();
    }


}
