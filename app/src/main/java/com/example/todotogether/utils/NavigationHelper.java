package com.example.todotogether.utils;

import android.content.Context;
import android.content.Intent;

import com.example.todotogether.R;
import com.example.todotogether.views.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationHelper {
    public static void setUpNavigation(Context context, BottomNavigationView view) {
        ((MainActivity)context).getSupportFragmentManager().beginTransaction();
        switch (view.getId()) {
            case R.id.optionProfile:
                break;
            case R.id.optionHome:
                ((MainActivity)context).getSupportFragmentManager().beginTransaction();
                break;
            case R.id.optionSocial:
                break;
            default:
                break;

        }

    }
}
