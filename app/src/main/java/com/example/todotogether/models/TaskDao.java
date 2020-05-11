package com.example.todotogether.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface TaskDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(Task task);

    @Update
    Completable update(Task task);

    @Delete
    Completable delete(Task task);

    @Delete
    Completable deleteSome(List<Task> tasks);

    @Query("DELETE FROM task_table")
    Completable deleteAllTasks();

    @Query("SELECT * FROM task_table")
    Flowable<List<Task>> getAllTasks();

}
