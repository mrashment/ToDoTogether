package com.mrashment.todotogether.views;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mrashment.todotogether.R;


public class PrivacyPolicyFragment extends Fragment {
    private static final String TAG = "PrivacyPolicyFragment";
    private WebView webView;
    private Button btnOk;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_privacy_policy,container,false);
        btnOk = v.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(view -> {
            Log.d(TAG, "onViewCreated: ok clicked");
            getParentFragmentManager().popBackStackImmediate();
        });
        webView = v.findViewById(R.id.wvPrivacyPolicy);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        webView.loadUrl("https://github.com/mrashment/ToDoTogether/blob/master/privacypolicy.md");
    }
}
