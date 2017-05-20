package com.example.test1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText id, pass;
    Button btnSave, btnResult;
    loadJsp task;
    private static final int BTH_ENABLE_CODE = 789;//int 앞에는 메모리 관리 명시
    protected BluetoothAdapter bthAdapter;
    protected BluetoothRx bthRx;
    protected BluetoothSerialService bthService;
    protected Button btFind,btFindable,btConnect,btWrite,btRead;
    protected EditText edWrite;
    protected TextView txRead;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BTH_ENABLE_CODE && resultCode == RESULT_OK){
            Toast.makeText(this,"Bluetooth is enabled!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bthAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bthAdapter == null){
            Toast.makeText(this,"Bluetooth isn't supported!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bthAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BTH_ENABLE_CODE);
        }
        if (!bthAdapter.isEnabled()) return;//블루투스 연결됬는지 확인

        btFind = (Button)findViewById(R.id.btFind);//주변 블루투스 찾음
        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bthAdapter.isDiscovering()) bthAdapter.cancelDiscovery(); //누군가 다른앱에서 디스커버리 하고 있으면 취소시키고 내꺼를 동작시킴
                bthAdapter.startDiscovery();
            }
        });

        bthRx = new BluetoothRx("yongseon");//메모리 할당해서 불러옴,지정된 블루투스 연결
        IntentFilter intentFilter = new  IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bthRx, intentFilter);

        btConnect = (Button)findViewById(R.id.btConnect);
        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bthRx.sDeviceAddress.isEmpty()) {
                    BluetoothDevice device = bthAdapter.getRemoteDevice(bthRx.sDeviceAddress);
                    bthService.connect(device);
                }
            }
        });
        bthService = new BluetoothSerialService(this, bthAdapter);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnResult = (Button) findViewById(R.id.btnResult);

        id = (EditText) findViewById(R.id.aa);
        pass = (EditText) findViewById(R.id.bb);



        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                task = new loadJsp();
                task.execute();

            }
        });

        btnResult.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = (new Intent(getApplicationContext(), Parsing.class));
                startActivity(intent);
            }
        });

        txRead = (TextView) findViewById(R.id.txRead);
        btRead = (Button)findViewById(R.id.btRead);
        btRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = bthService.sReadBuffer;
                bthService.sReadBuffer = "";
                txRead.append(str);
            }
        });

        edWrite = (EditText) findViewById(R.id.edWrite);
        btWrite = (Button)findViewById(R.id.btWrite);
        btWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = edWrite.getText().toString();
                byte[] buf = new byte[1];
                for (int i = 0; i < str.length(); i++){
                    buf[0] = (byte)str.charAt(i);
                    bthService.write(buf);
                }
            }
        });
    }

    class loadJsp extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... param) {

            try {
                HttpClient client = new DefaultHttpClient();


                String postURL = "http://203.249.119.183:8080/dbInsert.jsp";

                HttpPost post = new HttpPost(postURL);

                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("id", id.getText().toString()));
                params.add(new BasicNameValuePair("pass", pass.getText().toString()));

                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);

                post.setEntity(ent);

                HttpResponse responsePOST = client.execute(post);
                HttpEntity resEntity = responsePOST.getEntity();

                if (resEntity != null) {
                    Log.i("RESPONSE", EntityUtils.toString(resEntity));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}