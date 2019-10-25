package com.team09.hwealth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StepsActivity extends AppCompatActivity {
    private static final String TAG = "StepsActivity";
    private RequestQueue mQueue;

    /*TODO
        Find specific day for steps

    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);
        final String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Button createSteps = findViewById(R.id.createStepsButton);
        Button retrieveSteps = findViewById(R.id.retrieveStepsButton);
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

                Submit(send);
            }
        });
        retrieveSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject send = new JSONObject();
//                try {
//                    send.put("totalSteps", step);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }try {
//                    send.put("dateRecorded",date);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                Log.d(TAG, send.toString());

                Retrieve(send);
            }
        });

    }

    private void Submit(JSONObject data) {
        String URL = "https://hwealth.herokuapp.com/api/steps-record";
        final String savedata = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        final TextView currentStepTV = findViewById(R.id.currentStepsTV);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject objres = new JSONObject(response);
                            if (objres.getString("error").equals("false")) {
                                Log.d(TAG, objres.toString());
                                Toast.makeText(StepsActivity.this, objres.getString("message"), Toast.LENGTH_LONG).show();
                                currentStepTV.setText(objres.getString("message"));
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
//                        Log.d(TAG,errorJSON.getString("message").toString());
                        Toast.makeText(StepsActivity.this, errorJSON.getString("message"), Toast.LENGTH_LONG).show();
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
                SharedPreferences sharedPref = getSharedPreferences("token", MODE_PRIVATE);
                String token = sharedPref.getString("token", null);
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
//                Log.d(TAG,token);
                return headers;
            }

            @Override
            public byte[] getBody() {
                return savedata.getBytes(StandardCharsets.UTF_8);
            }

        };
        mQueue.add(stringRequest);
    }

    private void Retrieve(JSONObject data) {
        String URL = "https://hwealth.herokuapp.com/api/steps-record";
        final String savedata = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        final TextView currentStepTV = findViewById(R.id.currentStepsTV);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject objres = new JSONObject(response);
                            if (objres.getString("error").equals("false")) {
                                Log.d(TAG, objres.toString());
//                                Toast.makeText(StepsActivity.this, objres.getString("records"), Toast.LENGTH_LONG).show();
                                JSONArray recordsJSONArr = new JSONArray(objres.getString("records"));
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
//                        Log.d(TAG,errorJSON.getString("message").toString());
                        Toast.makeText(StepsActivity.this, errorJSON.getString("message"), Toast.LENGTH_LONG).show();
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
                SharedPreferences sharedPref = getSharedPreferences("token", MODE_PRIVATE);
                String token = sharedPref.getString("token", null);
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
//                Log.d(TAG,token);
                return headers;
            }

            @Override
            public byte[] getBody() {
                return savedata.getBytes(StandardCharsets.UTF_8);
            }

        };
        mQueue.add(stringRequest);
    }
}
