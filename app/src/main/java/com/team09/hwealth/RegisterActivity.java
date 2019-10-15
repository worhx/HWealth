package com.team09.hwealth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Button register = findViewById(R.id.register);
        final EditText fullNameET = findViewById(R.id.fullNameET);
        final EditText userET = findViewById(R.id.userET);
        final EditText passET = findViewById(R.id.passwordET);
        final EditText emailET = findViewById(R.id.emailET);
        mQueue = Volley.newRequestQueue(this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullNameString = fullNameET.getText().toString();
                String userString = userET.getText().toString();
                String passString = passET.getText().toString();
                String emailString = emailET.getText().toString();
                JSONObject send = new JSONObject();

                try {
                    send.put("fullname", fullNameString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    send.put("username", userString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    send.put("password", passString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    send.put("email", emailString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    send.put("role", "User");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, send.toString());
                Submit(send);
            }
        });
    }

    private void Submit(JSONObject data) {
        String URL = "https://hwealth.herokuapp.com/api/account/register";
        final String savedata = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject objres = new JSONObject(response);
//                            Log.d(TAG, objres.toString());
                            //Toast.makeText(getApplicationContext(), "Response is: " + response.substring(0, 500), Toast.LENGTH_LONG).show();
                            if (objres.getString("error") == "false") {
                                Log.d(TAG, objres.toString());
                                Toast.makeText(RegisterActivity.this, objres.getString("message"), Toast.LENGTH_LONG).show();
                                Intent LoginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(LoginActivityIntent);
                                finish();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(getApplicationContext(), "That didn't work!", Toast.LENGTH_LONG).show();
//                Log.d(TAG, "Error" + error.getMessage());

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String strJSONError = new String(networkResponse.data);
                    JSONObject errorJSON = null;
                    try {
                        errorJSON = new JSONObject(strJSONError);
//                        Log.d(TAG,errorJSON.getString("message").toString());
                        Toast.makeText(RegisterActivity.this, errorJSON.getString("message"), Toast.LENGTH_LONG).show();
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
            public byte[] getBody() throws AuthFailureError {
                return savedata == null ? null : savedata.getBytes(StandardCharsets.UTF_8);
            }

        };
        mQueue.add(stringRequest);
    }


}
