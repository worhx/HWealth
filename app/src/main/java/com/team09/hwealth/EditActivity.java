package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EditActivity extends AppCompatActivity {
    private static final String TAG = "EditActivity";
    private static final String UPDATE_BMI_URL = "https://hwealth.herokuapp.com/api/profile/update-bmi";
    private static final String SHAREDPREF = "SHAREDPREF";
    private EditText bmi;
    private EditText height;
    private EditText weight;
    private EditText email;
    private TextView name;
    private RequestQueue mQueue;
    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        prefs = getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        ImageButton close = findViewById(R.id.closeEdit);
        ImageButton proceed = findViewById(R.id.tickEdit);
        height = findViewById(R.id.heightET);
        weight = findViewById(R.id.weightET);
        bmi = findViewById(R.id.bmiET);
        email = findViewById(R.id.emailET);
        name = findViewById(R.id.userNameTV);
        height.setText(getIntent().getStringExtra("height"));
        weight.setText(getIntent().getStringExtra("weight"));
        bmi.setText(getIntent().getStringExtra("bmi"));
        name.setText(getIntent().getStringExtra("name"));
        email.setText(getIntent().getStringExtra("email"));
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText weightET = findViewById(R.id.weightET);
                final EditText heightET = findViewById(R.id.heightET);
                JSONObject send = new JSONObject();
                try {
                    send.put("weight", weightET.getText().toString());
                    send.put("height", heightET.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Submit(send);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void Submit(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getApplicationContext()));

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, UPDATE_BMI_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.toString());
                                JSONObject jsonProfile = new JSONObject(jsonResponse.getString("profile"));
                                Log.d(TAG, jsonProfile.toString());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String strJSONError = new String(networkResponse.data);
                    JSONObject errorJSON;
                    try {
                        errorJSON = new JSONObject(strJSONError);
                        Toast.makeText(getApplicationContext(), errorJSON.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {

                String iv = prefs.getString("keyIv", "null");
                String encrypted = prefs.getString("encryptedKey", "");
                try {
                    Cryptor cryptor = new Cryptor();
                    cryptor.initKeyStore();
                    String decrypted = cryptor.decryptText(encrypted, iv);
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + decrypted);
                    return headers;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }


            @Override
            public byte[] getBody() {
                return saveData.getBytes(StandardCharsets.UTF_8);
            }

        };
        mQueue.add(stringRequest);
    }
}
