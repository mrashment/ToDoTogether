package com.example.todotogether.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.todotogether.R;
import com.example.todotogether.models.Task;
import com.example.todotogether.utils.Filepaths;
import com.example.todotogether.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.jakewharton.rxbinding3.view.RxView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import kotlin.Unit;

/**
 * Adapter for the home screen of the application
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {
    private static final String TAG = "TaskAdapter";

    private List<Task> mTasks;
    private OnTaskListener onTaskListener;
    private CompositeDisposable disposable;
    private FirebaseAuth mAuth;
    private PublishSubject<Unit> mAccumulator = PublishSubject.create();

    public TaskAdapter(List<Task> tasks,OnTaskListener onTaskListener) {
        this.mTasks = tasks;
        this.mAuth = FirebaseAuth.getInstance();
        this.onTaskListener = onTaskListener;
        disposable = new CompositeDisposable();
        disposable.add(mAccumulator.subscribeOn(Schedulers.io())
                .debounce(2000,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Unit>() {
                    @Override
                    public void accept(Unit unit) throws Exception {
                        // once the user has stopped clicking on checkboxes, get all tasks that have been set delete
                        onTaskListener.onCheckBoxesClicked(getTasksToDelete());
                    }
                }));
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_cardview,parent,false);
        return new TaskHolder(v,onTaskListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        holder.tvName.setText(mTasks.get(position).getName());
        if (mTasks.get(position).getDescription() == null) {holder.tvDescription.setVisibility(View.GONE);}
        else {holder.tvDescription.setText(mTasks.get(position).getDescription());}
        holder.checkBox.setChecked(mTasks.get(position).isDelete());

        Context mContext = holder.collabLayout.getContext();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String authorid = mTasks.get(position).getAuthor();
            ImageView image = new ImageView(mContext);
            if (authorid.equals(user.getUid())) {
                Glide.with(mContext).load(user.getPhotoUrl()).circleCrop().into(image);
                holder.collabLayout.addView(image,70,70);
            }
            else {
                Log.d(TAG, "onBindViewHolder: authorid: " + authorid);
                FirebaseDatabase.getInstance().getReference(FirebaseHelper.USERS_NODE)
                        .child(authorid)
                        .child(FirebaseHelper.USERS_PROFILE_IMAGE)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Log.d(TAG, "onDataChange: authorid: " + authorid);
                                String uri = dataSnapshot.getValue(String.class);
                                Glide.with(mContext).load(Uri.parse(uri)).circleCrop().into(image);
                                holder.collabLayout.addView(image,70,70);

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.d(TAG, "onCancelled: Failed to load author image");
                            }
                        });
            }
        }

        // responds to clicks on the checkboxes, switching a boolean in each associated task to match
        RxView.clicks(holder.checkBox)
                .doOnEach(new Consumer<Notification<Unit>>() {
                    @Override
                    public void accept(Notification<Unit> unitNotification) throws Exception {
                        mTasks.get(position).setDelete(holder.checkBox.isChecked());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mAccumulator); // emit this to the publishsubscriber so I can debounce them until the user stops clicking
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.mTasks = tasks;
        notifyDataSetChanged();
    }

    private List<Task> getTasksToDelete() {
        List<Task> toDelete = new ArrayList<>();
        for (Task t : mTasks) {
            if (t.isDelete()) toDelete.add(t);
        }
        return toDelete;
    }

    class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName,tvDescription;
        private OnTaskListener onTaskListener;
        private CheckBox checkBox;
        private RelativeLayout collabLayout;
        private String authorid;

        public TaskHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            checkBox = itemView.findViewById(R.id.checkbox);
            collabLayout = itemView.findViewById(R.id.collaboratorsRelLayout);
            this.onTaskListener = onTaskListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onTaskListener.onTaskClick(this.getAdapterPosition());
            Log.d(TAG, "onClick: " + this.getAdapterPosition());
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        disposable.clear();
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public interface OnTaskListener {
        void onTaskClick(int position);
        void onCheckBoxesClicked(List<Task> toDelete);
    }
}
