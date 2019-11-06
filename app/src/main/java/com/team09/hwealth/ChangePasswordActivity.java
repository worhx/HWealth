package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.team09.hwealth.utils.Constants.CHANGE_PASSWORD_URL;
import static com.team09.hwealth.utils.Constants.SHARED_PREF;

public class ChangePasswordActivity extends AppCompatActivity {
    private RequestQueue mQueue;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Button changePasswordButton = findViewById(R.id.changePasswordButton);
        prefs = Objects.requireNonNull(getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE));
        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText currentPasswordET = findViewById(R.id.currentPasswordET);
                EditText newPasswordET = findViewById(R.id.newPasswordET);
                EditText confirmPasswordET = findViewById(R.id.confirmPasswordET);
                if ((!currentPasswordET.getText().toString().equals("")) && (!newPasswordET.getText().toString().equals("")) && (!confirmPasswordET.getText().toString().equals(""))) {
                    if (newPasswordET.getText().toString().equals(confirmPasswordET.getText().toString())) {
                        if ((newPasswordET.getText().toString().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*<>])(?=\\S+$).{8,64}$")) && (confirmPasswordET.getText().toString().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*<>])(?=\\S+$).{8,64}$"))) {
                            String currentPasswordStr = currentPasswordET.getText().toString();
                            String newPasswordStr = newPasswordET.getText().toString();
                            String confirmPasswordStr = confirmPasswordET.getText().toString();
                            JSONObject pwJSON = new JSONObject();
                            try {
                                pwJSON.put("currentPassword", currentPasswordStr);
                                pwJSON.put("newPassword", newPasswordStr);
                                pwJSON.put("confirmPassword", confirmPasswordStr);
                                ChangePassword(pwJSON);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "New Password must contain 1 Uppercase, 1 Lowercase, 1 Number, 1 Special Character, a minimum of 8 characters in total, and no white spaces.", Toast.LENGTH_LONG).show();

                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please do not leave any fields empty", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void ChangePassword(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getApplicationContext()));

        StringRequest stringRequest = new StringRequest(Request.Method.PUT, CHANGE_PASSWORD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Toast.makeText(getApplicationContext(), jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                                Intent LoginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                LoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
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
                        Toast.makeText(getApplicationContext(), errorJSON.getString("message"), Toast.LENGTH_LONG).show();
                        if (errorJSON.getString("message").equals("Invalid token.")) {
                            Intent LoginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
                            LoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                            startActivity(LoginActivityIntent);
                            finish();
                        }
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
