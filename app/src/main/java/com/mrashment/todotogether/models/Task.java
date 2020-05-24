package com.mrashment.todotogether.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A user defined task that can be displayed and shared with other users.
 */
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
                Objects.equals(author, task.author) &&
                Objects.equals(team.size(),task.getTeam().size());
    }

    @Override
    public int hashCode() {
        return Objects.hash(task_id, name, description, author);
    }
}
