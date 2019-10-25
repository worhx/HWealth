package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;

import java.util.Objects;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final String STEP_URL = "https://hwealth.herokuapp.com/api/steps-record";
    private static final String SHAREDPREF = "SHAREDPREF";
    private RequestQueue mQueue;
    private SharedPreferences prefs;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_profile,container,false);
        ImageButton setting = view.findViewById(R.id.setting);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }
}
