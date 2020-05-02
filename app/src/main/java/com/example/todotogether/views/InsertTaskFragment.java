package com.example.todotogether.views;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.todotogether.R;
import com.example.todotogether.models.Task;
import com.example.todotogether.viewmodels.TaskViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class InsertTaskFragment extends Fragment {

    private TextInputEditText etName,etDescription;
    private Button btnSubmit;
    private TaskViewModel mTaskViewModel;

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = etName.getText().toString();
            String description = etDescription.getText().toString();
            mTaskViewModel.insertTask(new Task(name,description,"Mason"));
//            getFragmentManager().beginTransaction().replace(R.id.midRelativeLayout,new TaskListFragment()).commit();
            getFragmentManager().popBackStack();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskViewModel = new TaskViewModel(getActivity().getApplication());
        mTaskViewModel.init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insert_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etName = view.findViewById(R.id.etName);
        etDescription = view.findViewById(R.id.etDescription);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(listener);
    }
}
