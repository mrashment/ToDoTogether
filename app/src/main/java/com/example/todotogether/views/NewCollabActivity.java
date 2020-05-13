package com.example.todotogether.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todotogether.R;
import com.example.todotogether.adapters.UserAdapter;
import com.example.todotogether.models.User;
import com.example.todotogether.utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class NewCollabActivity extends InsertTaskActivity implements UserAdapter.OnUserClickListener {
    private static final String TAG = "NewCollabActivity";

    private TextInputEditText etName,etDescription;
    private EditText etEmailHolder;
    private SearchView svCollaborators;
    private RecyclerView recyclerUsers;
    private UserAdapter adapter;
    private List<User> mUsers;
    private Set<User> potentialCollabs;
    private CompositeDisposable disposable;
    private FirebaseDatabase fbDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        contentView = R.layout.activity_new_collab;
        mUsers = new ArrayList<>();
        potentialCollabs = new HashSet<>();
        super.onCreate(savedInstanceState);

        disposable = new CompositeDisposable();
        fbDatabase = FirebaseDatabase.getInstance();

        querySubject = PublishSubject.create();
        querySubject.subscribeOn(Schedulers.io())
                .debounce(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(String s) {
                        // query the database and find the users with matching emails
                        Query query = fbDatabase.getReference()
                                .child(FirebaseHelper.USERS_NODE)
                                .orderByChild("email")
                                .startAt(s)
                                .limitToFirst(5);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                mUsers = new ArrayList<>();
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    Log.d(TAG, "onDataChange: " + d.getValue(User.class).getEmail() );
                                    mUsers.add(d.getValue(User.class));
                                }
                                Log.d(TAG, "onDataChange: mUsers size: " + mUsers.size());
                                adapter.setmUsers(mUsers);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: ");
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: querySubject");
                    }
                });
    }

    private PublishSubject<String> querySubject;

    @Override
    public void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etEmailHolder = findViewById(R.id.etEmailHolder);
        svCollaborators = findViewById(R.id.svCollaborators);
        svCollaborators.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                querySubject.onNext(newText);
                return true;
            }
        });
        setUpRecycler();
    }

    public void setUpRecycler() {
        recyclerUsers = findViewById(R.id.recyclerUsers);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        adapter = new UserAdapter(mUsers,this);
        recyclerUsers.setAdapter(adapter);
    }

    @Override
    public void saveTask() {
        String name = etName.getText().toString();
        String description = etDescription.getText().toString();

        if (name.trim().isEmpty()) {
            Toast.makeText(this,"Please enter a name",Toast.LENGTH_LONG).show();
            return;
        }
        if (description.trim().isEmpty()) description = null;

        Intent data = new Intent();
        if (requestCode == TaskDetailsFragment.UPDATE_TASK_REQUEST) {
            data.putExtra(EXTRA_ID,task.getTask_id());
            data.putExtra(EXTRA_AUTHOR,task.getAuthor());
            data.putExtra(EXTRA_KEY,task.getKey());
        } else {
            data.putExtra(EXTRA_AUTHOR,mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid(): null);
        }
        List<String> collaboratorIds = parseUserIds(etEmailHolder.getText().toString());

        data.putExtra(EXTRA_NAME,name);
        data.putExtra(EXTRA_DESCRIPTION,description);

        setResult(RESULT_OK,data);
        finish();
    }

    // parses a string of emails separated by commas
    public List<String> parseUserIds(String users) {
        List<String> emails = Arrays.asList(users.trim().replace(" ","").split(","));
        List<String> ids = new ArrayList<>();
        for (User u : potentialCollabs) {
            if (u.getEmail().contains())
        }
    }

    @Override
    protected void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }

    @Override
    public void onUserClick(User user) {
        etEmailHolder.setVisibility(View.VISIBLE);
        etEmailHolder.append(user.getEmail() + ",");
        potentialCollabs.add(user);
    }
}
