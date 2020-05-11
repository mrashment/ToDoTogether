package com.example.todotogether.models;

import android.app.Application;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.RxRoom;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class TaskRepository {
    private static final String TAG = "TaskRepository";

    private TaskDao taskDao;
    private Flowable<List<Task>> allTasks;
    private CompositeDisposable disposable;
    private FirebaseDatabase fbDatabase;
    private FirebaseAuth mAuth;
    private List<Task> latest;


    public TaskRepository(Application application) {
        TaskDatabase database = TaskDatabase.getInstance(application);
        taskDao = database.taskDao();
        allTasks = taskDao.getAllTasks();
        disposable = new CompositeDisposable();
        cacheLatest(allTasks.toObservable());
        mAuth = FirebaseAuth.getInstance();
        fbDatabase = FirebaseDatabase.getInstance();
    }

    private void cacheLatest(Observable<List<Task>> hotObservable) {
        hotObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Task>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: ");
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(List<Task> tasks) {
                        Log.d(TAG, "onNext: latest size: "+ tasks.size());
                        latest = tasks;
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }
                });
    }

    private CompletableObserver mCompletableObserver = new CompletableObserver() {
        @Override
        public void onSubscribe(Disposable d) {
            disposable.add(d);
        }

        @Override
        public void onComplete() {
            Log.d(TAG, "onComplete");
        }

        @Override
        public void onError(Throwable e) {
            Log.d(TAG, "onError: " + e.getMessage());
        }
    };

    public void uploadTasksToFirebase() {
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "uploadTasksToFirebase: user is null");
            return;
        }

        DatabaseReference mRef = fbDatabase.getReference("tasks").child(mAuth.getCurrentUser().getUid());
        for (Task t : latest) {
            mRef.child(Integer.toString(t.getTask_id())).setValue(t);
        }
    }

    public void retrieveTasksFromFirebase() {
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "retrieveTasksFromFirebase: user is null");
            return;
        }

        fbDatabase.getReference("tasks").child(mAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Task current = child.getValue(Task.class);
                            Log.d(TAG, "onDataChange: " + current.getTask_id());
                            insert(current);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    public void insert(final Task task) {
        taskDao.insert(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);
    }

    public void update(Task task) {
        taskDao.update(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);
    }

    public void delete(Task task) {
        taskDao.delete(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);
    }

    public void deleteSome(List<Task> tasks) {
        taskDao.deleteSome(tasks).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);
    }

    public void deleteAllTasks() {
        Log.d(TAG, "deleteAllTasks: deleting tasks");
        taskDao.deleteAllTasks().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);
    }

    public Flowable<List<Task>> getAllTasks() {
        Log.d(TAG, "getAllTasks");
        return allTasks;
    }

}
