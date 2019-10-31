package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class LoginTwoFAActivity extends AppCompatActivity {
    private static final String VERIFY_TWO_FA = "https://hwealth.herokuapp.com/api/two-factor/authenticate";
    private static final String SHAREDPREF = "SHAREDPREF";
    private static String TAG = "LoginTwoFAActivity";
    private RequestQueue mQueue;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_two_fa);
        Button confirmSixDigitCodeButton = findViewById(R.id.confirmSixDigitCodeButton);
        prefs = getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);

        confirmSixDigitCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText sixDigitCodeET = findViewById(R.id.sixDigitCodeET);
                if (sixDigitCodeET.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Code", Toast.LENGTH_LONG).show();
                } else if (!(sixDigitCodeET.getText().toString().equals("")) && (sixDigitCodeET.getText().toString().matches("^[0-9]{6}$"))) {
                    JSONObject sixDigitJSON = new JSONObject();
                    try {
                        sixDigitJSON.put("token", sixDigitCodeET.getText().toString());
                        Submit(sixDigitJSON);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please Enter Six Digits", Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void Submit(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, VERIFY_TWO_FA, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("error").equals("false")) {
                        Toast.makeText(LoginTwoFAActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(LoginTwoFAActivity.this, errorJSON.getString("message"), Toast.LENGTH_LONG).show();
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
