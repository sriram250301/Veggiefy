package in.veggiefy.androidapp.ui.logout;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.ui.login.LoginActivity;
import in.veggiefy.androidapp.ui.maindashboard.Dashboard;


public class Logout extends AppCompatActivity {

    Button yesButton;
    Button noButton;
    boolean isConnected=false;
    String phonenumber;
    DatabaseReference cartRef;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);


        //HOOKS
        yesButton=findViewById(R.id.yes_button);
        noButton=findViewById(R.id.no_button);


        //SHARED PREFERENCE
        SharedPreferences sharedPreferences=this.getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        phonenumber=sharedPreferences.getString("PHONE",null);

        mAuth= FirebaseAuth.getInstance();

        //CLICK LISTENER
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //SET LOG_STATE IN SHARED PREF  and   CLEARING CART
                if(isConnected()){
                    clearCartInDatabase();
                    mAuth.signOut();
                }
                else{
                    Toast.makeText(Logout.this, "No network,unable to log out", Toast.LENGTH_SHORT).show();
                }

            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Logout.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(Logout logout) {
        ConnectivityManager connectivityManager=(ConnectivityManager) logout.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if((wifiConn !=null && wifiConn.isConnected())|| (mobileConn!=null && mobileConn.isConnected())){
            isConnected=true;
        }
        else{
            isConnected=false;
        }
        return isConnected;
    }

    private void clearCartInDatabase() {
        cartRef= FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/cart");
        cartRef.removeValue();
        logOut();
    }

    private void logOut() {
        SharedPreferences sharedPreferences=this.getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        SharedPreferences cartsharedPreferences=this.getSharedPreferences("in.veggiefy.androidapp.cart", Context.MODE_PRIVATE);
        //Editor
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences.Editor carteditor = cartsharedPreferences.edit();

        editor.putBoolean("LOG_STATE",false);
        carteditor.clear();
        editor.apply();
        carteditor.apply();
        Intent intent=new Intent(Logout.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}