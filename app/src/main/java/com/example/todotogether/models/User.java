package com.example.todotogether.models;

import java.util.Objects;

/**
 * A representation of a user of this app.
 */
public class User {

    private String uid;
    private String name;
    private String email;
    private String profImage;

    public User() {}

    public User(String uid, String name, String email, String profImage) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.profImage = profImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfImage() {
        return profImage;
    }

    public void setProfImage(String profImage) {
        this.profImage = profImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return uid.equals(user.uid) &&
                Objects.equals(name, user.name) &&
                email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, name, email);
    }
}
