package com.pookidevs.karavan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private final int masterPort = 1234;
    public static String masterIP;
    public static String slaveIP;
    public static Socket socket;
    public static DataInputStream in;
    public static DataOutputStream out;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button makeConnect = (Button) findViewById(R.id.makeConnect);
        makeConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et_master = (EditText) findViewById(R.id.et_masterip);
                EditText et_slave = (EditText) findViewById(R.id.et_slaveip);
                masterIP = et_master.getText().toString();
                slaveIP = et_master.getText().toString();
                if(masterIP.equals("") || slaveIP.equals("")){
                    Toast.makeText(getApplicationContext(),"Please enter valid IP addresses",Toast.LENGTH_SHORT).show();
                    return;
                }
                Thread conn = new Thread(new Connection());
                conn.start();
                Intent activeActivity = new Intent(MainActivity.this, ActiveActivity.class);
                startActivity(activeActivity);
//                Intent intent = getIntent();
//                finish();
//                startActivity(intent);
//                Toast.makeText(getApplicationContext(),"Connecton failed. Try again!!!",Toast.LENGTH_LONG).show();
            }
        });

    }

    class Connection implements Runnable {

        @Override
        public void run() {

            try {
                //get address
                InetAddress serverAddr = InetAddress.getByName(masterIP);
                //connect to master car
                socket = new Socket(serverAddr, masterPort);
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());
                ActiveActivity.status = 0;
            } catch (UnknownHostException e1) {
                ActiveActivity.status = -1;
                e1.printStackTrace();
            } catch (IOException e1) {
                ActiveActivity.status = -1;
                e1.printStackTrace();
            } catch (Exception e){
                ActiveActivity.status = -1;
                e.printStackTrace();
            }


        }

    }
}
