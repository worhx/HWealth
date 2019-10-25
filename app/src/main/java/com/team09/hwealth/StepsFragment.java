package com.team09.hwealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class StepsFragment extends Fragment {
    private static final String TAG = "StepsActivity";
    private static final String STEP_URL = "https://hwealth.herokuapp.com/api/steps-record";
    private RequestQueue mQueue;
    private static final String SHAREDPREF = "SHAREDPREF";

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_steps, container, false);
        final String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Button createSteps = view.findViewById(R.id.createStepsButton);
        Button retrieveSteps = view.findViewById(R.id.retrieveStepsButton);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);

        //step fixed at 199 for testing
        final int step = 199;
        createSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                JSONObject send = new JSONObject();
                try {
                    send.put("totalSteps", step);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    send.put("dateRecorded", date);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, send.toString());

                Submit(send, view);
            }
        });
        retrieveSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject send = new JSONObject();
//
                Log.d(TAG, send.toString());

                Retrieve(send, view);
            }
        });
        return view;
    }

    private void Submit(JSONObject data, View view) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        final TextView currentStepTV = view.findViewById(R.id.currentStepsTV);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, STEP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.toString());
                                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                                currentStepTV.setText(jsonResponse.getString("message"));
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

    private void Retrieve(JSONObject data, View view) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        final TextView currentStepTV = view.findViewById(R.id.currentStepsTV);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, STEP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.toString());
                                JSONArray recordsJSONArr = new JSONArray(jsonResponse.getString("records"));
                                Log.d(TAG, recordsJSONArr.toString());
                                JSONObject recordsJSONArrJSONObject = recordsJSONArr.getJSONObject(0);
                                currentStepTV.setText(recordsJSONArrJSONObject.getString("totalSteps"));
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
