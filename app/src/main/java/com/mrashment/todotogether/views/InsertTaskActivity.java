package com.mrashment.todotogether.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mrashment.todotogether.R;
import com.mrashment.todotogether.models.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Originally used to add tasks, but doesn't provide a way to add collaborators. Acts as the parent
 * of NewCollabActivity
 */
public class InsertTaskActivity extends AppCompatActivity {

    private static final String TAG = "InsertTaskActivity";
    protected TextInputEditText etName,etDescription;
    protected int requestCode;
    protected Task task;
    protected FirebaseAuth mAuth;
    protected int contentView = R.layout.activity_insert_task;
    public static final String EXTRA_ID = "com.example.todotogether.ID";
    public static final String EXTRA_NAME = "com.example.todotogether.NAME";
    public static final String EXTRA_DESCRIPTION = "com.example.todotogether.DESCRIPTION";
    public static final String EXTRA_AUTHOR = "com.example.todotogether.AUTHOR";
    public static final String EXTRA_KEY = "com.example.todotogether.KEY";
    public static final String EXTRA_IDS = "com.example.todotogether.IDS";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(contentView);

        mAuth = FirebaseAuth.getInstance();
        initViews();

        // The user is actually editing a task
        requestCode = getIntent().getIntExtra("requestCode",0);
        if (requestCode == TaskDetailsFragment.UPDATE_TASK_REQUEST) {
            task = (Task)getIntent().getSerializableExtra("task");
            populateFields();
        }

        setUpToolbar();
    }

    public void initViews() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
    }

    public void populateFields() {
        etName.setText(task.getName());
        etDescription.setText(task.getDescription());
    }


    public void saveTask() {
        String name = etName.getText().toString();
        String description = etDescription.getText().toString();

        if (name.trim().isEmpty()) {
            Toast.makeText(this,"Please enter a name",Toast.LENGTH_LONG).show();
            return;
        }
        if (description.trim().isEmpty()) description = null;

        try {
            Intent data = new Intent();
            if (requestCode == TaskDetailsFragment.UPDATE_TASK_REQUEST) {
                data.putExtra(EXTRA_ID,task.getTask_id());
                data.putExtra(EXTRA_AUTHOR,task.getAuthor());
                data.putExtra(EXTRA_KEY,task.getKey());
            } else {
                data.putExtra(EXTRA_AUTHOR,mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid(): null); // maybe replace with Uid so I can query database
            }
            data.putExtra(EXTRA_NAME,name);
            data.putExtra(EXTRA_DESCRIPTION,description);

            setResult(RESULT_OK,data);
        } catch (NullPointerException e) {
            //Log.d(TAG, "saveTask: error" + e.getMessage());
            setResult(RESULT_CANCELED);
        }
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
                        saveTask();
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
