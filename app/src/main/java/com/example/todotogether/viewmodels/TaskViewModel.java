package com.example.todotogether.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.todotogether.models.Task;
import com.example.todotogether.models.TaskRepository;

import org.reactivestreams.Subscription;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

public class TaskViewModel extends AndroidViewModel {
    private static final String TAG = "TaskViewModel";

    private Flowable<List<Task>> mTasks;
    private TaskRepository taskRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        if (this.mTasks != null) {
            return;
        }
        taskRepository = new TaskRepository(getApplication());
        Log.d(TAG, "init: getting tasks");
    }

    public void insertTask(Task task) {
        taskRepository.insert(task);
    }

    public void updateTask(Task task) {
        taskRepository.update(task);
    }

    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }

    public void deleteAllTasks() {
        taskRepository.deleteAllTasks();
    }

    public Flowable<List<Task>> getTasks() {
        Log.d(TAG, "getTasks: ");
        return taskRepository.getAllTasks();
    }
}
