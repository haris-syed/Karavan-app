package com.pookidevs.karavan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private final int masterPort = 1234; //port of master car server
    public static String masterIP; //master car ip address
    public static String slaveIP; //slave car ip address
    public static Socket socket; //socket reference
    public static BufferedReader in; //socket input stream
    public static DataOutputStream out; //socket output stream
    public static Thread conn; //Thread reference for making socket connection


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button makeConnect = (Button) findViewById(R.id.makeConnect);
        makeConnect.setOnClickListener(new View.OnClickListener() {
            /*connect to server car when button is clicked */
            @Override
            public void onClick(View view) {
                EditText et_master = (EditText) findViewById(R.id.et_masterip);
                EditText et_slave = (EditText) findViewById(R.id.et_slaveip);
                masterIP = et_master.getText().toString(); //get ip for master
                slaveIP = et_slave.getText().toString(); //get ip for slave
                if(masterIP.equals("") || slaveIP.equals("")){ //check if not empty
                    Toast.makeText(getApplicationContext(),"Please enter valid IP addresses",Toast.LENGTH_SHORT).show();
                    return;
                }
                conn = new Thread(new Connection()); //new thread for making connection
                conn.start(); //start thread
                //switch to Active activity
                Intent activeActivity = new Intent(MainActivity.this, ActiveActivity.class);
                startActivity(activeActivity);
            }
        });

    }

    /* Thread class for making connection */
    class Connection implements Runnable {

        @Override
        public void run() {

            try {
                //get address
                InetAddress serverAddr = InetAddress.getByName(masterIP); //resolve address
                //connect to master car
                socket = new Socket(serverAddr, masterPort);// make socket
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //make input stream
                out = new DataOutputStream(socket.getOutputStream()); //make output stream
                ActiveActivity.status = 0; // if no exception was thrown set status to idle
            } catch (UnknownHostException e1) {
                ActiveActivity.status = -1; //set status to disconnected
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
