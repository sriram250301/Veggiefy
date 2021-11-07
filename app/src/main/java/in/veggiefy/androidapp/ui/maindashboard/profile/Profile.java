package in.veggiefy.androidapp.ui.maindashboard.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.ui.login.LoginActivity;

public class Profile extends AppCompatActivity {


    //VIEWS
    //personal info
    TextView nameText,phoneText,emailText;
    //business info
    TextView businessNameText,businessTypeText,addressText;
    ImageView shopImage;
    ImageView backIcon;
    Button deleteAccountButton;
    ProgressDialog progressDialog;
    FloatingActionButton fab;

    //VARIABLES personal info
    String name,email,phone,password,imageUrl;
    //VARIABLES business info
    String businessName,businessType,address;
    boolean isConnected;

    //Database References
    DatabaseReference ordersRef;
    ValueEventListener ordersRefListener;
    DatabaseReference userRef;
    FirebaseAuth mAuth;
    FirebaseStorage firebaseStorage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getHooks();
        getSharedPreferenceData();

        //SET TEXFIELDS
        businessNameText.setText(businessName);
        businessTypeText.setText(businessType);
        addressText.setText(address);

        //SET TEXFIELDS
        nameText.setText(name);
        phoneText.setText(phone);
        emailText.setText(email);
        Glide.with(getApplicationContext()).load(imageUrl).placeholder(R.mipmap.placeholder_png_foreground).into(shopImage);


        mAuth= FirebaseAuth.getInstance();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(Profile.this,ProfileEdit.class);
                startActivity(intent);
            }
        });
        //delete account clicked
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Profile.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Please confirm")
                    .setMessage("Are you sure you want to delete your account ?")
                    .setPositiveButton("YES",new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            initiateDeletionProcess();
                        }

                    })
                        .setNegativeButton("NO",null)
                    .show();

            }
        });

        //back icon clicked
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initiateDeletionProcess() {
        //Set ProgressDialog
        progressDialog=new ProgressDialog(Profile.this);
        //show progressDialog
        progressDialog.show();
        //set contentView for progressDialog
        progressDialog.setContentView(R.layout.progress_dialog);
        //set transparent background
        progressDialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent
        );
        if(isConnected()){

            pendingOrdersExist();
            Log.d("TAG", " just after callling pendingorderex..()");

        }
        else{
            progressDialog.dismiss();
            Toast.makeText(Profile.this, "Connection error ,Please try later", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        SharedPreferences sharedPreferences=this.getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        SharedPreferences cartsharedPreferences=this.getSharedPreferences("in.veggiefy.androidapp.cart", Context.MODE_PRIVATE);
        //Editor
        SharedPreferences.Editor editor = sharedPreferences.edit();
        SharedPreferences.Editor carteditor = cartsharedPreferences.edit();

        editor.putBoolean("LOG_STATE",false);
        carteditor.clear();
        editor.apply();
        carteditor.apply();
    }

    private void removeInDatabase() {
        userRef=FirebaseInit.getDatabase().getReference("USERS/"+phone);
        Log.d("TAG", "removeInDatabase: PHONE::"+phone);
        userRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                       @Override
                                                       public void onSuccess(Void aVoid) {
                                                           Toast.makeText(Profile.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                                           logout();
                                                           progressDialog.dismiss();
                                                       }
                                                   });

    }

    private void pendingOrdersExist() {

        Log.d("TAG", "pendingOrdersExist: Inside start of function");
        ordersRef= FirebaseInit.getDatabase().getReference("USERS/"+phone+"/orders");
        ordersRefListener=ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean pending=false;
                Log.d("TAG", "onDataChange: PENDING ORDERS ORDERS SNAP ::"+snapshot.toString());
                if(snapshot.exists()){

                    for(DataSnapshot childrensnapshot:snapshot.getChildren()){
                        Log.d("TAG", "onDataChange: ORDER CHILDREN SNAPSHOT::"+childrensnapshot.toString());
                        Log.d("TAG", "onDataChange: PAYSTATUS VALUE ::"+childrensnapshot.child("details").child("paystatus").getValue(Boolean.class));
                        Log.d("TAG", "onDataChange: PAYSTATUS VALUE ::"+childrensnapshot.child("details").child("orderclosed").getValue(Boolean.class));
                        if(!childrensnapshot.child("details").child("paystatus").getValue(Boolean.class) || childrensnapshot.child("details").child("orderclosed").getValue(Boolean.class) ){
                            pending=true;
                            Log.d("TAG", "onDataChange: PENDING TRUE CONDITION!"+pending);
                        }
                    }
                }
                else{
                    ordersRef.removeEventListener(ordersRefListener);
                    removeInDatabase();
                    mAuth.signOut();
                    progressDialog.dismiss();
                    Intent intent=new Intent(Profile.this, LoginActivity.class);
                    startActivity(intent);
                }
                if(pending){
                    ordersRef.removeEventListener(ordersRefListener);
                    Log.d("TAG", "initiateDeletionProcess: PENDING ORDERS PRESENT!");
                    progressDialog.dismiss();
                    new AlertDialog.Builder(Profile.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Cannot delete account !")
                            .setMessage("It seems there are pending or unpaid orders in your account , Please contact Veggiefy support in Help & Support menu to delete account")
                            .setPositiveButton("OK", null)
                            .show();
                }
                else{
                    ordersRef.removeEventListener(ordersRefListener);
                    removeInDatabase();
                    //delete image
                    StorageReference photoRef = firebaseStorage.getReferenceFromUrl(imageUrl);
                    photoRef.delete();
                    progressDialog.dismiss();
                    Intent intent=new Intent(Profile.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void getHooks() {
        //HOOKS   (PERSONAL INFO)
        nameText=findViewById(R.id.name_textView);
        phoneText=findViewById(R.id.phone_textView);
        emailText=findViewById(R.id.email_textView);
        //HOOKS  (BUSINESS INFO)
        businessNameText=findViewById(R.id.business_name_textView);
        businessTypeText=findViewById(R.id.business_type_textView);
        addressText=findViewById(R.id.address_textView);
        deleteAccountButton=findViewById(R.id.delete_account_button);
        shopImage=findViewById(R.id.shop_image_profile_imageView);
        backIcon=findViewById(R.id.back_icon_profile_imageView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
    }

    private void getSharedPreferenceData() {
        SharedPreferences sharedPreferences=this.getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        //Get values personal info
        name=sharedPreferences.getString("NAME","no value");
        phone=sharedPreferences.getString("PHONE","no value");
        email=sharedPreferences.getString("EMAIL","no value");
        password=sharedPreferences.getString("PASSWORD","no value");
        //Get values for business info
        businessName=sharedPreferences.getString("BUSINESS_NAME","no value");
        businessType=sharedPreferences.getString("BUSINESS_TYPE","no value");
        address=sharedPreferences.getString("ADDRESS","no value");
        imageUrl=sharedPreferences.getString("IMAGEURL",null);
    }

    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(Profile profile) {
        ConnectivityManager connectivityManager=(ConnectivityManager) profile.getSystemService(Context.CONNECTIVITY_SERVICE);

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
}