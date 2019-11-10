package com.team09.hwealth;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class TwoFAActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_fa);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new TwoFAFragment()).commit();
    }

//
}
