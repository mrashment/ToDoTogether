package com.example.todotogether.models;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.reactivestreams.Subscription;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TaskRepository {
    private static final String TAG = "TaskRepository";

    private TaskDao taskDao;
    private MutableLiveData<List<Task>> allTasks;
    private CompositeDisposable disposable;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        disposable = new CompositeDisposable();
        allTasks = new MutableLiveData<>();
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

    public MutableLiveData<List<Task>> getAllTasks() {
        taskDao.getAllTasks().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new FlowableSubscriber<List<Task>>() {
                    @Override
                    public void onSubscribe(Subscription s) {

                    }

                    @Override
                    public void onNext(List<Task> tasks) {
                        Log.d(TAG, "onNext: setting allTasks");

                        allTasks.setValue(tasks);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError: getAllTasks()" + t.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
        return allTasks;
    }

}
