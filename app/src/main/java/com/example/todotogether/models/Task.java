package com.example.todotogether.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "task_table")
public class Task implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Integer task_id;

    private String name;

    private String description;

    private String author;

    private String key;

    private ArrayList<String> team = new ArrayList<>();

    @Ignore
    private boolean delete;

    @Ignore
    public Task() {}

//    @Ignore
//    public Task(Integer task_id, String name, @Nullable String description, @Nullable String author) {
//        this.task_id = task_id;
//        this.name = name;
//        this.description = description;
//        this.author = author;
//        this.delete = false;
//    }

    public Task(Integer task_id, String name, String description, String author, String key, ArrayList<String> team) {
        this.task_id = task_id;
        this.name = name;
        this.description = description;
        this.author = author;
        this.key = key;
        this.delete = false;
        this.team.addAll(team);
    }

    public ArrayList<String> getTeam() {
        return team;
    }

    public void setTeam(ArrayList<String> team) {
        this.team = team;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getTask_id() {
        return task_id;
    }

    public void setTask_id(Integer task_id) {
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
        return task_id ==task.task_id &&
                name.equals(task.name) &&
                Objects.equals(key,task.key) &&
                Objects.equals(description, task.description) &&
                Objects.equals(author, task.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(task_id, name, description, author);
    }
}
