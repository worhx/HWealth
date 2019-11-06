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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import static com.team09.hwealth.utils.Constants.ACCOUNT_URL;
import static com.team09.hwealth.utils.Constants.PROFILE_URL;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final String SHAREDPREF = "SHAREDPREF";
    private String height;
    private String weight;
    private String bmi;
    private String dob;
    private String name;
    private String email;
    private RequestQueue mQueue;
    private SharedPreferences prefs;
    private boolean handledClick = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        Button twoFAButton = view.findViewById(R.id.twoFAButton);
        if (Objects.requireNonNull(getActivity()).getIntent().getBooleanExtra("2FA", false)) {
            twoFAButton.setText(R.string.remove_two_FA);
        }
        Button changePasswordButton = view.findViewById(R.id.changePasswordButton);
        ImageButton setting = view.findViewById(R.id.setting);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        JSONObject send = new JSONObject();
        RetrieveProfile(send, view);
        RetrieveAccount(send, view);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditActivity.class);
                intent.putExtra("height", height);
                intent.putExtra("weight", weight);
                intent.putExtra("bmi", bmi);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("dob", dob);
                startActivity(intent);

            }
        });
        twoFAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!handledClick) {
                    handledClick = true;
                    Intent TwoFAActivity = new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(), TwoFAActivity.class);
                    if (Objects.requireNonNull(getActivity()).getIntent().getBooleanExtra("2FA", false)) {
                        TwoFAActivity.putExtra("2FA", true);
                        startActivity(TwoFAActivity);
                    } else {
                        TwoFAActivity.putExtra("2FA", false);
                        startActivity(TwoFAActivity);
                    }
                }
            }
        });
        handledClick = false;

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LoginActivity = new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(), LoginActivity.class);
                LoginActivity.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(LoginActivity);
                getActivity().finish();
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ChangePasswordActivity = new Intent(Objects.requireNonNull(getActivity()).getApplicationContext(), com.team09.hwealth.ChangePasswordActivity.class);
                startActivity(ChangePasswordActivity);
            }
        });
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        handledClick = false;
        JSONObject send = new JSONObject();
        RetrieveAccount(send, Objects.requireNonNull(super.getView()));
        RetrieveProfile(send, super.getView());
        Log.d(TAG, "onresume");
    }

    @Override
    public void onStart() {
        super.onStart();
        JSONObject send = new JSONObject();
        RetrieveAccount(send, Objects.requireNonNull(super.getView()));
        RetrieveProfile(send, super.getView());
        Log.d(TAG, "onstart");

    }

    private void RetrieveProfile(JSONObject data, View view) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        final TextView heightTextView = view.findViewById(R.id.heightTV);
        final TextView weightTextView = view.findViewById(R.id.weightTV);
        final TextView bmiTextView = view.findViewById(R.id.bmiTV);
        final TextView nameTextView = view.findViewById(R.id.fullNameTV);
        final TextView dobTextView = view.findViewById(R.id.dobTV);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.toString());
                                JSONObject jsonProfile = new JSONObject(jsonResponse.getString("profile"));
                                Log.d(TAG, jsonProfile.toString());
                                name = jsonProfile.getString("fullname");
                                nameTextView.setText(name);
                                if (jsonProfile.has("weight") && jsonProfile.has("height") && jsonProfile.has("bmi")) {
                                    height = jsonProfile.getString("height");
                                    weight = jsonProfile.getString("weight");
                                    bmi = jsonProfile.getString("bmi");
                                    weightTextView.setText(weight);
                                    heightTextView.setText(height);
                                    bmiTextView.setText(bmi);
                                    if (jsonProfile.has("dateOfBirth")) {
                                        dob = jsonProfile.getString("dateOfBirth").substring(0, 10);
                                        dobTextView.setText(dob);
                                    }
                                }
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

    private void RetrieveAccount(JSONObject data, View view) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        final TextView emailTextView = view.findViewById(R.id.emailTV);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, ACCOUNT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.toString());
                                JSONObject jsonProfile = new JSONObject(jsonResponse.getString("account"));
                                Log.d(TAG, jsonProfile.toString());
                                email = jsonProfile.getString("email");
                                emailTextView.setText(email);
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
