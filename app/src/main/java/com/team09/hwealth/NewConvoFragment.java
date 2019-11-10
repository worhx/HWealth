package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import io.michaelrocks.paranoid.Obfuscate;

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
import static com.team09.hwealth.utils.Constants.PROF_URL;
import static com.team09.hwealth.utils.Constants.SHARED_PREF;

@Obfuscate
public class NewConvoFragment extends Fragment {

    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_new_convo, container, false);
        // Inflate the layout for this fragment
        prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE);
        JSONObject messageJSON = new JSONObject();
        retrieveProf(messageJSON, view);

        return view;

    }

    private void retrieveProf(JSONObject data, View view){
        final String saveData = data.toString();
        final ArrayList<MessageData> names = new ArrayList<>();
        final ListView userListView = view.findViewById(R.id.user_list);
        RequestQueue mQueue = Volley.newRequestQueue(Objects.requireNonNull(Objects.requireNonNull(getActivity()).getApplicationContext()));
        StringRequest stringRequest = new StringRequest(Request.Method.GET, PROF_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")){
                                JSONArray jsonProf = new JSONArray(jsonResponse.getString("professionals"));

                                for(int i = 0; i < jsonProf.length(); i++){
                                    MessageData data = new MessageData();
                                    JSONObject convo = jsonProf.getJSONObject(i);
                                    data.setName(convo.getJSONObject("accountId").get("username").toString());
                                    data.setUid(convo.getJSONObject("accountId").get("_id").toString());
                                    names.add(data);
                                }
                            }

                            String[] nameArray = new String[names.size()];
                            for(int i = 0; i < names.size(); i++){
                                nameArray[i] = names.get(i).getName();
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.fragment_listview, nameArray);
                            userListView.setAdapter(adapter);
                            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    MessageListFragment mlf = new MessageListFragment();
                                    Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mlf).commit();
                                    mlf.sendId(names.get(i).getUid());
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
