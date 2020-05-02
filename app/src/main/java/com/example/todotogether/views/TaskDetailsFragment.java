package com.example.todotogether.views;

import android.app.Dialog;
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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.todotogether.R;
import com.example.todotogether.models.Task;
import com.example.todotogether.viewmodels.TaskViewModel;

public class TaskDetailsFragment extends Fragment {
    private static final String TAG = "TaskDetailsFragment";
    private TaskViewModel mTaskViewModel;
    private TextView tvName,tvDescription;
    private Button btnDelete, btnEdit;
    private Task task;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mTaskViewModel = new TaskViewModel(getActivity().getApplication());
        mTaskViewModel.init();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_details,container,false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initViews(view);
        task = (Task)getArguments().getSerializable("task");
        tvName.setText(task.getName());
        tvDescription.setText(task.getDescription());
    }

    public void initViews(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvDescription = view.findViewById(R.id.tvDescription);
        btnDelete = view.findViewById(R.id.btnDelete);
        btnEdit = view.findViewById(R.id.btnEdit);
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
                builder.setTitle("Are you sure?")
                .setMessage("This will delete the task permanently.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTaskViewModel.deleteTask(task);
                        getFragmentManager().popBackStack();
                    }
                });
                builder.setNegativeButton("Cancel",null);
                builder.create().show();
            case R.id.optionEdit:
//                Intent intent = new Intent(getActivity(),InsertTaskActivity.class);
//                intent.putExtra("task",task);
//                startActivityForResult(intent,);
            default:
                break;
        }
        return false;
    }
}
