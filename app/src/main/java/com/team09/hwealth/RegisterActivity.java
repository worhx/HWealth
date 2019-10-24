package com.team09.hwealth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private RequestQueue mQueue;
    //final String URL_VERIFY_ON_SERVER = "https://hwealth.herokuapp.com/api/captcha";
    final String SITE_KEY = "6LeFk74UAAAAAL4n7fRYBIMw8Ri_G52acK3RfpVK";
    private RequestQueue vQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Button register = findViewById(R.id.registerButton);
        final EditText fullNameET = findViewById(R.id.fullNameET);
        final EditText userET = findViewById(R.id.userET);
        final EditText passET = findViewById(R.id.passwordET);
        final EditText emailET = findViewById(R.id.emailET);
        mQueue = Volley.newRequestQueue(this);

        //captcha button
        Button verify = findViewById(R.id.recaptchaButton);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateCaptcha();
            }

        });


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
                Log.d(TAG, send.toString());
                Submit(send);
            }
        });
    }

    public void validateCaptcha() {
        SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                .addOnSuccessListener(this,
                        new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                // Indicates communication with reCAPTCHA service was
                                // successful.
                                String userResponseToken = response.getTokenResult();
                                if (!userResponseToken.isEmpty()) {
                                    // Validate the user response token using the
                                    // reCAPTCHA siteverify API.

                                    verifyTokenOnServer(userResponseToken);
                                }
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.d(TAG, "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d(TAG, "Error: " + e.getMessage());
                        }
                    }
                });
    }


    public void verifyTokenOnServer(String token) {
        JSONObject tokenJSON = new JSONObject();
        try {
            tokenJSON.put("captchaResponse", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String savedata;
        savedata = tokenJSON.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        String URL = "https://hwealth.herokuapp.com/api/captcha";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject objres = new JSONObject(response);
//                            Log.d(TAG, objres.toString());
                            //Toast.makeText(getApplicationContext(), "Response is: " + response.substring(0, 500), Toast.LENGTH_LONG).show();
                            if (objres.getString("success").equals("true")) {
                                Log.d(TAG, objres.toString());
                                Toast.makeText(RegisterActivity.this, objres.getString("success"), Toast.LENGTH_LONG).show();
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

                NetworkResponse networkResponse = error.networkResponse;
                if (networkResponse != null && networkResponse.data != null) {
                    String strJSONError = new String(networkResponse.data);
                    JSONObject errorJSON = null;
                    try {
                        errorJSON = new JSONObject(strJSONError);
//                        Log.d(TAG,errorJSON.getString("message").toString());
                        Toast.makeText(RegisterActivity.this, errorJSON.getString("error-codes"), Toast.LENGTH_LONG).show();
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


    //Log.d(TAG, strReq.toString());
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
