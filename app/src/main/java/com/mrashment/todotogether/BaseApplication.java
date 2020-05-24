package com.mrashment.todotogether;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        super.onCreate();
    }
}
