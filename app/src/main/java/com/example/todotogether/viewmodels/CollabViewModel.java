package com.example.todotogether.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.todotogether.models.Task;
import com.example.todotogether.models.TaskRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CollabViewModel extends AndroidViewModel {

    private static final String TAG = "CollabViewModel";
    private LiveData<List<Task>> mCollabs;
    private TaskRepository taskRepository;
    private CompositeDisposable disposable;

    public CollabViewModel(@NonNull Application application) {
        super(application);
        init();
    }


    public void init() {
        if (this.mCollabs != null) {
            return;
        }
        disposable = new CompositeDisposable();
        taskRepository = new TaskRepository(getApplication());
        mCollabs = getCollabs();
    }

    public LiveData<List<Task>> getCollabs() {
        if (mCollabs == null) {
            mCollabs = taskRepository.getCollabs();
        }
        return mCollabs;
    }

    public void insertTask(Task task, ArrayList<String> collabs) {
        taskRepository.insert(task)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Task>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(Task task) {
                        taskRepository.insertFirebaseCollab(task,collabs);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: failed to insert collab header");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void delete(Task task) {
        taskRepository.delete(task);
    }

    @Override
    protected void onCleared() {
        disposable.clear();
        super.onCleared();
    }
}


















