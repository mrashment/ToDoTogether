package com.example.todotogether.models;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Database(entities = Task.class, version = 1)
public abstract class TaskDatabase extends RoomDatabase {
    private static final String TAG = "TaskDatabase";

    private static TaskDatabase instance;

    public abstract TaskDao taskDao();

    public static synchronized TaskDatabase getInstance(Context context) {
        if (instance == null) {
            Log.d(TAG, "getInstance: database being built");
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    TaskDatabase.class, "task_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build(); // TODO set up migration strategy
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.d(TAG, "onCreate: callback executed");
            new PopulateDbAsync().execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            instance.taskDao().insert(new Task("test","testing callback","Mason"));
            instance.taskDao().insert(new Task("test2","testing callback","Mason"));
            Log.d(TAG, "doInBackground: inserted tasks on creation" + instance.taskDao().toString());
            return null;
        }
    }

}
