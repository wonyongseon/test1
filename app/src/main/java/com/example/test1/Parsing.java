package com.example.test1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

public class Parsing extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parsing);

        Button btn = (Button)findViewById(R.id.button_call);
        btn.setOnClickListener(this);
    }

    public static String encodeString(Properties params) {
        StringBuffer sb = new StringBuffer(256);
        Enumeration names = params.propertyNames();

        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = params.getProperty(name);
            sb.append(URLEncoder.encode(name) + "=" + URLEncoder.encode(value) );

            if (names.hasMoreElements()) sb.append("&");
        }
        return sb.toString();
    }

    // @Override
    public void onClick(View v) {

        EditText et_webpage_src = (EditText)findViewById(R.id.webpage_src);

        URL url = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream buf = null;

        try {

            url = new URL("http://203.249.119.183:8080/jsonTest.jsp");
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);

            urlConnection.setUseCaches(false);

            Toast.makeText(this, "aaa", Toast.LENGTH_SHORT).show();
            buf  = new BufferedInputStream(urlConnection.getInputStream());
            Toast.makeText(this, "bbb", Toast.LENGTH_SHORT).show();


            BufferedReader bufreader = new BufferedReader(new InputStreamReader(buf, "UTF-8"));

            String line  = null;
            String page  = "";

            while ((line = bufreader.readLine()) != null) {
                page += line;
            }

            JSONObject json = new JSONObject(page);

            JSONArray jArr = json.getJSONArray("DBLists");


            for (int i=0; i<jArr.length(); i++) {

                json = jArr.getJSONObject(i);

                String name    = "id   : " + json.getString("id");
                String address = "pass : " + json.getString("pass");

                et_webpage_src.append(name + "\n");
                et_webpage_src.append(address + "\n");
            }


        } catch (Exception e){
            et_webpage_src.setText(e.getMessage());
        } finally {
            urlConnection.disconnect();
        }
    }
}