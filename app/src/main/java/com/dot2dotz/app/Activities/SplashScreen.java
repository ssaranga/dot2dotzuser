package com.dot2dotz.app.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dot2dotz.app.BuildConfig;
import com.dot2dotz.app.Dot2dotzApplicaton;
import com.dot2dotz.app.Helper.ConnectionHelper;
import com.dot2dotz.app.Helper.SharedHelper;
import com.dot2dotz.app.Helper.URLHelper;
import com.dot2dotz.app.R;
import com.dot2dotz.app.Utils.CommonUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.splunk.mint.Mint;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.dot2dotz.app.Dot2dotzApplicaton.trimMessage;


public class SplashScreen extends AppCompatActivity {

    String TAG = "SplashActivity";
    public Activity activity = SplashScreen.this;
    public Context context = SplashScreen.this;
    ConnectionHelper helper;
    Boolean isInternet;
    String device_token, device_UDID;
    Handler handleCheckStatus;
    int retryCount = 0;
    AlertDialog alert;
    TextView lblVersion;
    boolean begin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        CommonUtils.setLanguage(SplashScreen.this);

        //ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        lblVersion = (TextView) findViewById(R.id.lblVersion);
        Log.v("VERSION NAME", "" + BuildConfig.VERSION_NAME);
        Log.v("VERSION CODE", "" + BuildConfig.VERSION_CODE);
        lblVersion.setText(getResources().getString(R.string.version) + " " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")");
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(this.getApplication(), "2530eede");
//        getAccess();
        handleCheckStatus = new Handler();
        // check status every 3 sec


        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        handleCheckStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.w("Handler", "Called");
                if (helper.isConnectingToInternet()) {
                    if (SharedHelper.getKey(context, "loggedIn").equalsIgnoreCase(context.getResources().getString(R.string.True))) {
                        GetToken();
                        getProfile();
                    } else {


                        if (begin == false) {
                            GoToBeginActivity();
                            begin = true;
                        }
                        //GoToAccessActivity();
                        handleCheckStatus.removeCallbacksAndMessages(null);
                    }
                    if (alert != null && alert.isShowing()) {
                        alert.dismiss();
                    }
                } else {
                    showDialog();
                    handleCheckStatus.postDelayed(this, 3000);
                }
            }
        }, 3000);


    }

    public void getProfile() {
        Log.e("check","getProfile method Called");
        retryCount++;
        Log.e("GetPostAPI", "" + URLHelper.UserProfile + "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token);
        JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URLHelper.UserProfile + "" +
                "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.e(TAG, "GetProfile: " + response.toString());
                    SharedHelper.putKey(context, "id", response.optString("id"));
                    SharedHelper.putKey(context, "first_name", response.optString("first_name"));
                    SharedHelper.putKey(context, "last_name", response.optString("last_name"));
                    SharedHelper.putKey(context, "email", response.optString("email"));
                    SharedHelper.putKey(context, "user_type", response.optString("user_type"));
                    if (response.optString("picture").startsWith("http"))
                        SharedHelper.putKey(context, "picture", response.optString("picture"));
                    else
                        SharedHelper.putKey(context, "picture", URLHelper.base + "storage/" + response.optString("picture"));
                    SharedHelper.putKey(context, "gender", response.optString("gender"));
                    SharedHelper.putKey(context, "mobile", response.optString("mobile"));
                    SharedHelper.putKey(context, "wallet_balance", response.optString("wallet_balance"));
                    SharedHelper.putKey(context, "payment_mode", response.optString("payment_mode"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    SharedHelper.putKey(context, "sos", response.optString("sos"));
                    Log.e(TAG, "onResponse: Sos Call" + response.optString("sos"));
                    SharedHelper.putKey(context, "loggedIn", context.getResources().getString(R.string.True));
                    GoToMainActivity();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    if (retryCount < 5) {
                        getProfile();
                    } else if (retryCount >= 5) {

                        Log.e("check","goToBeginActivity method Called");
                        GoToBeginActivity();
                        //GoToAccessActivity();
                    }
                    String json = null;
                    String Message;
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {

                        try {
                            JSONObject errorObj = new JSONObject(new String(response.data));

                            if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                                try {
                                    displayMessage(errorObj.optString("message"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    displayMessage(context.getResources().getString(R.string.something_went_wrong));
                                }
                            } else if (response.statusCode == 401) {
                                refreshAccessToken();
                            } else if (response.statusCode == 422) {

                                json = trimMessage(new String(response.data));
                                if (json != null && !json.equalsIgnoreCase("")) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(context.getResources().getString(R.string.please_try_again));
                                }

                            } else if (response.statusCode == 503) {
                                displayMessage(context.getResources().getString(R.string.server_down));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            displayMessage(context.getResources().getString(R.string.something_went_wrong));
                        }

                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            getProfile();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        Dot2dotzApplicaton.getInstance().addToRequestQueue(jsonObjectRequest);

    }


    @Override
    protected void onDestroy() {
        handleCheckStatus.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void refreshAccessToken() {
        JSONObject object = new JSONObject();
        try {
            object.put("grant_type", "refresh_token");
            object.put("client_id", URLHelper.client_id);
            object.put("client_secret", URLHelper.client_secret);
            object.put("refresh_token", SharedHelper.getKey(context, "refresh_token"));
            object.put("scope", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("URL: ", URLHelper.login);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URLHelper.login, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.v("SignUpResponse", response.toString());
                    SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                    SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
                    SharedHelper.putKey(context, "token_type", response.optString("token_type"));
                    getProfile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    String json = null;
                    String Message;
                    NetworkResponse response = error.networkResponse;

                    if (response != null && response.data != null) {
                        SharedHelper.putKey(context, "loggedIn", context.getResources().getString(R.string.False));
                        GoToBeginActivity();
                        //GoToAccessActivity();
                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            refreshAccessToken();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };

        Dot2dotzApplicaton.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(activity, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        activity.finish();
    }

    public void GoToBeginActivity() {
        //Intent mainIntent = new Intent(activity, SignIn.class);
        Intent mainIntent = new Intent(activity, IntroActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void GoToAccessActivity() {
        Intent mainIntent = new Intent(activity, AccessKeyActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void displayMessage(String toastString) {
        Log.e("displayMessage", "" + toastString);
        Toast.makeText(activity, toastString, Toast.LENGTH_SHORT).show();
    }

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                Log.i(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = "" + FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                Log.i(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            e.printStackTrace();
            device_token = "COULD NOT GET FCM TOKEN";
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            Log.i(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            Log.d(TAG, "Failed to complete device UDID");
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(context.getResources().getString(R.string.connect_to_network))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.connect_to_wifi), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.quit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        if (alert == null) {
            alert = builder.create();
            alert.show();
        }
    }


}