package com.example.todotogether.models;

/**
 * POJO used for retrieving the location of a shared task object stored in Firebase.
 */
public class CollabHeader {
    public String author;
    public Integer task_id;

    public CollabHeader() {}

    public CollabHeader(String author, Integer task_id) {
        this.author = author;
        this.task_id = task_id;
    }
}
