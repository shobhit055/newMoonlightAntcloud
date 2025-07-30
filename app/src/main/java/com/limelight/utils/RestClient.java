package com.limelight.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import org.bouncycastle.jcajce.provider.symmetric.ARC4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestClient {
    Context context;
    boolean status;
    int statusCode = 0;
    String responseString;
    int timeOut = 30000;
    boolean errorLog = true;
    boolean requestLog = true;
    boolean responseLog = true;
    String TAG_RESPONSE = "apis";
    String INTERNET_CONNECTION_ERROR = "Please check internet connection";
    String API_ERROR = "Failed to connect. Please try again later.";

    public RestClient(Context context) {
        this.context = context;
    }

    public static boolean isOnline(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    public RestClient postRequestWithHeader(final String tag, final String postUrl, final HashMap<String, String> postParams, final String apiToken, final String xLocalisation, final ResponseListener responseListener, final ErrorListener errorListener) {
        if (isOnline(context)) {
            HandlerThread handlerThread = new HandlerThread("HandlerThread");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    InputStream is = null;
                    String authorizationValue = apiToken;
                    try {
                        URL url = new URL("https://api.antcloud.co/api/" + postUrl); //+ postUrl
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();

                        con.setRequestProperty("Accept", "application/json");
                        con.setRequestProperty("Authorization", authorizationValue);
                        con.setDoOutput(true);
                        con.setReadTimeout(timeOut);
                        con.setConnectTimeout(timeOut);
                        con.setRequestMethod("POST");
                        con.setDoInput(true);
                        con.setDoOutput(true);
                        OutputStream os = con.getOutputStream();
                        os.write(getPostDataString(postParams).getBytes());
                        os.flush();
                        os.close();

                        statusCode = con.getResponseCode();

                        if (statusCode == 200) {
                            is = con.getInputStream();
                            if (is != null)
                                responseString = convertInputStreamToString(is);
                            status = true;
                            if (responseLog && responseString != null)
                                Log.d(TAG_RESPONSE, "Response :- " + responseString);
                        } else if (statusCode == 302) {
                            is = con.getInputStream();
                            if (is != null)
                                responseString = convertInputStreamToString(is);
                            status = true;
                            if (responseLog && responseString != null)
                                Log.d(TAG_RESPONSE, "Response :- " + responseString);
                        } else if (statusCode == 500 || statusCode == 401 || statusCode == 203) {
//                            Toast.makeText(context,"",Toast.LENGTH_SHORT).show();
                            status = true;
                            is = con.getErrorStream();
                            if (is != null)
                                responseString = convertInputStreamToString(is);
                            if (errorLog)
                                Log.d(TAG_RESPONSE, "Response :- " + "Error: Unable to connect to server." + statusCode);
                            if (responseLog && responseString != null)
                                Log.d(TAG_RESPONSE, "Response :- " + responseString);

                        }
                    } catch (java.net.SocketTimeoutException e) {
                        status = false;
                        if (errorLog)
                            Log.d(TAG_RESPONSE, "Response :- " + "Error: Connection timed out.");
                        responseString = "Connection timed out. Please try again later.";

                    } catch (Exception ex) {
                        status = false;
                        if (errorLog)
                            Log.d(TAG_RESPONSE, "Response :- " + "Error: " + ex.getLocalizedMessage());
                    } finally {
                        try {
                            if (is != null) {
                                is.close();
                            }
                        } catch (IOException ex) {
                            status = false;
                            if (errorLog)
                                Log.d(TAG_RESPONSE, "Response :- " + "Error: " + ex.getLocalizedMessage());
                        }
                    }

                    Handler mainHandler = new Handler(context.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (status) {
                                if (responseString != null && !responseString.isEmpty())
                                    responseListener.onResponse(tag, responseString);
                            } else {
                                if (responseString != null && !responseString.isEmpty())
                                    errorListener.onError(tag, responseString, statusCode);
                                else
                                    errorListener.onError(tag, API_ERROR, statusCode);
                            }
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            };

            handler.sendEmptyMessage(0);
        } else {
            errorListener.onError(tag, INTERNET_CONNECTION_ERROR, statusCode);
        }
        return this;
    }


    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder result = null;
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            result = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                result.append(line).append('\n');
            }
            return result.toString();
        } catch (Exception ex) {
            return "";
        }

    }

    public String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public interface ResponseListener {
        void onResponse(String tag, String response);
    }

    public interface ErrorListener {
        void onError(String tag, String errorMsg, long statusCode);
    }
}
