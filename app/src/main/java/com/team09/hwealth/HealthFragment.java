package com.team09.hwealth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import io.michaelrocks.paranoid.Obfuscate;

import java.util.Objects;
@Obfuscate
public class HealthFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_health, container, false);
        LinearLayout Steps = view.findViewById(R.id.Steps);
        LinearLayout Calories = view.findViewById(R.id.Calories);
        Steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
                ft.replace(R.id.fragment_container, new StepsFragment());
                ft.commit();
            }
        });
        Calories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
                ft.replace(R.id.fragment_container, new FoodFragment());
                ft.commit();
            }
        });
        return view;
    }


}
