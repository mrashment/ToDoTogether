package com.example.todotogether.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todotogether.models.Task;
import com.example.todotogether.models.TaskRepository;

import java.util.List;

public class CollabViewModel extends AndroidViewModel {

    private LiveData<List<Task>> mCollabs;
    private TaskRepository taskRepository;

    public CollabViewModel(@NonNull Application application) {
        super(application);
        init();
    }


    public void init() {
        if (this.mCollabs != null) {
            return;
        }
        taskRepository = new TaskRepository(getApplication());
        mCollabs = getCollabs();
    }

    public LiveData<List<Task>> getCollabs() {
        if (mCollabs == null) {
            mCollabs = taskRepository.getCollabs();
        }
        return mCollabs;
    }


}


















