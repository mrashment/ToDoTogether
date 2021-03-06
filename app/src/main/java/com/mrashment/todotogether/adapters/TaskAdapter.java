package com.mrashment.todotogether.adapters;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrashment.todotogether.R;
import com.mrashment.todotogether.models.Task;
import com.mrashment.todotogether.utils.FirebaseHelper;
import com.mrashment.todotogether.viewmodels.CollabViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.rxbinding3.view.RxView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
    private CollabViewModel mCollabViewModel;
    private Context context;
    private FirebaseAuth mAuth;
    private PublishSubject<Unit> mAccumulator = PublishSubject.create();
    public static final int MARGIN_FOR_USER_IMAGES = 10;

    public TaskAdapter(List<Task> tasks,OnTaskListener onTaskListener, Context context) {
        this.context = context;
        this.mTasks = tasks;
        this.mAuth = FirebaseAuth.getInstance();
        this.onTaskListener = onTaskListener;
        this.mCollabViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(CollabViewModel.class);
        disposable = new CompositeDisposable();
        disposable.add(mAccumulator.subscribeOn(Schedulers.io())
                .debounce(2000,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unit -> {
                    // once the user has stopped clicking on checkboxes, get all tasks that have been set delete
                    onTaskListener.onCheckBoxesClicked(getTasksToDelete());
                }));
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_cardview,parent,false);
        return new TaskHolder(v, onTaskListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        holder.tvName.setText(mTasks.get(position).getName());
        if (mTasks.get(holder.getAdapterPosition()).getDescription() == null) {
//            Log.d(TAG, "onBindViewHolder: task " + mTasks.get(position).getName() + " description is null, setting gone");
            holder.tvDescription.setVisibility(View.GONE);
        }
        else {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(mTasks.get(position).getDescription());
//            Log.d(TAG, "onBindViewHolder: task " + mTasks.get(position).getName() + " description is not null, setting description");
        }
        holder.checkBox.setChecked(mTasks.get(position).isDelete());

        Context mContext = holder.collabLayout.getContext();

        // sets user image(s) on task
        FirebaseUser user = mAuth.getCurrentUser();
//        Log.d(TAG, "onBindViewHolder: " + mTasks.get(position).getName());
        if (user != null) {
            String authorid = mTasks.get(position).getAuthor();
            ImageView authorImage = new ImageView(mContext);
            if (authorid.equals(user.getUid())) {
                Glide.with(mContext).load(user.getPhotoUrl()).circleCrop().into(authorImage);
                holder.collabLayout.addView(authorImage,-1,new RelativeLayout.LayoutParams(70,70));
            }
            else {
//                Log.d(TAG, "onBindViewHolder: authorid: " + authorid);
                FirebaseDatabase.getInstance().getReference(FirebaseHelper.USERS_NODE)
                        .child(authorid)
                        .child(FirebaseHelper.USERS_PROFILE_IMAGE)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                Log.d(TAG, "onDataChange: authorid: " + authorid);
                                String uri = dataSnapshot.getValue(String.class);
                                Glide.with(mContext).load(Uri.parse(uri)).circleCrop().into(authorImage);
                                holder.collabLayout.addView(authorImage,-1,new RelativeLayout.LayoutParams(70,70));

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                                Log.d(TAG, "onCancelled: Failed to load author image");
                            }
                        });
            }

            // layer other collaborator images behind the author
            
            LiveData<HashMap<String,String>> profileImages = mCollabViewModel.getUserProfileImages(mTasks.get(position).getTeam());
            profileImages.observe((LifecycleOwner) context,new Observer<HashMap<String,String>>() {
                @Override
                public void onChanged(HashMap<String, String> stringStringHashMap) {
                    int margin = MARGIN_FOR_USER_IMAGES;
                    if (position >= mTasks.size()) {return;}
                    for (String id : mTasks.get(position).getTeam()) {
                        if (stringStringHashMap.containsKey(id)) {
//                            Log.d(TAG, "onChanged: attempting to load image into collabLayout");
                            ImageView collaboratorImage = new ImageView(mContext);
                            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(70,70);
                            lp.setMarginStart(margin);
                            if (margin < 70) {
                                margin += MARGIN_FOR_USER_IMAGES;
                            }
                            Glide.with(holder.collabLayout.getContext()).load(stringStringHashMap.get(id)).circleCrop().into(collaboratorImage);
                            holder.collabLayout.addView(collaboratorImage,0,lp);
                        }
                        
                    }

                }
            });

        }

        // responds to clicks on the checkboxes, switching a boolean in each associated task to match
        RxView.clicks(holder.checkBox)
                .doOnEach(unitNotification -> mTasks.get(position).setDelete(holder.checkBox.isChecked()))
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

    static class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName,tvDescription;
        private OnTaskListener onTaskListener;
        private CheckBox checkBox;
        private RelativeLayout collabLayout;

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
//            Log.d(TAG, "onClick: " + this.getAdapterPosition());
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
