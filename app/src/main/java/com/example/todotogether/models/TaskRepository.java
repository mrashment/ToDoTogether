package com.example.todotogether.models;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.todotogether.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


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

        DatabaseReference mRef = fbDatabase.getReference(FirebaseHelper.TASKS_NODE).child(mAuth.getCurrentUser().getUid());
        for (Task t : latest) {
            t.setAuthor(mAuth.getUid());
            insert(t);
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
                        Log.d(TAG, "onCancelled: ");
                    }
                });
    }

    /**
     * Main insert
     * @param task Task to insert
     */
    public void insert(Task task) {
        if (mAuth.getCurrentUser() != null && !task.getAuthor().equals(mAuth.getCurrentUser().getUid())) {
            Log.d(TAG, "insert: Failed to insert, you are not the author. You: " + mAuth.getCurrentUser().getUid() + " Author: "
            + task.getAuthor());
            return; // if you're not the author, don't add this to local memory
        }

        taskDao.insert(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: insert");
                    }

                    @Override
                    public void onSuccess(Long aLong) {
                        task.setTask_id(aLong.intValue());
                        if (mAuth.getCurrentUser() != null && task.getKey() == null) {
                            String key = fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                                    .child(mAuth.getCurrentUser().getUid())
                                    .child(task.getTask_id().toString())
                                    .push()
                                    .getKey();
                            task.setKey(key);
                            update(task);
                        }
                        Log.d(TAG, "onSuccess: task name: " + task.getName() + " task key: " + task.getKey());
                        insertIntoFirebase(task);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }
                });
    }

    /**
     * helper for inserting tasks into Firebase
     * @param task
     */
    public void insertIntoFirebase(Task task) {
        if (mAuth.getCurrentUser() != null) {
            DatabaseReference newRef = fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                    .child(mAuth.getCurrentUser().getUid())
                    .child(task.getTask_id().toString());

            newRef.setValue(task);
        }
    }

    public void update(Task task) throws NullPointerException {
        if (mAuth.getCurrentUser() != null && !task.getAuthor().equals(mAuth.getCurrentUser().getUid())) {
            return; // if you're not the author, don't update this
        }
        if (task.getTask_id() == null) {
            throw new NullPointerException("This task has no id");
        }
        taskDao.update(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);

        if (mAuth.getCurrentUser() != null) {
            fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                    .child(mAuth.getCurrentUser().getUid())
                    .child(task.getTask_id().toString())
                    .setValue(task);
        }
    }

    public void delete(Task task) {
        if (mAuth.getCurrentUser() != null && !task.getAuthor().equals(mAuth.getCurrentUser().getUid())) {
            return; // if you're not the author, TODO allow user to take themselves off collab
        }
        taskDao.delete(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);

        if (mAuth.getCurrentUser() != null) {
            fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                    .child(mAuth.getCurrentUser().getUid())
                    .child(task.getTask_id().toString())
                    .getRef()
                    .setValue(null);
        }
    }

    public void deleteSome(List<Task> tasks) {
        // TODO check for collabs and take this user off them
        taskDao.deleteSome(tasks).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);

        if (mAuth.getCurrentUser() != null) {
            for (Task t : tasks) {
                fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                        .child(mAuth.getCurrentUser().getUid())
                        .child(t.getTask_id().toString())
                        .getRef()
                        .setValue(null);
            }
        }
    }

    public void deleteAllTasks() {
        // TODO check for collabs and take this user off them
        Log.d(TAG, "deleteAllTasks: deleting tasks");
        taskDao.deleteAllTasks().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);

        if (mAuth.getCurrentUser() != null) {
            fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                    .child(mAuth.getCurrentUser().getUid())
                    .getRef()
                    .setValue(null);
        }
    }

    public Flowable<List<Task>> getAllTasks() {
        Log.d(TAG, "getAllTasks");
        retrieveTasksFromFirebase();
        return allTasks;
    }

    /**
     * Getting the tasks which other users have added this user to.
     * // TODO Optimize this
     * @return LiveData<List<Task>> collabs An auto updating list of Tasks we are collaborating on
     */
    public LiveData<List<Task>> getCollabs() {
        MutableLiveData<List<Task>> collabs = new MutableLiveData<>();

        if (mAuth.getCurrentUser() == null) {
            return collabs;
        }
        // get list of task headers this user has been added to
        fbDatabase.getReference("collabs")
                .child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Task> collabTasks = new ArrayList<>();
                        Log.d(TAG, "onDataChange: " + dataSnapshot.getKey());

                        // iterate through that list of task headers
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: collabtask = " + d.getKey());
                            CollabHeader ch = d.getValue(CollabHeader.class);

                            // get the actual task associated with this header
                            fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                                    .child(ch.author)
                                    .child(ch.task_id.toString())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            // update the livedata object
                                            Task cur = dataSnapshot.getValue(Task.class);
                                            Log.d(TAG, "onDataChange: getting individual task" + cur.getName());
                                            collabTasks.add(cur);
                                            collabs.setValue(collabTasks);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.d(TAG, "onCancelled: getting individual collab task");
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: getting collab list");
                    }
                });
        return collabs;
    }

}
