package com.team09.hwealth;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.concurrent.Executor;

abstract class RecaptchaActivity extends AppCompatActivity implements View.OnClickListener, OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse> {
    //AndroidNetworking.initialize(applicationContext);
    private static final String TAG = "RegisterActivity";
    private static final String SITE_KEY = "6LeFk74UAAAAAL4n7fRYBIMw8Ri_G52acK3RfpVK";

    public void onClick(View v) {
        SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                .addOnSuccessListener((Executor) this,
                        new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                // Indicates communication with reCAPTCHA service was
                                // successful.
                                String userResponseToken = response.getTokenResult();
                                if (!userResponseToken.isEmpty()) {
                                    // Validate the user response token using the
                                    // reCAPTCHA siteverify API.

                                    //Invalid SiteKey
                                    if (userResponseToken == "12007") {
                                        Log.d(TAG, "Error Code: " + userResponseToken + " INVALID SITEKEY" );
                                    }

                                    //Invalid KeyType
                                    if (userResponseToken == "12008") {
                                        Log.d(TAG, "Error Code: " + userResponseToken + " INVALID KEYTYPE" );
                                    }

                                    //Invalid PackageName
                                    if (userResponseToken == "12013") {
                                        Log.d(TAG, "Error Code: " + userResponseToken + " INVALID PACKAGENAME" );
                                    }

                                    //Unsupported SDKVersion
                                    if (userResponseToken == "12006") {
                                        Log.d(TAG, "Error Code: " + userResponseToken + " INVALID SDKVERSION" );
                                    }

                                    //TimeOut
                                    if (userResponseToken == "15") {
                                        Log.d(TAG, "Error Code: " + userResponseToken + " TIMEOUT" );
                                    }

                                    //NetworkError
                                    if (userResponseToken == "7") {
                                        Log.d(TAG, "Error Code: " + userResponseToken + " NETWORKERROR" );
                                    }

                                    //GeneralError
                                    if (userResponseToken == "13") {
                                        Log.d(TAG, "Error Code: " + userResponseToken + " GENERALERROR" );
                                    }
                                }
                            }
                        })
                .addOnFailureListener((Executor) this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            Log.d(TAG, "Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            Log.d(TAG, "Error: " + e.getMessage());
                        }
                    }
                });
    }


}


