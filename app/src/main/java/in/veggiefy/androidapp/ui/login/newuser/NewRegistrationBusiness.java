package in.veggiefy.androidapp.ui.login.newuser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;

public class NewRegistrationBusiness extends AppCompatActivity {


    public Button finishButton;
    public Spinner spinner;
    public EditText businessNameEditText;
    public ImageView image;
    public Button imageButton;
    public ProgressDialog progressDialog;
    //VARIABLES from bundle
    String address,hashcode,DC_KEY;
    Double lat,lang;
    String phone,name,email,password;
    //VARIABLES for current activity
    public String businessName;
    public String businessType;
    public String imageUrl;
    public String DEPOT_KEY;
    public Uri imageUri;
    public Boolean valid=false,imagechosen=false,imageUploaded=false,dataUploaded=false;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    public DatabaseReference databaseReference;
    public DatabaseReference dbDepotRef;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_registration_business);

        //HOOKS
        finishButton = findViewById(R.id.next_page_button2);
        businessNameEditText=findViewById(R.id.business_name_editText);
        spinner =  findViewById(R.id.spinner);
        image=findViewById(R.id.shop_image);
        imageButton=findViewById(R.id.shop_image_button);
        //firebase hooks
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();



        //SPINNER
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewRegistrationBusiness.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.vendor_type));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //GET VARIABLES FROM BUNDLE
        Bundle bundle=getIntent().getExtras();
        phone =bundle.getString("Phone");
        name=bundle.getString("Name");
        email=bundle.getString("Email");
        address=bundle.getString("Address");
        hashcode=bundle.getString("Hashcode");
        DC_KEY=bundle.getString("DCKey");
        lat=bundle.getDouble("Lat");
        lang=bundle.getDouble("Lang");
        

        //CLICKED ON IMAGE BUTTON
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //FINISH BUTTON CLICKED
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Set ProgressDialog
                progressDialog=new ProgressDialog(NewRegistrationBusiness.this);
                //show progressDialog
                progressDialog.show();
                //set contentView for progressDialog
                progressDialog.setContentView(R.layout.progress_dialog);
                //set transparent background
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                validateFields();

            }
        });

    }



    private void chooseImage() {
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri=data.getData();
            image.setImageURI(imageUri);
            imagechosen=true;
        }
    }

    private boolean validateFields() {

        Log.d("INSIDE------>", "validate fields: ");
        businessName=businessNameEditText.getText().toString().trim();
        businessType=spinner.getSelectedItem().toString();
        if(businessName.equals(""))
        {
            Toast.makeText(this, "Name can't be empty", Toast.LENGTH_SHORT).show();
        }
        else{
            if(businessType.equals("Choose your Business Category")){
                Toast.makeText(this, "Choose your business type", Toast.LENGTH_SHORT).show();
            }
            else if(!imagechosen){
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
            else
            {
                valid=true;
                uploadImage();
            }
        }

        return valid;
    }

    private boolean uploadImage() {


        Log.d("INSIDE------>", "uploadIMAGE: ");
        final String filePath=phone+"shopimage";
        StorageReference shopImageRef = storageReference.child("shopimages/"+filePath);
        UploadTask uploadTask = shopImageRef.putFile(imageUri);
        Log.d("INSIDE------>", "file PUT!");
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(NewRegistrationBusiness.this, "Umm..something went wrong", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageUploaded=true;
                Log.d("INSIDE------>", "uploadIMAGE::UPLOAD SUCCESS");

                shopImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        imageUrl=uri.toString();
                        uploadDataToFirebase();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        new AlertDialog.Builder(NewRegistrationBusiness.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Please confirm")
                                .setMessage("You might not be able to change your phone number again")
                                .setPositiveButton("Yes, I understand",null)
                                .show();
                    }
                });


            }
        });
        return imageUploaded;
    }

    private boolean uploadDataToFirebase() {
        Log.d("INSIDE------>", "uploaddataTOFIREBASE: ");

        getDepotKey();

        //Firebase reference

        databaseReference= FirebaseInit.getDatabase().getReference().child("USERS").child(phone);
        //set personal details
        databaseReference.child("profile").child("personal").child("name").setValue(name);
        databaseReference.child("profile").child("personal").child("email").setValue(email);
        databaseReference.child("profile").child("personal").child("phone").setValue(phone);
        databaseReference.child("profile").child("personal").child("dckey").setValue(DC_KEY);
        databaseReference.child("profile").child("personal").child("depotkey").setValue(DEPOT_KEY);
        //set business details
        databaseReference.child("profile").child("business").child("name").setValue(businessName);
        databaseReference.child("profile").child("business").child("type").setValue(businessType);
        databaseReference.child("profile").child("business").child("address").setValue(address);
        databaseReference.child("profile").child("business").child("image").setValue(imageUrl);
        databaseReference.child("profile").child("business").child("location").child("g").setValue(hashcode);
        databaseReference.child("profile").child("business").child("location").child("l").child("0").setValue(lat);
        databaseReference.child("profile").child("business").child("location").child("l").child("1").setValue(lang).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(NewRegistrationBusiness.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                dataUploaded=true;
                Log.d("INSIDE------>", "uploaddataTOFIREBASE: SUCCESS!!!!");

                storeInSharedPreference();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewRegistrationBusiness.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
            }
        });

        return dataUploaded;
    }

    private void storeInSharedPreference() {

        //Instantiate
        SharedPreferences sharedPreferences=getSharedPreferences("in.veggiefy.androidapp.userdetails", Context.MODE_PRIVATE);
        //Editor
        SharedPreferences.Editor editor;
        editor = sharedPreferences.edit();
        //STRING
        editor.putString("NAME",name);
        editor.putString("PHONE",phone);
        editor.putString("EMAIL",email);
        editor.putString("PASSWORD",password);
        editor.putString("BUSINESS_NAME",businessName);
        editor.putString("BUSINESS_TYPE",businessType);
        editor.putString("ADDRESS",address);
        editor.putString("HASHCODE",hashcode);
        editor.putString("DCKEY",DC_KEY);

        editor.putString("DEPOTKEY",DEPOT_KEY);
        editor.putString("IMAGEURL",imageUrl);
        //DOUBLE
        editor.putString("LAT",lat.toString());
        editor.putString("LANG",lang.toString());
        //BOOLEAN
        editor.putBoolean("LOG_STATE",true);
        //save
        editor.apply();
        Log.d("SharedPreference","Phone::"+sharedPreferences.getString("PHONE",null));
        Log.d("SharedPreference","Name::"+sharedPreferences.getString("NAME",null));
        Log.d("SharedPreference","LATITUDE::"+Double.valueOf(sharedPreferences.getString("LAT",null)));
        Log.d("SharedPreference","LONGITUDE::"+Double.valueOf(sharedPreferences.getString("LANG",null)));


        //Exit Activity
        progressDialog.dismiss();
        Intent intent = new Intent(NewRegistrationBusiness.this, Registered.class);
        intent.putExtra("orderplaced",false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void getDepotKey() {
        //GET CORRESPONDING DEPOT KEY

        dbDepotRef= FirebaseInit.getDatabase().getReference().child("MARKET/DISTRIBUTION_CENTRES/"+DC_KEY);
        dbDepotRef.keepSynced(true);

        dbDepotRef.child("details").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DEPOT_KEY=snapshot.child("depot").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NewRegistrationBusiness.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Go back?")
                .setMessage("Do you want to change the selected location?")
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