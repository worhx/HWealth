package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.NoSuchPaddingException;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String LOGIN_URL = "https://hwealth.herokuapp.com/api/auth/login";
    private static final String SHAREDPREF = "SHAREDPREF";
    private RequestQueue mQueue;
    private ProgressBar progressBar;
    private SharedPreferences prefs;
    private boolean handledClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handledClick = false;
        setContentView(R.layout.activity_login);
        Button forgetPassword = findViewById(R.id.forgetPasswordButton);
        Button login = findViewById(R.id.login);
        Button register = findViewById(R.id.registerButton);
        final EditText userET = findViewById(R.id.userET);
        final EditText passET = findViewById(R.id.passwordET);
        progressBar = findViewById(R.id.progressBar);
        prefs = getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(this);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!handledClick) {
                    handledClick = true;
                    String userString = userET.getText().toString();
                    String passString = passET.getText().toString();
                    if (!userString.isEmpty() && !passString.isEmpty()) {
                        JSONObject send = new JSONObject();
                        try {
                            send.put("username", userString);
                            send.put("password", passString);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, send.toString());
                        progressBar.setVisibility(View.VISIBLE);
                        Submit(send);
                        handledClick = false;
                        InputMethodManager imm = (InputMethodManager) getSystemService(LoginActivity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    } else {
                        Toast.makeText(LoginActivity.this, "Please enter username or password", Toast.LENGTH_LONG).show();
                        handledClick = false;
                    }


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
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ForgetPasswordIntent = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
                startActivity(ForgetPasswordIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        handledClick = false;
    }

    ////Submit
    private void Submit(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if ((jsonResponse.getString("error").equals("false") && jsonResponse.getString("twoFactorEnabled").equals("false"))) {
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, jsonResponse.getString("token"));
                        String token = jsonResponse.getString("token");
                        Cryptor cryptor = new Cryptor();
                        try {
                            cryptor.setIv();
                            prefs.edit().putString("encryptedKey", cryptor.encryptText(token)).apply();
                            prefs.edit().putString("keyIv", cryptor.getIv_string()).apply();
                            Intent StepsActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                            StepsActivityIntent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(StepsActivityIntent);
                            finish();
                        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | InvalidKeyException e) {
                            e.printStackTrace();
                        }

                    } else if ((jsonResponse.getString("error").equals("false")) && (jsonResponse.getString("twoFactorEnabled").equals("true"))) {
                        Cryptor cryptor = new Cryptor();
                        prefs.edit().putString("encryptedKey", cryptor.encryptText(jsonResponse.getString("token"))).apply();
                        prefs.edit().putString("keyIv", cryptor.getIv_string()).apply();
                        Intent LoginTwoFAActivityIntent = new Intent(getApplicationContext(), LoginTwoFAActivity.class);
                        startActivity(LoginTwoFAActivityIntent);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                handledClick = false;
                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String strJSONError = new String(networkResponse.data);
                    JSONObject errorJSON;
                    try {
                        errorJSON = new JSONObject(strJSONError);
                        Toast.makeText(LoginActivity.this, errorJSON.getString("message"), Toast.LENGTH_LONG).show();
                        handledClick = false;
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

