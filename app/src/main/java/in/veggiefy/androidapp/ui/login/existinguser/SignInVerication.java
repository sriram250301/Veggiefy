package in.veggiefy.androidapp.ui.login.existinguser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.ui.maindashboard.Dashboard;

import static android.widget.Toast.LENGTH_LONG;

public class SignInVerication extends AppCompatActivity {


    public Button Verify;
    public TextView otpsentText;
    EditText OtpEditText;
    String phonenumber;
    String codebysystem;
    ProgressDialog progressDialog;
    //variables to fetch from db
    String address,hashcode,DC_KEY,DEPOT_KEY;
    String lat,lang;
    String name,email;
    String businessName,businessType,imageUrl;
    boolean sentCode=false;
    //Firebase Variables
    DatabaseReference dbRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_verication);


        //HOOKS
        otpsentText=findViewById(R.id.otp_sent_number_signin_verification_textview);
        Verify=(Button) findViewById(R.id.sign_in_verify_button);
        OtpEditText=findViewById(R.id.otp_signin_verification_edittext);

        //VARIABLES
        Bundle bundle=getIntent().getExtras();
        phonenumber=bundle.getString("Phone");

        //SET OTP SENT TO PHONE NUMBER TEXT
        Typeface typeface = ResourcesCompat.getFont(this, R.font.sniglet);
        otpsentText.setTypeface(typeface);
        otpsentText.setText(String.format("Sending OTP to %s", phonenumber));

        //firebase instance
        mAuth= FirebaseAuth.getInstance();

        //send OTP
        sendVerificationCodeToUser(phonenumber);

        Verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String codeByUser = OtpEditText.getText().toString().trim();
                if (!codeByUser.equals("") && sentCode) {
                    verifyCode(codeByUser);
                }
                else
                {
                    Toast.makeText(SignInVerication.this,"Code incorrect", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Code to send OTP message
    void sendVerificationCodeToUser(String phoneNo) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+phoneNo)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(SignInVerication.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

    }
    //Call Back after phone auth
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    codebysystem = s;
                    sentCode=true;
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {

                        verifyCode(code);
                    }

                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(SignInVerication.this, e.getMessage(), Toast.LENGTH_LONG).show();

                }
            };

    //Code to verify the OTP
    void verifyCode(String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codebysystem, code);
        signInTheUserByCredentials(credential);

    }

    void signInTheUserByCredentials(PhoneAuthCredential credential) {


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignInVerication.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            //Set ProgressDialog
                            progressDialog=new ProgressDialog(SignInVerication.this);
                            //show progressDialog
                            progressDialog.show();
                            //set contentView for progressDialog
                            progressDialog.setContentView(R.layout.progress_dialog);
                            //set transparent background
                            progressDialog.getWindow().setBackgroundDrawableResource(
                                    android.R.color.transparent
                            );
                            getUserDataFromDatabase();

                        } else {
                            Toast.makeText(SignInVerication.this, "Incorrect code..", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void getUserDataFromDatabase() {
        Log.d("TAG", "getUserDataFromDatabase: BEFORE GETUSERDATA FROM DB");
        dbRef= FirebaseInit.getDatabase().getReference("USERS/"+phonenumber+"/profile");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnashot) {
                if (userSnashot.exists()) {
                    Log.d("TAG", "onDataChange: SNAPSHOT"+userSnashot);
                    name=userSnashot.child("personal").child("name").getValue(String.class);
                            email=userSnashot.child("personal").child("email").getValue(String.class);
                            DC_KEY=userSnashot.child("personal").child("dckey").getValue(String.class);
                            DEPOT_KEY=userSnashot.child("personal").child("depotkey").getValue(String.class);

                            //business data
                            businessName=userSnashot.child("business").child("name").getValue(String.class);
                            businessType=userSnashot.child("business").child("type").getValue(String.class);
                            imageUrl=userSnashot.child("business").child("image").getValue(String.class);
                            address=userSnashot.child("business").child("address").getValue(String.class);
                            hashcode=userSnashot.child("business").child("location").child("g").getValue(String.class);
                            Long latitude,longitude;
                            latitude=userSnashot.child("business").child("location").child("l").child("0").getValue(Long.class);
                            longitude=userSnashot.child("business").child("location").child("l").child("0").getValue(Long.class);
                            lat= String.valueOf(latitude);
                            lang= String.valueOf(longitude);
                            //
                            storeInSharedPreferences();

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Account doesn't exist!", LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void storeInSharedPreferences() {

        //Instantiate
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        //Editor
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();
        //STRING
        editor.putString("NAME",name);
        editor.putString("PHONE",phonenumber);
        editor.putString("EMAIL",email);
        editor.putString("BUSINESS_NAME",businessName);
        editor.putString("BUSINESS_TYPE",businessType);
        editor.putString("ADDRESS",address);
        editor.putString("HASHCODE",hashcode);
        editor.putString("DCKEY",DC_KEY);

        editor.putString("DEPOTKEY",DEPOT_KEY);
        editor.putString("IMAGEURL",imageUrl);
        //DOUBLE
        editor.putString("LAT",lat);
        editor.putString("LANG",lang);
        //BOOLEAN
        editor.putBoolean("LOG_STATE",true);
        //save
        editor.apply();

        Log.d("SharedPreference","Phone::"+sharedPreferences.getString("PHONE",null));
        Log.d("SharedPreference","Name::"+sharedPreferences.getString("NAME",null));
        Log.d("SharedPreference","LATITUDE::"+Double.valueOf(sharedPreferences.getString("LAT",null)));
        Log.d("SharedPreference","LONGITUDE::"+Double.valueOf(sharedPreferences.getString("LANG",null)));

        progressDialog.dismiss();
        Log.d("TAG", "storeInSharedPreferences: INSIDE SAVEINSHARED PREF");

        Intent intent=new Intent(SignInVerication.this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(SignInVerication.this, "Signed in successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(SignInVerication.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Please wait")
                .setMessage("Just wait a moment while we are verifying your mobile")
                .setPositiveButton("OK",null)
                .show();
    }
}