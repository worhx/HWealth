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

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class EditActivity extends AppCompatActivity {
    private static final String TAG = "EditActivity";
    private static final String UPDATE_BMI_URL = "https://hwealth.herokuapp.com/api/profile/update-bmi";
    private static final String UPDATE_EMAIL_URL = "https://hwealth.herokuapp.com/api/account/update-email";
    private static final String UPDATE_DOB_URL = "https://hwealth.herokuapp.com/api/profile/update-profile";
    private static final String SHAREDPREF = "SHAREDPREF";
    private EditText bmi;
    private EditText height;
    private EditText weight;
    private TextView name;
    private EditText email;
    private EditText dob;
    private String intentDobStr;
    private String intentEmailStr;
    private String intentNameStr;
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
        name = findViewById(R.id.userTV);
        bmi = findViewById(R.id.bmiET);
        email = findViewById(R.id.emailET);
        dob = findViewById(R.id.dobET);
        dob.setText(getIntent().getStringExtra("dob"));
        intentDobStr = getIntent().getStringExtra("dob");
        height.setText(getIntent().getStringExtra("height"));
        weight.setText(getIntent().getStringExtra("weight"));
        bmi.setText(getIntent().getStringExtra("bmi"));
        name.setText(getIntent().getStringExtra("name"));
        intentNameStr = getIntent().getStringExtra("name");
        email.setText(getIntent().getStringExtra("email"));
        intentEmailStr = getIntent().getStringExtra("email");
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!height.getText().toString().equals("") && !(weight.getText().toString().equals(("")))) {
                    if (height.getText().toString().matches("^\\b[1-9]\\d{0,2}\\.\\d{0,2}\\b")) {
                        if (weight.getText().toString().matches("\\b[1-9]\\d{0,2}\\.\\d{0,2}\\b")) {
                            JSONObject bmiJSON = new JSONObject();
                            JSONObject emailJSON = new JSONObject();
                            JSONObject dobJSON = new JSONObject();
                            final EditText weightET = findViewById(R.id.weightET);
                            final EditText heightET = findViewById(R.id.heightET);
                            final EditText emailET = findViewById(R.id.emailET);
                            final EditText dobET = findViewById(R.id.dobET);
                            try {
                                bmiJSON.put("weight", weightET.getText().toString());
                                bmiJSON.put("height", heightET.getText().toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (!emailET.getText().toString().equals(intentEmailStr)) {
                                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                                    try {
                                        emailJSON.put("email", emailET.getText().toString());
                                        SubmitEmail(emailJSON);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "Please enter email in the format example@gmail.com", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            } else if (!dobET.getText().toString().equals(intentDobStr)) {
                                if (dobET.getText().toString().matches("^[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30)))$")) {
                                    try {
                                        dobJSON.put("dateOfBirth", dobET.getText().toString());
                                        dobJSON.put("fullname", intentNameStr);
                                        SubmitDOB(dobJSON);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                } else {
                                    Toast.makeText(getApplicationContext(), "Please enter date in the format 1992-12-19", Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                            SubmitBMI(bmiJSON);
                            Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter weight in the format 60.5", Toast.LENGTH_LONG).show();

                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter height in the format 1.65", Toast.LENGTH_LONG).show();

                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Please do not height or weight empty", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void SubmitBMI(JSONObject data) {
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

    private void SubmitEmail(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getApplicationContext()));

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, UPDATE_EMAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.getString("message"));

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

    private void SubmitDOB(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getApplicationContext()));

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, UPDATE_DOB_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.getString("message"));

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
