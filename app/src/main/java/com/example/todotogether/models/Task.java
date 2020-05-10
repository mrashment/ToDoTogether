package com.example.todotogether.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

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
    public Task(int task_id, String name, @Nullable String description, @Nullable String author) {
        this.task_id = task_id;
        this.name = name;
        this.description = description;
        this.author = author;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return task_id == task.task_id &&
                name.equals(task.name) &&
                Objects.equals(description, task.description) &&
                Objects.equals(author, task.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task_id, name, description, author);
    }
}
