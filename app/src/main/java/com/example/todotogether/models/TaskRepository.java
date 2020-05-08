package com.example.todotogether.models;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.Consumer;

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
    private Flowable<List<Task>> allTasks;
    private CompositeDisposable disposable;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference myRef;

    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
        disposable = new CompositeDisposable();
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference("message");
    }

    public void simpleRequest() {
        myRef.setValue("Hello, World!");
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
                        Log.d(TAG, "onComplete: inserted task " + task.getName() + taskDao.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: error inserting task" + e.getMessage());
                    }
                });
    }

    public void update(Task task) {
        taskDao.update(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: updated task " + task.getName() + taskDao.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: error inserting task" + e.getMessage());
                    }
                });
    }

    public void delete(Task task) {
        taskDao.delete(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: deleted task " + task.getName() + taskDao.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: error deleting task" + e.getMessage());
                    }
                });
    }

    public void deleteAllTasks() {
        Log.d(TAG, "deleteAllTasks: deleting tasks");
        taskDao.deleteAllTasks().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: deleted all tasks");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: error deleting task" + e.getMessage());
                    }
                });
    }

    public Flowable<List<Task>> getAllTasks() {
        Log.d(TAG, "getAllTasks");
        return allTasks;
    }

}
