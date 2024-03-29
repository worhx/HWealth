package com.team09.hwealth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import io.michaelrocks.paranoid.Obfuscate;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import static com.team09.hwealth.utils.Constants.CAPTCHA_URL;
import static com.team09.hwealth.utils.Constants.REGISTER_URL;
import static com.team09.hwealth.utils.Constants.SITE_KEY;
@Obfuscate
public class RegisterActivity extends AppCompatActivity {
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Button register = findViewById(R.id.registerButton);
        final EditText fullNameET = findViewById(R.id.fullNameET);
        final EditText userET = findViewById(R.id.userET);
        final EditText passET = findViewById(R.id.passwordET);
        final EditText emailET = findViewById(R.id.emailET);
        final EditText confirmPasswordET = findViewById(R.id.confirmPasswordET);
        mQueue = Volley.newRequestQueue(this);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((fullNameET.getText().toString().equals("")) || (userET.getText().toString().equals("")) || (passET.getText().toString().equals(""))
                        || (emailET.getText().toString().equals("")) || (confirmPasswordET.getText().toString().equals(""))) {
                    Toast.makeText(getApplicationContext(), "Please do not leave fields empty", Toast.LENGTH_LONG).show();
                } else if ((fullNameET.getText().length() < 5) || (userET.getText().length() < 5)) {
                    Toast.makeText(getApplicationContext(), "Username or full name cannot be less than 5 characters ", Toast.LENGTH_LONG).show();
                } else if (!fullNameET.getText().toString().matches("^[a-zA-Z .,\\-']{1,100}$")) {
                    Toast.makeText(getApplicationContext(), "Invalid full name.", Toast.LENGTH_LONG).show();
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailET.getText().toString()).matches()) {
                    Toast.makeText(getApplicationContext(), "Please enter valid email address", Toast.LENGTH_LONG).show();
                } else if (!userET.getText().toString().matches("^[a-zA-Z0-9]*$")) {
                    Toast.makeText(getApplicationContext(), "Username should only contain upper and lowercase letters and numbers", Toast.LENGTH_LONG).show();
                } else if (!passET.getText().toString().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*<>])(?=\\S+$).{8,64}$")) {
                    Toast.makeText(getApplicationContext(), "Password must contain 1 Uppercase, 1 Lowercase, 1 Number, 1 Special Character, a minimum of 8 characters in total, and no white spaces.", Toast.LENGTH_LONG).show();
                } else if (!passET.getText().toString().matches(confirmPasswordET.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_LONG).show();

                } else {
                    String fullNameString = fullNameET.getText().toString();
                    String userString = userET.getText().toString();
                    String passString = passET.getText().toString();
                    String emailString = emailET.getText().toString();
                    JSONObject send = new JSONObject();

                    try {
                        send.put("fullname", fullNameString);
                        send.put("username", userString);
                        send.put("password", passString);
                        send.put("email", emailString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    validateCaptcha(send);
                }
            }


        });
    }

    public void validateCaptcha(final JSONObject submitJSON) {
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
                                    verifyTokenOnServer(userResponseToken, submitJSON);
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
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void verifyTokenOnServer(String token, final JSONObject submit) {
        JSONObject tokenJSON = new JSONObject();
        try {
            tokenJSON.put("captchaResponse", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String saveData;
        saveData = tokenJSON.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, CAPTCHA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("success").equals("true")) {
                                Submit(submit);
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
            public byte[] getBody() {
                return saveData.getBytes(StandardCharsets.UTF_8);
            }

        };
        mQueue.add(stringRequest);
    }

    private void Submit(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Toast.makeText(RegisterActivity.this, jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
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
                    JSONObject errorJSON;
                    try {
                        errorJSON = new JSONObject(strJSONError);
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
            public byte[] getBody() {
                return saveData.getBytes(StandardCharsets.UTF_8);
            }

        };
        mQueue.add(stringRequest);
    }


}
