package com.team09.hwealth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


public class ForgetPasswordActivity extends AppCompatActivity {
    private static final String FORGETPASSWORD_URL = "https://hwealth.herokuapp.com/api/account/forgot-password";
    private static final String TAG = "ForgetPasswordActivity";
    private RequestQueue mQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        Button resetPassword = findViewById(R.id.resetPasswordButton);
        final EditText email = findViewById(R.id.emailET);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText() != null) {
                    JSONObject send = new JSONObject();
                    try {
                        send.put("email", email.getText().toString());
                        Log.d(TAG, "text");
                        Submit(send);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });


    }


    private void Submit(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, FORGETPASSWORD_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("error").equals("false")) {
                        Toast.makeText(ForgetPasswordActivity.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                        Intent LoginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(LoginActivityIntent);
                        finish();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
                        Toast.makeText(ForgetPasswordActivity.this, errorJSON.getString("message"), Toast.LENGTH_LONG).show();
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
            public byte[] getBody() {
                return saveData.getBytes(StandardCharsets.UTF_8);
            }

        };
        mQueue.add(stringRequest);
    }

}
