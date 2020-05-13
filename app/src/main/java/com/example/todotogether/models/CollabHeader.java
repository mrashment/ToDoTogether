package com.example.todotogether.models;

public class CollabHeader {
    public String author;
    public Integer task_id;

    public CollabHeader() {}

    public CollabHeader(String author, Integer task_id) {
        this.author = author;
        this.task_id = task_id;
    }
}
