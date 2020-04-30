package com.example.todotogether.models;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TaskRepository {
    private static final String TAG = "TaskRepository";

    private TaskDao taskDao;
    private LiveData<List<Task>> allTasks;
    private CompositeDisposable disposable;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        disposable = new CompositeDisposable();
    }

    public void insert(final Task task) {
        taskDao.insert(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: inserted task " + task.getName());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: error inserting task" + e.getMessage());
                    }
                });
    }

    public void update(Task task) {

    }

    public void delete(Task task) {

    }

    public void deleteAllNodes() {

    }

    public LiveData<List<Task>> getAllTasks() {
        return allTasks;
    }

}
