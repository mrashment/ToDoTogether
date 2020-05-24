package com.mrashment.todotogether.models;

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

/**
 * Data Access Object for reading and writing tasks in the Room database.
 */
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
