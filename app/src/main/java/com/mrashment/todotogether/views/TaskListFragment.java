package com.mrashment.todotogether.views;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mrashment.todotogether.R;
import com.mrashment.todotogether.adapters.TaskAdapter;
import com.mrashment.todotogether.models.Task;
import com.mrashment.todotogether.viewmodels.TaskViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.disposables.CompositeDisposable;

import static android.app.Activity.RESULT_OK;

/**
 * The 'home' fragment which displays all the tasks authored by the current user.
 */
public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskListener {
    private static final String TAG = "TaskListFragment";

    protected RecyclerView recyclerView;
    protected FloatingActionButton fab;
    private TaskViewModel mTaskViewModel;
    private Flowable<List<Task>> mTasksFlowable;
    protected ArrayList<Task> mTasks;
    protected TaskAdapter adapter;
    protected CompositeDisposable disposable;
    public static final int INSERT_TASK_REQUEST = 1;

    // listener for fab
    protected View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.fab:
                    // add a new task
                    Intent intent = new Intent(getActivity(),NewCollabActivity.class);
                    intent.putExtra("requestCode",INSERT_TASK_REQUEST);
                    startActivityForResult(intent,INSERT_TASK_REQUEST);
                default:
                    break;
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTasks = new ArrayList<>();

        setUpObserver();
    }

    protected void setUpObserver() {
        disposable = new CompositeDisposable();

        mTaskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        mTasksFlowable = mTaskViewModel.getTasks();

        disposable.add(mTasksFlowable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Task>>() {
                    @Override
                    public void accept(List<Task> tasks) {
                        Log.d(TAG, "accept: updating list");
                        parseDifferences(tasks,mTasks);
                        // send to adapter
                        adapter.setTasks(mTasks);
                    }
                }));
    }

    public void parseDifferences(List<Task> current, List<Task> previous) {
        // copy the incoming tasks
        List<Task> copy = new ArrayList<>(current);
        // create a list with only the new or updated tasks
        copy.removeAll(previous);
        List<Task> addsOrUpdates = new ArrayList<Task>(copy);
        // keep all the original copies of duplicates
        previous.retainAll(current);
        // add the new ones
        previous.addAll(addsOrUpdates);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == INSERT_TASK_REQUEST && resultCode == RESULT_OK) {
            String name = data.getStringExtra(InsertTaskActivity.EXTRA_NAME);
            String description = data.getStringExtra(InsertTaskActivity.EXTRA_DESCRIPTION);
            String author = data.getStringExtra(InsertTaskActivity.EXTRA_AUTHOR);
            ArrayList<String> userIds = data.getStringArrayListExtra(NewCollabActivity.EXTRA_IDS);
            mTaskViewModel.insertTask(new Task(null,name,description,author,null, userIds),userIds);

            Toast.makeText(getActivity(),"Task added",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),"Task not added",Toast.LENGTH_SHORT).show();
        }
    }

    public void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewTasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        adapter = new TaskAdapter(mTasks,this, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }


    @Override
    public void onResume() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        disposable.clear();
        super.onDestroy();
    }

    //-----------------------------------TaskAdapter.OnTaskListener--------------------------------
    // Go to task details
    @Override
    public void onTaskClick(int position) {
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        TaskDetailsFragment f = new TaskDetailsFragment();
        Bundle b = new Bundle();
        b.putSerializable("task",mTasks.get(position));
        f.setArguments(b);

        ft.replace(R.id.midRelativeLayout,f,MainActivity.TASK_DETAILS_FRAGMENT)
                .addToBackStack(MainActivity.TASK_LIST_FRAGMENT)
                .commit();

    }

    @Override
    public void onCheckBoxesClicked(List<Task> tasksToDelete) {
        Log.d(TAG, "onCheckBoxesClicked: deleting task list with size =" + tasksToDelete.size());
        mTaskViewModel.deleteSome(tasksToDelete);
    }
}
