package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.team09.hwealth.utils.Constants.DISABLE_TWO_FA_URL;
import static com.team09.hwealth.utils.Constants.ENABLE_TWO_FA_URL;


public class TwoFAFragment extends Fragment {
    private static final String TAG = "TwoFAFragment";
    private static final String SHAREDPREF = "SHAREDPREF";
    View view;
    private RequestQueue mQueue;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_two_fa, container, false);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);

        final EditText passwordET = view.findViewById(R.id.passwordET);
        Button confirmButton = view.findViewById(R.id.confirmPasswordButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (passwordET.getText() != null) {
                    JSONObject send = new JSONObject();
                    String passwordETStr = passwordET.getText().toString();
                    try {
                        send.put("password", passwordETStr);
                        if (Objects.requireNonNull(getActivity()).getIntent().getBooleanExtra("2FA", false)) {
                            DisableTwoFA(send);
                        } else {
                            EnableTwoFA(send);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Please Enter Password", Toast.LENGTH_LONG).show();
                }
            }

        });
        return view;

    }

    private void EnableTwoFA(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ENABLE_TWO_FA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
//                                Log.d(TAG, jsonResponse.toString());
                                JSONObject jsonProfile = new JSONObject(jsonResponse.getString("secret"));
                                Bundle bundle = new Bundle();
                                bundle.putString("twoFA", jsonProfile.toString());
                                TwoFAQRFragment fragment2 = new TwoFAQRFragment();
                                fragment2.setArguments(bundle);
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = Objects.requireNonNull(fragmentManager).beginTransaction();
                                fragmentTransaction.replace(R.id.frameLayout, fragment2);
                                fragmentTransaction.commit();

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
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), errorJSON.getString("message"), Toast.LENGTH_LONG).show();
                        if (errorJSON.getString("message").equals("Invalid token.")) {
                            Intent LoginActivityIntent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                            LoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                            startActivity(LoginActivityIntent);
                            getActivity().finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

    private void DisableTwoFA(JSONObject data) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DISABLE_TWO_FA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.toString());
                                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), jsonResponse.getString("message") + " You will be logged out", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
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
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), errorJSON.getString("message"), Toast.LENGTH_LONG).show();
                        if (errorJSON.getString("message").equals("Invalid token.")) {
                            Intent LoginActivityIntent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                            LoginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | FLAG_ACTIVITY_NEW_TASK);
                            startActivity(LoginActivityIntent);
                            getActivity().finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
