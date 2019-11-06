package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.team09.hwealth.utils.Constants.SHARED_PREF;
import static com.team09.hwealth.utils.Constants.STEP_URL;

public class StepsFragment extends Fragment {
    private RequestQueue mQueue;
    private SharedPreferences prefs;
    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> mStep = new ArrayList<>();
    private String date;
    private RecyclerViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_steps, container, false);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        JSONObject send = new JSONObject();
        RetrieveSteps(send, view);
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Button createSteps = view.findViewById(R.id.createStepsButton);
        final EditText stepsET = view.findViewById(R.id.stepsET);

        createSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stepsET.getText().toString().equals("")) {
                    if (stepsET.getText().toString().matches("^[0-9]{1,6}$")) {
                        JSONObject send = new JSONObject();
                        int steps = Integer.parseInt(stepsET.getText().toString());
                        try {
                            send.put("totalSteps", steps);
                            send.put("dateRecorded", date);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        stepsET.setText("");
                        SubmitSteps(send,view);
                    } else {
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Please enter number within 6 digits", Toast.LENGTH_LONG).show();

                    }
                } else {
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Please enter steps", Toast.LENGTH_LONG).show();
                }

            }
        });
        return view;
    }

    private void RetrieveSteps(JSONObject data, final View view) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, STEP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                JSONArray recordsJSONArr = new JSONArray(jsonResponse.getString("records"));
                                for (int i = 0; i < recordsJSONArr.length(); i++) {
                                    JSONObject recordsJSONArrJSONObject = recordsJSONArr.getJSONObject(i);
                                    String a = recordsJSONArrJSONObject.getString("totalSteps");
                                    String b = recordsJSONArrJSONObject.getString("dateRecorded");
                                    if (mDate.contains(b.substring(0,10))) {
                                        int value = mDate.indexOf(b.substring(0,10));
                                        mStep.set(value,Integer.toString(Integer.parseInt( mStep.get(value))+Integer.parseInt(a)));
                                    } else {
                                        mDate.add(b.substring(0,10));
                                        mStep.add(a);
                                    }
                                }
                                initRecyclerView(view);
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

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recylerView);
        adapter = new RecyclerViewAdapter(mStep, mDate, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void SubmitSteps(JSONObject data, final View view) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, STEP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                mStep.clear();
                                mDate.clear();
                                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                                JSONObject send = new JSONObject();
                                RetrieveSteps(send,view);
                                adapter.notifyDataSetChanged();
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
