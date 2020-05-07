package com.example.todotogether.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todotogether.R;
import com.example.todotogether.models.Task;
import com.jakewharton.rxbinding3.view.RxView;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import kotlin.Unit;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {
    private static final String TAG = "TaskAdapter";

    private List<Task> mTasks;
    private OnTaskListener onTaskListener;
    private CompositeDisposable disposable;

    public TaskAdapter(List<Task> tasks,OnTaskListener onTaskListener) {
        this.mTasks = tasks;
        this.onTaskListener = onTaskListener;
        disposable = new CompositeDisposable();
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
        holder.tvDescription.setText(mTasks.get(position).getDescription());
        holder.checkBox.setChecked(mTasks.get(position).isDelete());

        disposable.add(RxView.clicks(holder.checkBox)
                .doOnEach(new Consumer<Notification<Unit>>() {
                    @Override
                    public void accept(Notification<Unit> unitNotification) throws Exception {
                        mTasks.get(position).setDelete(holder.checkBox.isChecked());
                    }
                })
                .debounce(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Unit>() {
                    @Override
                    public void accept(Unit unit) throws Exception {
//                        if (holder.checkBox.isChecked()) {
                        Log.d(TAG, "accept: emitting" + mTasks.get(position).getName());
                            onTaskListener.onCheckBoxClicked(position);
//                        }
                    }
                }));
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull TaskHolder holder) {
        disposable.clear();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.mTasks = tasks;
        notifyDataSetChanged();
    }

    class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvName,tvDescription;
        private OnTaskListener onTaskListener;
        private Observable<Unit> observable;
        private CheckBox checkBox;

        public TaskHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            checkBox = itemView.findViewById(R.id.checkbox);

            this.onTaskListener = onTaskListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onTaskListener.onTaskClick(this.getAdapterPosition());
            Log.d(TAG, "onClick: " + this.getAdapterPosition());
        }
    }

    public interface OnTaskListener {
        void onTaskClick(int position);
        void onCheckBoxClicked(int position);
    }
}
