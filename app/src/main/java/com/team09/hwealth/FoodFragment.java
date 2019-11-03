package com.team09.hwealth;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FoodFragment extends Fragment {
    private static final String TAG = "StepsFragment";
    private static final String FOOD_URL = "https://hwealth.herokuapp.com/api/calories-record";
    private RequestQueue mQueue;
    private static final String SHAREDPREF = "SHAREDPREF";
    private SharedPreferences prefs;
    private ArrayList<String> mDate = new ArrayList<>();
    private ArrayList<String> mCalories = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_calories, container, false);
        final Spinner spinner = view.findViewById(R.id.foodTypeSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(this.getActivity()), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.food_type_array));
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        spinner.setAdapter(adapter);
        JSONObject send = new JSONObject();
        RetrieveSteps(send,view);
        return view;
    }

    private void RetrieveSteps(JSONObject data, final View view) {
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, FOOD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                Log.d(TAG, jsonResponse.toString());
                                JSONArray recordsJSONArr = new JSONArray(jsonResponse.getString("records"));
                                for(int i = 0;i<recordsJSONArr.length();i++){
                                    JSONObject recordsJSONArrJSONObject = recordsJSONArr.getJSONObject(i);
                                    Log.d(TAG,recordsJSONArrJSONObject.toString());
                                    String a = recordsJSONArrJSONObject.getString("totalCalories");
                                    String b = recordsJSONArrJSONObject.getString("dateRecorded");
                                    if (mDate.contains(b.substring(0,10))) {
                                        int value = mDate.indexOf(b.substring(0,10));
                                        mCalories.set(value,Integer.toString(Integer.parseInt( mCalories.get(value))+Integer.parseInt(a)));
                                    } else {
                                        mDate.add(b.substring(0,10));
                                        mCalories.add(a);
                                    }
                                }
                                Log.d(TAG,mCalories.toString());
                                Log.d(TAG,mDate.toString());
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
    private void initRecyclerView(View view){
        RecyclerView recyclerView = view.findViewById(R.id.recylerView);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mCalories,mDate,getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
}
///////////////////////////////////////////////////////
//private void SubmitFood(JSONObject data) {
//    final String saveData = data.toString();
//    mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
//
//    StringRequest stringRequest = new StringRequest(Request.Method.POST, FOOD_URL,
//            new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    try {
//                        JSONObject jsonResponse = new JSONObject(response);
//                        if (jsonResponse.getString("error").equals("false")) {
//                            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }, new Response.ErrorListener() {
//        @Override
//        public void onErrorResponse(VolleyError error) {
//            NetworkResponse networkResponse = error.networkResponse;
//            if (networkResponse != null && networkResponse.data != null) {
//                String strJSONError = new String(networkResponse.data);
//                JSONObject errorJSON;
//                try {
//                    errorJSON = new JSONObject(strJSONError);
//                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), errorJSON.getString("message"), Toast.LENGTH_LONG).show();
//                } catch (JSONException e) {
//                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//
//            }
//        }
//    }) {
//        @Override
//        public String getBodyContentType() {
//            return "application/json; charset=utf-8";
//        }
//
//        @Override
//        public Map<String, String> getHeaders() {
//
//            String iv = prefs.getString("keyIv", "null");
//            String encrypted = prefs.getString("encryptedKey", "");
//            try {
//                Cryptor cryptor = new Cryptor();
//                cryptor.initKeyStore();
//                String decrypted = cryptor.decryptText(encrypted, iv);
//                HashMap<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + decrypted);
//                return headers;
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//
//        @Override
//        public byte[] getBody() {
//            return saveData.getBytes(StandardCharsets.UTF_8);
//        }
//
//    };
//    mQueue.add(stringRequest);
//}
///////////////////////////////////////////////////////