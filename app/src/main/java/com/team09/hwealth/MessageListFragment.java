package com.team09.hwealth;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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


public class MessageListFragment extends Fragment {

    private static final String MESSAGE_URL = "https://hwealth.herokuapp.com/api/message";
    private static final String CONVO_URL = "https://hwealth.herokuapp.com/api/conversation";
    private static final String SHAREDPREF = "SHAREDPREF";
    private String uid;
    private String cid;
    private SharedPreferences prefs;
    private RequestQueue mQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View topview =  inflater.inflate(R.layout.fragment_message_list, container, false);
        Button msgBtn = topview.findViewById(R.id.button_chatbox_send);
        final EditText chatbox = topview.findViewById(R.id.edittext_chatbox);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity().getApplicationContext()));

        msgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageToSend = chatbox.getText().toString();

                if(messageToSend.isEmpty()){
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Please type in your message", Toast.LENGTH_LONG).show();
                }
                else{
                    JSONObject sendMsg = new JSONObject();
                    try{
                        sendMsg.put("recipient", uid);
                        sendMsg.put("message", messageToSend);
                    } catch(JSONException e){
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    submitMsg(sendMsg, topview);
                }
            }
        });
        JSONObject messageJSON = new JSONObject();
        getMsg(messageJSON, topview);

        // Inflate the layout for this fragment
        return topview;
    }

    protected void sendId(String uid, String cid){
        this.uid = uid;
        this.cid = cid;
    }

    private void submitMsg(JSONObject data, final View view){
        final String saveData = data.toString();
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MESSAGE_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("error").equals("false")){
                        JSONObject messageJSON = new JSONObject();
                        getMsg(messageJSON, view);
                        EditText msgETxt = view.findViewById(R.id.edittext_chatbox);
                        ListView lv = view.findViewById(R.id.listview_messages);
                        msgETxt.setText("");


                    }
                } catch (JSONException e) {
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        }){
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

    private void getMsg(JSONObject data, View view){
        final String saveData = data.toString();
        final ArrayList<MessageData> msgArrList = new ArrayList<>();
        final ListView listView = view.findViewById(R.id.listview_messages);
        mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CONVO_URL + "/" + cid, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (jsonResponse.getString("error").equals("false")){
                        JSONArray jsonMsgArr = new JSONArray(jsonResponse.getString("messages"));

                        for(int i = 0; i < jsonMsgArr.length(); i++){
                            MessageData data = new MessageData();
                            JSONObject jsonMsg = jsonMsgArr.getJSONObject(i);
                            data.setName(jsonMsg.getJSONObject("sentBy").getString("username"));
                            data.setMessage(jsonMsg.getString("message"));
                            String time = jsonMsg.getString("createdAt");
                            data.setTime(time.substring(0,10) + "\n" + time.substring(11,19));
                            msgArrList.add(data);
                        }
                        ArrayAdapter adapter = new MessageAdapter(getContext(), msgArrList);

                        listView.setAdapter(adapter);
                    }
                } catch (JSONException e) {
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        }){
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
