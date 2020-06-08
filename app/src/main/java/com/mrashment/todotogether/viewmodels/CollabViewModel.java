package com.mrashment.todotogether.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrashment.todotogether.models.Task;
import com.mrashment.todotogether.models.TaskRepository;
import com.mrashment.todotogether.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * ViewModel which deals with certain tasks like retrieving user information and collaborations.
 */
public class CollabViewModel extends AndroidViewModel {

    private static final String TAG = "CollabViewModel";
    private LiveData<List<Task>> mCollabs;
    private TaskRepository taskRepository;
    private CompositeDisposable disposable;
    private MutableLiveData<HashMap<String,String>> userProfileImages;

    public CollabViewModel(@NonNull Application application) {
        super(application);
        init();
    }


    public void init() {
        if (this.mCollabs != null && this.userProfileImages != null) {
            return;
        }
        disposable = new CompositeDisposable();
        taskRepository = new TaskRepository(getApplication());
        userProfileImages = new MutableLiveData<>();
        userProfileImages.setValue(new HashMap<>());
        mCollabs = getCollabs();
    }

    public LiveData<List<Task>> getCollabs() {
        if (mCollabs == null) {
            mCollabs = taskRepository.getCollabs();
        }
        return mCollabs;
    }

    public LiveData<HashMap<String, String>> getUserProfileImages(List<String> ids) {
        HashMap<String, String> currentStore = userProfileImages.getValue();
        for (String id : ids) {
            if (!currentStore.containsKey(id)) {
                getUserProfileImage(id);
            }
        }

        return userProfileImages;
    }

    private void getUserProfileImage(String id) {
        FirebaseDatabase.getInstance().getReference(FirebaseHelper.USERS_NODE)
                .child(id)
                .child(FirebaseHelper.USERS_PROFILE_IMAGE)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String url;
                        url = dataSnapshot.getValue(String.class);
//                        Log.d(TAG, "onDataChange: adding profile image for " + id + " to store");
                        HashMap<String, String> tempHolder = userProfileImages.getValue();
                        tempHolder.put(id,url);
                        userProfileImages.setValue(tempHolder);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        Log.d(TAG, "onCancelled: getting user image");
                    }
                });
    }

    public void insertTask(Task task) {taskRepository.insert(task);}

    public void delete(Task task) {
        taskRepository.delete(task);
    }

    @Override
    protected void onCleared() {
        disposable.clear();
        super.onCleared();
    }
}


















