package com.example.todotogether.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "task_table")
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int task_id;

    private String name;

    private String description;

    private String author;

    @Ignore
    private boolean delete;

    public Task(String name, String description, String author) {
        this.name = name;
        this.description = description;
        this.author = author;
        this.delete = false;
    }

    @Ignore
    public Task(int task_id,String name, String description, String author) {
        this.task_id = task_id;
        this.name = name;
        this.description = description;
        this.author = author;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public int getTask_id() {
        return task_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }
}
