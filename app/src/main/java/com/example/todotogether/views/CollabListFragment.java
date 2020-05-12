package com.example.todotogether.views;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.todotogether.R;
import com.example.todotogether.adapters.TaskAdapter;
import com.example.todotogether.models.Task;
import com.example.todotogether.views.TaskListFragment;
import com.example.todotogether.viewmodels.CollabViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class CollabListFragment extends TaskListFragment{

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private LiveData<List<Task>> mCollabsLive;
    private ArrayList<Task> mCollabs;
    private TaskAdapter adapter;
    private CollabViewModel mCollabViewModel;

    // listener for fab
    private View.OnClickListener listener = new View.OnClickListener() {
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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user == null) {
            sendToLogin();
        }

    }

    @Override
    protected void setUpObserver() {
        this.mCollabs = new ArrayList<>();

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

    private void sendToLogin() {
        getParentFragmentManager().beginTransaction().replace(R.id.midRelativeLayout,LoginFragment.getInstance(LoginFragment.PROFILE_INTENT)).commit();
    }
}
