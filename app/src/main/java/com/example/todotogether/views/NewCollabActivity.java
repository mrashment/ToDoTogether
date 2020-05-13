package com.example.todotogether.views;

import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todotogether.R;
import com.example.todotogether.adapters.UserAdapter;
import com.example.todotogether.models.User;
import com.example.todotogether.utils.FirebaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class NewCollabActivity extends InsertTaskActivity {
    private static final String TAG = "NewCollabActivity";

    private TextInputEditText etName,etDescription;
    private SearchView svCollaborators;
    private RecyclerView recyclerUsers;
    private UserAdapter adapter;
    private List<User> mUsers;
    private CompositeDisposable disposable;
    private FirebaseDatabase fbDatabase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        contentView = R.layout.activity_new_collab;
        mUsers = new ArrayList<>();
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
                        Query query = fbDatabase.getReference()
                                .child(FirebaseHelper.USERS_NODE)
                                .orderByChild("email")
                                .limitToFirst(5).startAt("ma");
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
        adapter = new UserAdapter(mUsers);
        recyclerUsers.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }
}
