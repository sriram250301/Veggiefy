package in.veggiefy.androidapp.ui.login.existinguser;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;

import static android.widget.Toast.LENGTH_LONG;

public class ExistingUserSignIn extends AppCompatActivity implements View.OnClickListener {

    public Button signIn;
    public EditText Phone;
    public EditText passwordEditText;
    public TextView forgotPasswordText;
    String passwordInDatabase;
    String passwordByUser;
    String phoneNumber;
    boolean isConnected=false;

    //Progress Dialog
    ProgressDialog progressDialog;
    //Firebase variables
    DatabaseReference dbRef;
    DatabaseReference userDbRef;
    DataSnapshot userSnashot;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_user_sign_in);

        //HOOKS
        signIn=findViewById(R.id.sign_in);
        Phone = findViewById(R.id.phone_edittext);

        //Sign In Clicked
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set ProgressDialog
                progressDialog=new ProgressDialog(ExistingUserSignIn.this);
                //show progressDialog
                progressDialog.show();
                //set contentView for progressDialog
                progressDialog.setContentView(R.layout.progress_dialog);
                //set transparent background
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                if(isValidMobile()){

                    String phonenumber = Phone.getText().toString().trim();
                    if(isConnected()) checkIfPhoneNumberExists(phonenumber);

                    else{
                        progressDialog.dismiss();
                        Toast.makeText(ExistingUserSignIn.this, "No connection", Toast.LENGTH_SHORT).show();
                    }


                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Incorrect phone number", LENGTH_LONG).show();
                }
            }
        });
    }
    public boolean isValidMobile() {
        String phone = Phone.getText().toString().trim();
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length()==10) {
                check=true;
            }
            else
                Toast.makeText(getApplicationContext(), "Incorrect phone number", LENGTH_LONG);

        }
        else
            Toast.makeText(getApplicationContext(), "Invalid phone number", LENGTH_LONG);
        return check;
    }

    private void checkIfPhoneNumberExists(String phonenumber) {
        phoneNumber=phonenumber;
        Log.d("TAG", "checkIfPhoneNumberExists: CHECK IF PH EXST"+phoneNumber);
        dbRef=FirebaseInit.getDatabase().getReference("USERS/"+phoneNumber);
        dbRef.keepSynced(true);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d("TAG", "onDataChange: SNAPSHOT"+userSnashot);
                    userSnashot=snapshot;
                    //
                    progressDialog.dismiss();
                    Intent intent = new Intent(ExistingUserSignIn.this, SignInVerication.class);
                    String phone = Phone.getText().toString();
                    intent.putExtra("Phone",phone);
                    startActivity(intent);
                    finish();

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ExistingUserSignIn.this, "Account doesn't exist!", LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error, please try later", LENGTH_LONG).show();
            }
        });
    }

    private boolean isConnected() {
        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(ExistingUserSignIn existingUserSignIn) {
        ConnectivityManager connectivityManager=(ConnectivityManager) existingUserSignIn.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn =connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        isConnected= (wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected());
        return isConnected;
    }
    @Override
    public void onClick(View v) {

    }
}