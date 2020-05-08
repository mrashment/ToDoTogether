package com.example.todotogether.views;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.todotogether.R;
import com.example.todotogether.adapters.TaskAdapter;
import com.example.todotogether.models.Task;
import com.example.todotogether.viewmodels.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.rxbinding3.view.RxView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;

import static android.app.Activity.RESULT_OK;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskListener {
    private static final String TAG = "TaskListFragment";

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private TaskViewModel mTaskViewModel;
    private Flowable<List<Task>> mTasksFlowable;
    private ArrayList<Task> mTasks;
    private TaskAdapter adapter;
    private CompositeDisposable disposable;
    public static final int INSERT_TASK_REQUEST = 1;

    // listener for fab
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.fab:
                    // add a new task
                    Intent intent = new Intent(getActivity(),InsertTaskActivity.class);
                    intent.putExtra("requestCode",INSERT_TASK_REQUEST);
                    startActivityForResult(intent,INSERT_TASK_REQUEST);
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == INSERT_TASK_REQUEST && resultCode == RESULT_OK) {
            String name = data.getStringExtra(InsertTaskActivity.EXTRA_NAME);
            String description = data.getStringExtra(InsertTaskActivity.EXTRA_DESCRIPTION);
            String author = data.getStringExtra(InsertTaskActivity.EXTRA_AUTHOR);
            mTaskViewModel.insertTask(new Task(name,description,author));

            Toast.makeText(getActivity(),"Task added",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),"Task not added",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTasks = new ArrayList<>();
        disposable = new CompositeDisposable();

        mTaskViewModel = ((MainActivity)getActivity()).getTaskViewModel();

        mTasksFlowable = mTaskViewModel.getTasks();

        disposable.add(mTasksFlowable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Task>>() {
                    @Override
                    public void accept(List<Task> tasks) throws Exception {
                        Log.d(TAG, "accept: updating list");
                        // copy the incoming tasks
                        List<Task> copy = new ArrayList<>(tasks);
                        // create a list with only the new or updated tasks
                        copy.removeAll(mTasks);
                        List<Task> addsOrUpdates = new ArrayList<Task>(copy);
                        // keep all the original copies of duplicates
                        mTasks.retainAll(tasks);
                        // add the new ones
                        mTasks.addAll(addsOrUpdates);
                        // send to adapter
                        adapter.setTasks(mTasks);
                    }
                }));
    }

    public void applyChanges(List<Task> list) {
        Set<Task> set = new HashSet<>();
//        set.add
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(listener);
        setupRecyclerView(view);
    }

    public void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new TaskAdapter(mTasks,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }

    // Go to task details
    @Override
    public void onTaskClick(int position) {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        TaskDetailsFragment f = new TaskDetailsFragment();
        Bundle b = new Bundle();
        b.putSerializable("task",mTasks.get(position));
        f.setArguments(b);

        ft.replace(R.id.midRelativeLayout,f)
                .addToBackStack("TaskListFragment")
                .commit();

    }

    @Override
    public void onCheckBoxesClicked(List<Task> tasksToDelete) {
        Log.d(TAG, "onCheckBoxesClicked: deleting task list with size =" + tasksToDelete.size());
        mTaskViewModel.deleteSome(tasksToDelete);
    }
}
