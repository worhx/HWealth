package com.example.a3x03;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private RequestQueue mQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button login = findViewById(R.id.login);
        final EditText idET = findViewById(R.id.idET);
        final EditText passET = findViewById(R.id.passwordET);
        mQueue = Volley.newRequestQueue(this);
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String idString = idET.getText().toString();
                String passString = passET.getText().toString();
                JSONObject send = new JSONObject();
                try {
                    send.put("username",idString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    send.put("password",passString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,send.toString());
                Submit(send);
            }
        });
    }
    ////Submit
    private void Submit(JSONObject data)
    {
        String URL="https://jsonplaceholder.typicode.com/posts";
        final String savedata = data.toString();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    //correct response
                    JSONObject objres=new JSONObject(response);
                    JSONObject fakeresult = new JSONObject();
                    fakeresult.put("error","false");
                    fakeresult.put("username","jerrylim");
                    fakeresult.put("token","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI1ZDkwODMwZDZmY2U0ODQxMzBmMmQwZGEiLCJ1c2VybmFtZSI6ImpvaG5kb2UiLCJpYXQiOjE1Njk4MjYzNjAsImV4cCI6MTU2OTgyOlp2MH0.-xkuijaih-YDz6XpEJCAXMi8jVaxoVqoLafKcJhaF2E");
                    if(!Boolean.parseBoolean(fakeresult.getString("error"))){
                        Intent MainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(MainActivityIntent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"else",Toast.LENGTH_SHORT);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"Server Error",Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null : savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    //Log.v("Unsupported Encoding while trying to get the bytes", data);
                    return null;
                }
            }

        };
        mQueue.add(stringRequest);
    }
}

