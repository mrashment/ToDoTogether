package com.example.todotogether.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.todotogether.R;
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
        setUpToolbar();
    }

    public void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
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
    public void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.optionSaveTask:
                        submitNewTask();
                        break;
                    case R.id.optionClose:
                        setResult(RESULT_CANCELED);
                        finish();
                        break; //
                    default:
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_insert_task, menu);
        return true;
    }
}
