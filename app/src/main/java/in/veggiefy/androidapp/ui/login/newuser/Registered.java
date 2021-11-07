package in.veggiefy.androidapp.ui.login.newuser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import in.veggiefy.androidapp.R;

import in.veggiefy.androidapp.ui.maindashboard.Dashboard;

import java.util.Timer;
import java.util.TimerTask;
public class Registered extends AppCompatActivity {

    public Timer timer;
    TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);

        titleText=findViewById(R.id.registered_caption_textView);

        Bundle bundle=getIntent().getExtras();
        if(bundle.getBoolean("orderplaced")){
            titleText.setText("ORDER PLACED");
        }else{
            titleText.setText("REGISTERED SUCCESSFULLY");
        }

        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                Intent intent = new Intent(Registered.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        },3000);
    }
}