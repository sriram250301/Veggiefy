package in.veggiefy.androidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import in.veggiefy.androidapp.R;

import in.veggiefy.androidapp.ui.login.LoginActivity;
import in.veggiefy.androidapp.ui.maindashboard.Dashboard;

import java.util.Timer;
import java.util.TimerTask;

//import com.example.veggiefy.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity
{

    Timer timer;
    //Variables
    Boolean logStatus=false;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //CHECK LOGGED IN STATUS
        sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        logStatus=sharedPreferences.getBoolean("LOG_STATE",false);

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(logStatus){
                    Intent intent = new Intent(MainActivity.this, Dashboard.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        },3000);


    }



}
