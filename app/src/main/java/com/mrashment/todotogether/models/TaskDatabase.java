package com.mrashment.todotogether.models;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mrashment.todotogether.utils.Converters;

/**
 * Room database for holding user defined tasks.
 */
@Database(entities = Task.class, version = 4)
@TypeConverters({Converters.class})
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
        }
    };


}
