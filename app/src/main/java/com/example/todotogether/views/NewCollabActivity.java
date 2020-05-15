package com.example.todotogether.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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

    private TextView tvEmailHolder;
    private ImageButton ibClearCollabs;
    private SearchView svCollaborators;
    private RecyclerView recyclerUsers;
    private UserAdapter adapter;
    private List<User> mUsers;
    private Set<User> potentialCollabs;
    private CompositeDisposable disposable;
    private FirebaseDatabase fbDatabase;
    private PublishSubject<String> querySubject;

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
                                    User cur = d.getValue(User.class);
                                    if (cur != null && !cur.getEmail().equals(mAuth.getCurrentUser().getEmail())) {
                                        mUsers.add(cur);
                                    }
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

    @Override
    public void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        tvEmailHolder = findViewById(R.id.etEmailHolder);
        ibClearCollabs = findViewById(R.id.ibClearCollabs);
        ibClearCollabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEmailHolder.setText("");
                potentialCollabs.clear();
                ibClearCollabs.setVisibility(View.GONE);
                tvEmailHolder.setVisibility(View.GONE);
            }
        });
        svCollaborators = findViewById(R.id.svCollaborators);
        if (mAuth.getCurrentUser() == null) {
            svCollaborators.setVisibility(View.GONE);
        } else {
            svCollaborators.setVisibility(View.VISIBLE);
        }
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

        try {
            Intent data = new Intent();

            // these shouldn't change
            if (requestCode == TaskDetailsFragment.UPDATE_TASK_REQUEST) {
                data.putExtra(EXTRA_ID, task.getTask_id());
                data.putExtra(EXTRA_AUTHOR, task.getAuthor());
                data.putExtra(EXTRA_KEY, task.getKey());
            } else {
                // if the user isn't signed in, the sign in process will add their id to the task later
                data.putExtra(EXTRA_AUTHOR,mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid(): null);
            }

            //these might change
            ArrayList<String> collaboratorIds = parseUserIds();
            data.putStringArrayListExtra(EXTRA_IDS, collaboratorIds);
            data.putExtra(EXTRA_NAME, name);
            data.putExtra(EXTRA_DESCRIPTION, description);

            setResult(RESULT_OK, data);
        } catch (NullPointerException e) {
            Log.d(TAG, "saveTask: error" + e.getMessage());
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    // parses a string of emails separated by commas
    public ArrayList<String> parseUserIds() {
        ArrayList<String> ids = new ArrayList<>();
        for (User u : potentialCollabs) {
            ids.add(u.getUid());
        }
        return ids;
    }

    @Override
    protected void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }

    /**
     * When a user is chosen from the dropdown list of the search
     * @param user
     */
    @Override
    public void onUserClick(User user) {
        tvEmailHolder.setVisibility(View.VISIBLE);
        ibClearCollabs.setVisibility(View.VISIBLE);
        if (!potentialCollabs.contains(user)) {
            potentialCollabs.add(user);
            tvEmailHolder.append(user.getEmail() + ", ");
        }
    }
}
