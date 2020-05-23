package com.example.todotogether.models;

import android.app.Application;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


import com.example.todotogether.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableObserver;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Handles the various data sources and contains logic for retrieving data from them and sending it
 * to the necessary ViewModels.
 */
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

        if (latest != null) {
            for (Task t : latest) {
                if (t.getAuthor() == null) {
                    t.setAuthor(mAuth.getUid());
                }

                insert(t);
            }
        }
    }

    public void retrieveTasksFromFirebase() {
        if (mAuth.getCurrentUser() == null) {
            Log.d(TAG, "retrieveTasksFromFirebase: user is null");
            return;
        }

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d(TAG, "onDataChange: found task from firebase");
                    Task current = child.getValue(Task.class);
                    insertLocalOnly(current);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        };

        DatabaseReference mRef = fbDatabase.getReference(FirebaseHelper.TASKS_NODE).child(mAuth.getCurrentUser().getUid());
        mRef.addValueEventListener(listener);

        DatabaseReference cRef = fbDatabase.getReference(FirebaseHelper.COLLABS_NODE).child(mAuth.getCurrentUser().getUid());
        cRef.keepSynced(true);

        Completable.timer(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: removing listener");
                        mRef.removeEventListener(listener);
                        cRef.keepSynced(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: while waiting to remove listener");
                    }
                });

    }

    public void insertLocalOnly(Task task) {
        Log.d(TAG, "insertLocalOnly: " + task.getName());
        taskDao.insert(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable.add(d);
            }

            @Override
            public void onSuccess(Long aLong) {
                Log.d(TAG, "onSuccess: inserted locally, task id = " + aLong);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError: " + e.getMessage());
            }
        });
    }

    /**
     * Main insert
     * @param task Task to insert
     */
    public PublishSubject<Task> insert(Task task) {

        PublishSubject<Task> completedTaskSubject = PublishSubject.create();

        // using a single to get id back from Room, then grab the key from Firebase once that's finished. That way we can have
        // all fields filled when we insert a task
        Single<Long> insertSingle = taskDao.insert(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        insertSingle.subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "onSubscribe: insert");
                        disposable.add(d);
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
                            completedTaskSubject.onNext(task);
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

        return completedTaskSubject;
    }

    /**
     * helper for inserting tasks into Firebase
     * @param task the task to insert
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

            insertFirebaseCollab(task,task.getTeam());
        }
    }

    public void delete(Task task) {
        if (mAuth.getCurrentUser() != null && !task.getAuthor().equals(mAuth.getCurrentUser().getUid())) {
            removeSelfFromCollab(task);
            return; // if you're not the author
        }
        taskDao.delete(task).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);

        if (mAuth.getCurrentUser() != null) {
            // collaborative tasks
            if (task.getTeam().size() > 0) {
                for (String id : task.getTeam()) {
                    fbDatabase.getReference(FirebaseHelper.COLLABS_NODE)
                            .child(id)
                            .child(task.getKey())
                            .removeValue();
                }
            }

            fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                    .child(mAuth.getCurrentUser().getUid())
                    .child(task.getTask_id().toString())
                    .getRef()
                    .setValue(null);
        }
    }

    public void deleteSome(List<Task> tasks) {

        taskDao.deleteSome(tasks).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCompletableObserver);

        if (mAuth.getCurrentUser() != null) {
            for (Task t : tasks) {
                // collaborative tasks
                if (t.getTeam().size() > 0) {
                    for (String id : t.getTeam()) {
                        fbDatabase.getReference(FirebaseHelper.COLLABS_NODE)
                                .child(id)
                                .child(t.getKey())
                                .removeValue();
                    }
                }

                fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                        .child(mAuth.getCurrentUser().getUid())
                        .child(t.getTask_id().toString())
                        .getRef()
                        .setValue(null);
            }
        }
    }

    public Completable deleteAllTasks() {
        // TODO check for collabs and take this user off them
        Log.d(TAG, "deleteAllTasks: deleting tasks");
        Completable completable = taskDao.deleteAllTasks().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(mCompletableObserver);

        if (mAuth.getCurrentUser() != null) {
            fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                    .child(mAuth.getCurrentUser().getUid())
                    .getRef()
                    .setValue(null);
        }

        return completable;
    }

    public Flowable<List<Task>> getAllTasks() {
        Log.d(TAG, "getAllTasks");
        retrieveTasksFromFirebase();
        return allTasks;
    }

    /**
     * Getting the tasks which other users have added this user to.
     * @return LiveData<List<Task>> collabs An auto updating list of Tasks we are collaborating on
     */
    public LiveData<List<Task>> getCollabs() {
        MutableLiveData<List<Task>> collabs = new MutableLiveData<>();
        List<Task> collabTasks = new ArrayList<>();

        if (mAuth.getCurrentUser() == null) {
            return collabs;
        }

        fbDatabase.getReference(FirebaseHelper.COLLABS_NODE)
                .child(mAuth.getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Log.d(TAG, "onDataChange: collabtask = " + dataSnapshot.getKey());
                            CollabHeader ch = dataSnapshot.getValue(CollabHeader.class);

                            // get the actual task associated with this header
                            fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                                    .child(ch.author)
                                    .child(ch.task_id.toString())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            // update the livedata object
                                            Task cur = dataSnapshot.getValue(Task.class);
                                            if (cur != null) {
                                                Log.d(TAG, "onDataChange: getting individual task" + cur.getName());
                                                collabTasks.add(cur);
                                                collabs.setValue(collabTasks);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Log.d(TAG, "onCancelled: getting individual collab task");
                                        }
                                    });
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        CollabHeader ch = dataSnapshot.getValue(CollabHeader.class);

                        // get the actual task associated with this header
                        fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                                .child(ch.author)
                                .child(ch.task_id.toString())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        // update the livedata object
                                        Task cur = dataSnapshot.getValue(Task.class);
                                        if (cur != null) {
                                            Log.d(TAG, "onDataChange: getting individual task" + cur.getName());
                                            collabTasks.remove(cur);
                                            collabs.setValue(collabTasks);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d(TAG, "onCancelled: getting individual collab task");
                                    }
                                });
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: cancelled");
                    }
                });



        return collabs;
    }

    public void insertFirebaseCollab(Task task, ArrayList<String> collabIds) {
        for (String id : collabIds) {
            CollabHeader header = new CollabHeader(task.getAuthor(),task.getTask_id());
            DatabaseReference mRef = fbDatabase.getReference(FirebaseHelper.COLLABS_NODE)
                    .child(id)
                    .child(task.getKey());

            mRef.setValue(header);
        }
    }

    public void removeSelfFromCollab(Task task) {
        if (mAuth.getCurrentUser() != null) {
            DatabaseReference mRef = fbDatabase.getReference(FirebaseHelper.COLLABS_NODE)
                    .child(mAuth.getCurrentUser().getUid())
                    .child(task.getKey());
            mRef.removeValue();

            ArrayList<String> newTeam = task.getTeam();
            newTeam.remove(mAuth.getCurrentUser().getUid());
            DatabaseReference teamRef = fbDatabase.getReference(FirebaseHelper.TASKS_NODE)
                    .child(task.getAuthor())
                    .child(task.getTask_id().toString())
                    .child("team");
            teamRef.setValue(newTeam);
        }
    }


    public void clearDisposable() {
        disposable.clear();
    }
}
