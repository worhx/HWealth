package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.team09.hwealth.utils.Constants.CONVERSATION_URL;
import static com.team09.hwealth.utils.Constants.MESSAGE_URL;
import static com.team09.hwealth.utils.Constants.SHARED_PREF;


public class MessageListFragment extends Fragment {
    private static final int delay = 10*1000;
    private String uid = "";
    private String cid = "";
    private SharedPreferences prefs;
    private RequestQueue mQueue;
    private Handler handler = new Handler();
    private Runnable runnable;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View topview =  inflater.inflate(R.layout.fragment_message_list, container, false);
        Button msgBtn = topview.findViewById(R.id.button_chatbox_send);
        final EditText chatbox = topview.findViewById(R.id.edittext_chatbox);
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
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
        if(!cid.isEmpty()){
            getMsg(messageJSON, topview);
        }


        // Inflate the layout for this fragment
        return topview;
    }

    @Override
    public void onResume() {
        //start handler as activity become visible

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                //do something
                if(!cid.isEmpty()){
                    JSONObject messageJSON = new JSONObject();
                    getMsg(messageJSON, Objects.requireNonNull(getView()));
                    handler.postDelayed(runnable, delay);
                }
            }
        }, delay);

        super.onResume();
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    void sendId(String uid, String cid){
        this.uid = uid;
        this.cid = cid;
    }
    void sendId(String uid){
        this.uid = uid;
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
                        if(cid.isEmpty()){
                            JSONObject cidJSON = new JSONObject();
                            getCid(cidJSON);
                        }
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
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CONVERSATION_URL + "/" + cid, new Response.Listener<String>() {
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

    private void getCid(JSONObject data){
        final String saveData = data.toString();
        final ArrayList<MessageData> names = new ArrayList<>();
        RequestQueue mQueue = Volley.newRequestQueue(Objects.requireNonNull(Objects.requireNonNull(getActivity()).getApplicationContext()));
        StringRequest stringRequest = new StringRequest(Request.Method.GET, CONVERSATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);

                            if (jsonResponse.getString("error").equals("false")){
                                JSONArray jsonConvo = new JSONArray(jsonResponse.getString("allConversation"));

                                for(int i = 0; i < jsonConvo.length(); i++){
                                    MessageData data = new MessageData();
                                    JSONObject convo = jsonConvo.getJSONObject(i);

                                    JSONArray members = convo.getJSONArray("members");
                                    JSONObject name = members.getJSONObject(0);
                                    if(name.getJSONObject("accountId").get("_id").toString().equals(uid)){
                                        cid = convo.getString("_id");
                                        break;
                                    }
                                }
                            }
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
