package com.example.todotogether.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.todotogether.models.Task;
import com.example.todotogether.models.TaskRepository;

import org.reactivestreams.Subscription;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TaskViewModel extends AndroidViewModel {

    private MutableLiveData<List<Task>> mTasks;
    private TaskRepository taskRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        if (mTasks != null) {
            return;
        }
        taskRepository = new TaskRepository(getApplication());
        mTasks = taskRepository.getAllTasks();
    }

    public LiveData<List<Task>> getTasks() {
        return mTasks;
    }

    public void insertTask(Task task) {
        taskRepository.insert(task);
    }

}
