package in.veggiefy.androidapp.ui.login.newuser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;

public class ChooseLocation extends AppCompatActivity {

    TextView deliveryAddress;
    TextView unserviceableLocationText;
    Button selectFromMapButton;
    Button confirmButton;
    ProgressDialog progressDialog;
    //variables
    String address;
    String hashcode;
    String DCkey,DEPOT_KEY;
    Double lat;
    Double lang;
    Boolean available=false;
    public DatabaseReference dbDepotRef;
    ValueEventListener dbDepotRefListener;
    //variables in bundle from NewRegistrationPersonal Activity
    String phone,name,email,password;
    int PLACE_PICKER_REQUEST=1;
    double radius=3.5;
    boolean storedInSharedPref=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);

        //HOOKS
        deliveryAddress=findViewById(R.id.delivery_address_textView);
        unserviceableLocationText=findViewById(R.id.unserviceable_location_text);
        selectFromMapButton=findViewById(R.id.select_from_map_button);
        confirmButton=findViewById(R.id.confirm_button);

        //Recieve variables from bundle
        Bundle bundle=getIntent().getExtras();
        phone =bundle.getString("Phone");
        name=bundle.getString("Name");
        email=bundle.getString("Email");

        //deliveryAddress Text clicked..
        deliveryAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChooseLocation.this, "Please select from map..", Toast.LENGTH_SHORT).show();
            }
        });

        //SELECT_FROM_MAP button clicked
        selectFromMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    showPlacePicker();
            }
        });

        //CONFIRM button clicked
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Checking if User selected a location
                address=deliveryAddress.getText().toString().trim();
                if(address.equals("Delivery address"))
                {
                    Toast.makeText(ChooseLocation.this, "Please select a location from map", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(available){
//                        Toast.makeText(ChooseLocation.this, DCkey, Toast.LENGTH_SHORT).show();
                        if(bundle.getBoolean("CHANGING",false)){
                            String PHONE=bundle.getString("PHONE",null);
                            Log.d("TAG", "onClick: PHONE FROM BUNDLE::"+PHONE);
                            uploadInFirebase(PHONE);
                        }
                        else{
                            callNextActivity();
                        }
                    }
                    else
                    {
                        //Intimating user location is unserviceable
                        Typeface typeface = ResourcesCompat.getFont(ChooseLocation.this, R.font.sniglet);
                        unserviceableLocationText.setTypeface(typeface);
                        unserviceableLocationText.setText(R.string.unserviceable_location);
                    }
                }
            }
        });
    }



    //INVOKING PING PLACE PICKER
    private void showPlacePicker(){
        PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
        builder.setAndroidApiKey("AIzaSyApTssphFZSh0aZvLoET9hLhgB1PGBrZto")
                .setMapsApiKey("AIzaSyC8c6iMPOmgAuMpOMOD--2BDUsoQWtjlPg");

        // If you want to set a initial location rather then the current device location.
        // NOTE: enable_nearby_search MUST be true.
        builder.setLatLng(new LatLng(11.019867, 76.962593));
        available=false;
        try {
            Intent placeIntent = builder.build(ChooseLocation.this);
            startActivityForResult(placeIntent, PLACE_PICKER_REQUEST);
        }
        catch (Exception ex) {
            Toast.makeText(this,"Google Play unavailable in your device",Toast.LENGTH_LONG).show();
        }
    }

    //RETURNING TO THIS ACTIVITY AFTER SELECTING LOCATION
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PLACE_PICKER_REQUEST) && (resultCode == RESULT_OK)) {
            Place place = PingPlacePicker.getPlace(data);

            if (place != null) {

                //Set ProgressDialog
                progressDialog=new ProgressDialog(ChooseLocation.this);
                //show progressDialog
                progressDialog.show();
                //set contentView for progressDialog
                progressDialog.setContentView(R.layout.progress_dialog);
                //set transparent background
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );

                //setting progress dialog delay
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 3000);

                    address= String.valueOf(place.getAddress());

                    //Setting values for LATITUDE , LONGITUDE , ADDRESS
                    lat=  place.getLatLng().latitude;
                    lang= place.getLatLng().longitude;
                    //Set DeliveryAddress TextView
                    Typeface typeface = ResourcesCompat.getFont(this, R.font.sniglet);
                    deliveryAddress.setTypeface(typeface);
                    deliveryAddress.setText(address);

                    //Check if dc is nearby (3.5 and 5 km radius)
                    checkavailability(lat,lang);
                    if(!available){
                        radius=5;
                        checkavailability(lat,lang);

                    }
            }
        }
        else
        {
            Toast.makeText(this, "uh oh..something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    //CHECKING IF ANY DCs IS NEARBY THE SELECTED LOCATION
    private void checkavailability(Double lat, Double lang) {

        hashcode = GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lang));
        //Referencing database to query locations of DCs.
        DatabaseReference ref = FirebaseInit.getDatabase().getReference().child("MARKET/DISTRIBUTION_CENTRES/DC_locations");

        //Setting up Geoquery
        GeoFire geoFire = new GeoFire(ref);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lang), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {

            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //IF DC IS AVAILABLE
                available=true;
                DCkey=key;
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(ChooseLocation.this,"Something went wrong..please try later",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void callNextActivity() {
        Intent intent=new Intent(ChooseLocation.this,NewRegistrationBusiness.class);
        intent.putExtra("Name",name);
        intent.putExtra("Phone",phone);
        intent.putExtra("Email",email);
        intent.putExtra("Password",password);
        //data from this activity
        intent.putExtra("Address",address);
        intent.putExtra("Hashcode",hashcode);
        intent.putExtra("DCKey",DCkey);
        intent.putExtra("Lat",lat);
        intent.putExtra("Lang",lang);
        startActivity(intent);
        Log.d("VALUES----->", "Radius::"+radius+"Key::"+DCkey+"name::"+name+"Password::"+password+"Hash::"+hashcode);
    }




    //////////////////////////////////////////////////////         FUNCTIONS USED ONLY FOR EDITING        ///////////////////////////////////////////

    private void storeInSharedPreferences() {

        //Instantiate
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        //Editor
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();
        getDepotKey();
        editor.putString("ADDRESS",address);
        editor.putString("HASHCODE",hashcode);
        editor.putString("DCKEY",DCkey);
        editor.putString("DEPOTKEY",DEPOT_KEY);

        //DOUBLE
        editor.putString("LAT",lat.toString());
        editor.putString("LANG",lang.toString());
        editor.apply();
        //back to editing activity
        Log.d("TAG", "storeInSharedPreferences: SAVING IN SAVED PREF AND FINISHING");
        finish();
    }

    private void uploadInFirebase(String PHONE) {
        DatabaseReference databaseReference;
        Log.d("TAG", "uploadInFirebase: PHONE"+PHONE);
        databaseReference= FirebaseInit.getDatabase().getReference().child("USERS").child(PHONE);
        //set personal details

        databaseReference.child("profile/personal/dckey").setValue(DCkey);
        databaseReference.child("profile/personal/depotkey").setValue(DEPOT_KEY);
        //set business details

        databaseReference.child("profile/business/address").setValue(address);
        databaseReference.child("profile/business/location/g").setValue(hashcode);
        databaseReference.child("profile/business/location/l/0").setValue(lat);
        databaseReference.child("profile/business/location/l/1").setValue(lang).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

//                storeInSharedPreferences();
                getDepotKey();
                Log.d("TAG", "onSuccess: _________________________________SAVED IN DATABASE!!_______________________________________________________");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                unserviceableLocationText.setText("Someting went wrong on our side, Please try again later");
            }
        });

    }

    private void getDepotKey() {
        //GET CORRESPONDING DEPOT KEY

        if(!storedInSharedPref){
            dbDepotRef= FirebaseInit.getDatabase().getReference().child("MARKET/DISTRIBUTION_CENTRES/"+DCkey);

            dbDepotRefListener=dbDepotRef.child("details").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("TAG", "onDataChange: GET DEPOT KEY BEFORE SHRD PREF");
                    DEPOT_KEY=snapshot.child("depot").getValue(String.class);
                    storedInSharedPref=true;
                    storeInSharedPreferences();
                    if(dbDepotRefListener!=null){
                        dbDepotRef.removeEventListener(dbDepotRefListener);
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ChooseLocation.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
                    if(dbDepotRefListener!=null){
                        dbDepotRef.removeEventListener(dbDepotRefListener);
                    }
                }
            });
        }


    }

}