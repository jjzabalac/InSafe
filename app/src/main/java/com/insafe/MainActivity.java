package com.insafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "LEDER";

    DatabaseReference updater;
    DatabaseReference count;
    DatabaseReference data;

    WebView myWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWebView = findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("file:///android_asset/index.html");

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        updater = database.getReference("updater");
        updater.setValue(0);

        data = database.getReference("logs");
        count = database.getReference("count");

        myWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {

                data.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String Json = "";

                        for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                            String data = (String) messageSnapshot.getValue();

                            try {

                                JSONArray jsonArray = new JSONArray(data);
                                JSONObject jsonObject1 = jsonArray.getJSONObject(0);

                                String value1 = jsonObject1.optString("count");
                                String value2 = jsonObject1.optString("time");

                                String array = "{\"count\":"+value1+",\"time\":"+ value2 +"},";

                                Json = Json + array;

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        Json = method(Json);

                        Log.d("TAG", Json);
                        myWebView.loadUrl("javascript:updateChart([" + Json + "])");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Failed to read value.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to read value.", error.toException());
                    }
                });

                count.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Integer value = dataSnapshot.getValue(Integer.class);
                        myWebView.loadUrl("javascript:updateConunt("+value+")");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Failed to read value.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to read value.", error.toException());
                    }
                });
            }
        });
    }

    // Elimina el ultimo caracter de una cadena
    public String method(String str) {
        return str.substring(1,str.length()-1);
    }
}
