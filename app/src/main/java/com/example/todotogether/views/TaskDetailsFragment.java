package com.example.todotogether.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.todotogether.R;
import com.example.todotogether.models.Task;
import com.example.todotogether.viewmodels.TaskViewModel;

public class TaskDetailsFragment extends Fragment {

    private TaskViewModel mTaskViewModel;
    private TextView tvName,tvDescription;
    private Button btnDelete, btnEdit;
    private Task task;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
