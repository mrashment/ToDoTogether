package com.example.todotogether.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todotogether.R;
import com.example.todotogether.models.Task;
import com.example.todotogether.viewmodels.TaskViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class InsertTaskActivity extends AppCompatActivity {


    private TextInputEditText etName,etDescription;
    private Button btnSubmit;
    public static final String EXTRA_NAME = "com.example.todotogether.NAME";
    public static final String EXTRA_DESCRIPTION = "com.example.todotogether.DESCRIPTION";
    public static final String EXTRA_AUTHOR = "com.example.todotogether.AUTHOR";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_task);

        initViews();
    }

    public void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitNewTask();
            }
        });
    }

    public void submitNewTask() {
        String name = etName.getText().toString();
        String description = etDescription.getText().toString();

        if (name.trim().isEmpty() || description.trim().isEmpty()) {
            Toast.makeText(this,"Please fill in all fields",Toast.LENGTH_LONG).show();
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_NAME,name);
        data.putExtra(EXTRA_DESCRIPTION,description);
        data.putExtra(EXTRA_AUTHOR,"Mason");

        setResult(RESULT_OK,data);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
