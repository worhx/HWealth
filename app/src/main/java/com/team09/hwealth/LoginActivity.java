package com.team09.hwealth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
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

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private RequestQueue mQueue;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button login = findViewById(R.id.login);
        Button register = findViewById(R.id.registerButton);
        final EditText userET = findViewById(R.id.userET);
        final EditText passET = findViewById(R.id.passwordET);
        progressBar = findViewById(R.id.progressBar);
        mQueue = Volley.newRequestQueue(this);
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String userString = userET.getText().toString();
                String passString = passET.getText().toString();
                if (!userString.isEmpty() || !passString.isEmpty()) {
                    JSONObject send = new JSONObject();
                    try {
                        send.put("username", userString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        send.put("password",passString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG,send.toString());
                    progressBar.setVisibility(View.VISIBLE);
                    Submit(send);
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter username or password", Toast.LENGTH_LONG).show();
                }


            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RegisterActivityIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(RegisterActivityIntent);
            }
        });
    }
    ////Submit
    private void Submit(JSONObject data)
    {
        String URL = "https://hwealth.herokuapp.com/api/auth/login";
        final String savedata = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject objres = new JSONObject(response);
                    if (objres.getString("error") == "false") {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                        SharedPreferences.Editor editor = getSharedPreferences("token", MODE_PRIVATE).edit();
                        editor.putString("token", objres.getString("token"));
                        editor.apply();

                        //Toast.makeText(LoginActivity.this,sharedPref.getString("token","null"),Toast.LENGTH_LONG).show();
//                        Intent MainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
//                        startActivity(MainActivityIntent);
                        progressBar.setVisibility(View.GONE);
                        Intent StepsActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(StepsActivityIntent);
                        finish();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String strJSONError = new String(networkResponse.data);
                    JSONObject errorJSON = null;
                    try {
                        errorJSON = new JSONObject(strJSONError);
//                        Log.d(TAG,errorJSON.getString("message").toString());
                        Toast.makeText(LoginActivity.this, errorJSON.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return savedata == null ? null : savedata.getBytes(StandardCharsets.UTF_8);
            }

        };
        mQueue.add(stringRequest);
    }
}

