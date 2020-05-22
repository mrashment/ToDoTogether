package com.example.todotogether.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.todotogether.R;
import com.example.todotogether.models.Task;
import com.example.todotogether.utils.Converters;
import com.example.todotogether.viewmodels.CollabViewModel;
import com.example.todotogether.viewmodels.TaskViewModel;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class TaskDetailsFragment extends Fragment {
    private static final String TAG = "TaskDetailsFragment";
    public static final int UPDATE_TASK_REQUEST = 2;
    private TaskViewModel mTaskViewModel;
    private CollabViewModel mCollabViewModel;
    private TextView tvName,tvDescription,tvCollabStatic;
    private LinearLayout llCollaborators;
    private LiveData<HashMap<String, String>> images;
    private Toolbar toolbar;
    private Task task;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTaskViewModel = new ViewModelProvider(getActivity()).get(TaskViewModel.class);
        mCollabViewModel = new ViewModelProvider(getActivity()).get(CollabViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_details,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        toolbar = getActivity().findViewById(R.id.toolbarMain);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        task = (Task)getArguments().getSerializable("task");
        tvName.setText(task.getName());
        tvDescription.setText(task.getDescription());

        // display the collaborators for this task
        images = mCollabViewModel.getUserProfileImages(task.getTeam());
        images.observe(getViewLifecycleOwner(), new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringStringHashMap) {
                llCollaborators.removeAllViews();
                for (String id : task.getTeam()) {
                    if (stringStringHashMap.containsKey(id)) {
                        ImageView image = new ImageView(getContext());
                        Glide.with(TaskDetailsFragment.this).load(stringStringHashMap.get(id)).circleCrop().into(image);
                        Log.d(TAG, "onChanged: adding image to collaborators linear layout");
                        llCollaborators.addView(image, 90, 90);
                    }
                }
            }
        });
    }

    public void initViews(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvDescription = view.findViewById(R.id.tvDescription);
        llCollaborators = view.findViewById(R.id.llCollaborators);
    }

    @Override
    public void onStop() {
        toolbar.setNavigationIcon(null);
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_TASK_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(InsertTaskActivity.EXTRA_ID,-1);
            String name = data.getStringExtra(InsertTaskActivity.EXTRA_NAME);
            String description = data.getStringExtra(InsertTaskActivity.EXTRA_DESCRIPTION);
            String author = data.getStringExtra(InsertTaskActivity.EXTRA_AUTHOR);
            String key = data.getStringExtra(InsertTaskActivity.EXTRA_KEY);
            ArrayList<String> team = data.getStringArrayListExtra(NewCollabActivity.EXTRA_IDS);
            mTaskViewModel.updateTask(new Task(id,name,description,author,key, team));

            Toast.makeText(getActivity(),"Task updated",Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        } else {
            Toast.makeText(getActivity(),"Task not updated",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_task_details,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case R.id.optionDelete:
                Log.d(TAG, "onOptionsItemSelected: Delete option selected");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.are_you_sure)
                .setMessage("This will delete the task permanently.")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTaskViewModel.deleteTask(task);
                        getParentFragmentManager().popBackStack();
                    }
                });
                builder.setNegativeButton(R.string.cancel,null);
                builder.create().show();
                return true;
            case R.id.optionEdit:
                Intent intent = new Intent(getActivity(),NewCollabActivity.class);
                intent.putExtra("task",task);
                intent.putExtra("requestCode",UPDATE_TASK_REQUEST);
                startActivityForResult(intent,UPDATE_TASK_REQUEST);
                return true;
            default:
                break;
        }
        return false;
    }
}
