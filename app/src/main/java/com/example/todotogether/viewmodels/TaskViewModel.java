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

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * The main ViewModel for handling task manipulation and communication with the repository.
 */
public class TaskViewModel extends AndroidViewModel {
    private static final String TAG = "TaskViewModel";

    private Flowable<List<Task>> mTasks;
    private LiveData<List<Task>> mCollabs;
    private TaskRepository taskRepository;
    private CompositeDisposable disposable;


    public TaskViewModel(@NonNull Application application) {
        super(application);
        init();
    }

    public void init() {
        if (this.mTasks != null) {
            return;
        }
        disposable = new CompositeDisposable();
        taskRepository = new TaskRepository(getApplication());
        mTasks = getTasks();
    }

    public void migrateToFirebase() {
        taskRepository.uploadTasksToFirebase();
        taskRepository.retrieveTasksFromFirebase();
    }

    public void sync() {
        taskRepository.retrieveTasksFromFirebase();
        taskRepository.getCollabs();
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

    public void updateTask(Task task) {
        taskRepository.update(task);
    }

    public void deleteTask(Task task) {
        taskRepository.delete(task);
    }

    public void deleteSome(List<Task> tasks) {taskRepository.deleteSome(tasks);}

    public Completable deleteAllTasks() {
        return taskRepository.deleteAllTasks();
    }

    public Flowable<List<Task>> getTasks() {
        if (mTasks == null) {
            mTasks = taskRepository.getAllTasks();
        }
        return mTasks;
    }

    @Override
    protected void onCleared() {
        disposable.clear();
        taskRepository.clearDisposable();
        super.onCleared();
    }
}
