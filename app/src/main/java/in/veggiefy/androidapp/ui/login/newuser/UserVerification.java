package in.veggiefy.androidapp.ui.login.newuser;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

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
import in.veggiefy.androidapp.ui.login.existinguser.ExistingUserSignIn;

import static android.widget.Toast.LENGTH_LONG;

public class UserVerification extends AppCompatActivity implements View.OnClickListener  {

    public Button Verify;
    public TextView otpsentText;
    EditText OtpEditText;
    String phonenumber;
    String codebysystem;
    ProgressDialog progressDialog;

    //Firebase Variables
    DatabaseReference dbRef;

    FirebaseAuth mAuth;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_verification);

        //HOOKS
        otpsentText=findViewById(R.id.otp_sent_number_textview);
        Verify=(Button) findViewById(R.id.verify);
        OtpEditText=findViewById(R.id.otp_edittext);

        //VARIABLES
        Bundle bundle=getIntent().getExtras();
        phonenumber=bundle.getString("Phone");

        //SET OTP SENT TO PHONE NUMBER TEXT
        Typeface typeface = ResourcesCompat.getFont(this, R.font.sniglet);
        otpsentText.setTypeface(typeface);
        otpsentText.setText(String.format("Sending OTP to %s", phonenumber));

        //firebase instance
        mAuth=FirebaseAuth.getInstance();

        //send OTP
        sendVerificationCodeToUser(phonenumber);

        Verify.setOnClickListener(this);
        }


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.verify:{

                String codeByUser = OtpEditText.getText().toString().trim();
                if (!codeByUser.equals("")) {
                    verifyCode(codeByUser);
                }
                else
                {
                    Toast.makeText(UserVerification.this,"Code incorrect", Toast.LENGTH_LONG).show();
                }
                break;

            }

        }
    }
    //Code to send OTP message
    void sendVerificationCodeToUser(String phoneNo) {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+phoneNo)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(UserVerification.this)                 // Activity (for callback binding)
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
                    Toast.makeText(UserVerification.this, e.getMessage(), Toast.LENGTH_LONG).show();

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
                .addOnCompleteListener(UserVerification.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {



                            //Set ProgressDialog
                            progressDialog=new ProgressDialog(UserVerification.this);
                                //show progressDialog
                                progressDialog.show();
                                //set contentView for progressDialog
                                progressDialog.setContentView(R.layout.progress_dialog);
                                //set transparent background
                                progressDialog.getWindow().setBackgroundDrawableResource(
                                        android.R.color.transparent
                                );

                            //Check if account exists already..checking in database
                            checkIfUserAlreadyExists();



                        } else {
                            Toast.makeText(UserVerification.this, "Incorrect code..", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkIfUserAlreadyExists() {



            dbRef= FirebaseInit.getDatabase().getReference();
            dbRef.child("USERS").child(phonenumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    progressDialog.dismiss();
                    //Call SignUp Activity
                    Intent intent=new Intent(UserVerification.this, ExistingUserSignIn.class);
                    Toast.makeText(UserVerification.this, "Account already exists, Please sign in", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();

                } else {


                    progressDialog.dismiss();
                    //Toast
                    Toast.makeText(UserVerification.this, "Verified successfully!", Toast.LENGTH_LONG).show();
                    //Call SignUp Activity
                    Intent intent=new Intent(UserVerification.this, NewRegistrationPersonal.class);
                    intent.putExtra("Phone",phonenumber);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Oops,something went wrong", LENGTH_LONG).show();
            }
        });

    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Leave?")
                .setMessage("Are you sure you want to stop the registration process?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

}