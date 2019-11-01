package com.team09.hwealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MessageFragment extends Fragment {
    private static final String CONVERSATION_URL = "https://hwealth.herokuapp.com/api/conversation";
    private static final String SHAREDPREF = "SHAREDPREF";
    private SharedPreferences prefs;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_message,container,false);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        JSONObject messageJSON = new JSONObject();
        retrieveName(messageJSON, view);


        return view;
    }

    private void retrieveName(JSONObject data, View view){
        final String saveData = data.toString();
        final ArrayList<MessageData> names = new ArrayList<>();
        final ListView messageListView = view.findViewById(R.id.message_list);
        RequestQueue mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity().getApplicationContext()));
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CONVERSATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            String cid = "";
                            if (jsonResponse.getString("error").equals("false")){
                                JSONArray jsonConvo = new JSONArray(jsonResponse.getString("allConversation"));

                                for(int i = 0; i < jsonConvo.length(); i++){
                                    MessageData data = new MessageData();
                                    JSONObject convo = jsonConvo.getJSONObject(i);
                                    cid = convo.getString("_id");
                                    JSONArray members = convo.getJSONArray("members");
                                    JSONObject name = members.getJSONObject(0);
                                    data.setName(name.getJSONObject("accountId").get("username").toString());
                                    data.setUid(name.getJSONObject("accountId").get("_id").toString());
                                    data.setCid(cid);
                                    names.add(data);
                                }
                            }

                            String[] nameArray = new String[names.size()];
                            for(int i = 0; i < names.size(); i++){
                                nameArray[i] = names.get(i).getName();
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.fragment_listview, nameArray);
                            messageListView.setAdapter(adapter);
                            messageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    MessageListFragment mlf = new MessageListFragment();
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,mlf).commit();
                                    mlf.sendId(names.get(i).getUid(), names.get(i).getCid());
                                }
                            });
                        } catch(JSONException e){
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
