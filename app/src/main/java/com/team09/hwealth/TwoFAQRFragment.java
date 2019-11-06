package com.team09.hwealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.Context.WINDOW_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static com.team09.hwealth.utils.Constants.TWO_FA_QR_URL;


public class TwoFAQRFragment extends Fragment {
    private static final String SHAREDPREF = "SHAREDPREF";
    private SharedPreferences prefs;
    View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (getArguments() != null) {
            String twoFAStr = getArguments().getString("twoFA");
            try {
                prefs = Objects.requireNonNull(getActivity()).getSharedPreferences(SHAREDPREF, Context.MODE_PRIVATE);
                final JSONObject twoFAJSON = new JSONObject(twoFAStr);
                view = inflater.inflate(R.layout.fragment_two_faqr, container, false);
                qrGenerator(twoFAJSON.getString("otpauth_url"));
                final TextView securityCodeTV = view.findViewById(R.id.securityCodeTV);
                securityCodeTV.setText(twoFAJSON.getString("base32"));
                final EditText qrET = view.findViewById(R.id.qrET);
                Button confirmSecurityButton = view.findViewById(R.id.confirmSecurityCodeButton);
                confirmSecurityButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!(qrET.getText().toString().matches(""))) {
                            if (qrET.getText().toString().matches("^[0-9]{6}$")) {
                                JSONObject send = new JSONObject();
                                try {
                                    String token = qrET.getText().toString();
                                    send.put("secret", twoFAJSON.getString("base32"));
                                    send.put("token", token);
                                    Submit(send, view);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Enter only numbers", Toast.LENGTH_LONG).show();

                            }

                        } else {
                            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Enter Token", Toast.LENGTH_LONG).show();
                        }


                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return view;

    }

    private void qrGenerator(String data) {
        try {
            //setting size of qr code
            WindowManager manager = (WindowManager) Objects.requireNonNull(getActivity()).getSystemService(WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallestDimension = width < height ? width : height;


            //setting parameters for qr code
            String charset = "UTF-8"; // or "ISO-8859-1"
            Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            createQRCode(data, charset, hintMap, smallestDimension, smallestDimension);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createQRCode(String qrCodeData, String charset, Map hintMap, int qrCodeheight, int qrCodewidth) {

        try {
            //generating qr code in bitmatrix type
            BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
            //converting bitmatrix to bitmap
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            // All are 0, or black, by default
            for (int y = 0; y < height; y++) {
                int offset = y * width;
                for (int x = 0; x < width; x++) {
                    pixels[offset + x] = matrix.get(x, y) ? BLACK : WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            //setting bitmap to image view
            ImageView myImage = view.findViewById(R.id.imageView);
            myImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Submit(JSONObject data, final View v) {
        final String saveData = data.toString();
        RequestQueue mQueue = Volley.newRequestQueue(Objects.requireNonNull(getActivity()).getApplicationContext());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, TWO_FA_QR_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("error").equals("false")) {
                                TextView recoveryCodeTV = v.findViewById(R.id.recoveryCodeTV);
                                recoveryCodeTV.setText(jsonResponse.getString("recoveryCode"));
                                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), jsonResponse.getString("message") + " You will be logged out", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity().getApplicationContext(), LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();

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
