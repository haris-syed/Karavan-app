package com.pookidevs.karavan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ActiveActivity extends AppCompatActivity {

    Button b1, b2, b3, b4;
    ImageView trackImage, marker1, marker2, marker3, marker4;
    /*
    * -1 = disconnected
    * 0 = idle
    * 1 = running
    * */
    public static int status;
    /*
     * 0 = unknown
     * 1 = marker 1
     * 2 = marker 2
     * 3 = marker 3
     * 4 = marker 4
     * */
    public static int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active);
        b1 = findViewById(R.id.button);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        trackImage = findViewById(R.id.img_track);
        marker1 = findViewById(R.id.marker1);
        marker2 = findViewById(R.id.marker2);
        marker3 = findViewById(R.id.marker3);
        marker4 = findViewById(R.id.marker4);
        //status = 0;
        position = 0;
        Thread statusMonitor = new Thread(new statusMonitor());
        statusMonitor.start();
        //runOnUiThread(statusMonitor);
    }

    public void reConnect(View v) {
        try {
            MainActivity.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        Intent mainIntent = new Intent(ActiveActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }


    //called when a location button is pressed
    public void setDestination(View v){
        switch (v.getId()){
            //#88FF2929 = red
            //#882979FF =blue
            case R.id.button:
                disableButtons();
                b1.setBackgroundColor(Color.parseColor("#88FF2929"));
                Thread t = new Thread ( new goToLocation(1));
                t.start();
                break;
            case R.id.button2:
                disableButtons();
                b2.setBackgroundColor(Color.parseColor("#88FF2929"));
                t = new Thread ( new goToLocation(2));
                t.start();
                break;
            case R.id.button3:
                disableButtons();
                b3.setBackgroundColor(Color.parseColor("#88FF2929"));
                t = new Thread ( new goToLocation(3));
                t.start();
                break;
            case R.id.button4:
                disableButtons();
                b4.setBackgroundColor(Color.parseColor("#88FF2929"));
                t = new Thread ( new goToLocation(4));
                t.start();
                break;
        }
    }

    //functions to enable/disable buttons

    private void disableButtons(){
        b1.setEnabled(false);
        b2.setEnabled(false);
        b3.setEnabled(false);
        b4.setEnabled(false);
    }

    private void enableButtons(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                b1.setEnabled(true);
                b2.setEnabled(true);
                b3.setEnabled(true);
                b4.setEnabled(true);
                b1.setBackgroundColor(Color.parseColor("#882979FF"));
                b2.setBackgroundColor(Color.parseColor("#882979FF"));
                b3.setBackgroundColor(Color.parseColor("#882979FF"));
                b4.setBackgroundColor(Color.parseColor("#882979FF"));
            }
        });
    }

    class goToLocation implements Runnable{

        private int location;

        goToLocation(int location){
            this.location=location;
        }

        @Override
        public void run() {
            try {
                MainActivity.conn.join();// wait for connection thread to finish
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(ActiveActivity.status == -1){
                //not connected so return
                return;
            }
            Socket s = MainActivity.socket;
            BufferedReader in = (MainActivity.in);
            DataOutputStream out = MainActivity.out;
            try {
                out.writeUTF("L" + location);
                out.writeUTF(MainActivity.slaveIP);
            }
            catch (IOException e){
                e.printStackTrace();
            }

            String response = "";
            while (!response.equals("end")) {
                try {
                    response = in.readLine();
                    if(response == null){ //means the server died
                        ActiveActivity.status = -1;
                        ActiveActivity.position = 0;
                        response="";
                        System.err.println("Server died");
                        break;
                    }
                    if(response.equals("start")){
                        ActiveActivity.status = 1;
                    }
                    else if (response.equals("p1")){
                        ActiveActivity.position = 1;
                    }
                    else if (response.equals("p2")){
                        ActiveActivity.position = 2;
                    }
                    else if (response.equals("p3")){
                        ActiveActivity.position = 3;
                    }
                    else if (response.equals("p4")){
                        ActiveActivity.position = 4;
                    }
                    else if (response.equals("end")){
                        enableButtons();
                        ActiveActivity.status = 0;
                    }
                    System.out.println(response);
                } catch (IOException e) {
                    e.printStackTrace();
                    ActiveActivity.status = -1;
                    break;
                }
            }
        }
    }

    class statusMonitor implements Runnable{

        @Override
        public void run() {
            int prevStatus = -2;
            int prevPosition = 0;
            while(true){
                try {
                    //only call if changed (to reduce updates)
                    if(ActiveActivity.status != prevStatus || ActiveActivity.position!=prevPosition){
                        displayStatusChange();
                        prevStatus = ActiveActivity.status;
                        prevPosition = ActiveActivity.position;
                    }
                    Thread.sleep(1000); //sleep for 1 sec
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void disableMarkers(){
            marker1.setVisibility(View.INVISIBLE);
            marker2.setVisibility(View.INVISIBLE);
            marker3.setVisibility(View.INVISIBLE);
            marker4.setVisibility(View.INVISIBLE);
        }

        private void displayStatusChange() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String st_text = "";
                    int color = 0;
                    if(ActiveActivity.status == -1){
                        st_text = "System Status: Disconnected";
                        color = getResources().getColor(R.color.red);
                    }
                    else if(ActiveActivity.status == 0){
                        st_text = "System Status: Idle";
                        color = getResources().getColor(R.color.orange);
                    }
                    else if(ActiveActivity.status == 1){
                        st_text = "System Status: Running";
                        color = getResources().getColor(R.color.green);
                    }
                    TextView tv_status = findViewById(R.id.tv_status);
                    TextView tv_position = findViewById(R.id.tv_position);
                    tv_status.setText(st_text);
                    tv_status.setBackgroundColor(color);
                    tv_position.setText("Current position: "+ (ActiveActivity.position == 0 ? "Unknown":ActiveActivity.position));
                    if(ActiveActivity.position == 1){
                        disableMarkers();
                        marker1.setVisibility(View.VISIBLE);
                    }
                    else if(ActiveActivity.position == 2){
                        disableMarkers();
                        marker2.setVisibility(View.VISIBLE);
                    }
                    else if(ActiveActivity.position == 3){
                        disableMarkers();
                        marker3.setVisibility(View.VISIBLE);
                    }
                    else if(ActiveActivity.position == 4){
                        disableMarkers();
                        marker4.setVisibility(View.VISIBLE);
                    }
//                    trackImage.setVisibility(View.INVISIBLE);
//                    trackImage.setVisibility(View.VISIBLE);
                }
            });

        }
    }


}
