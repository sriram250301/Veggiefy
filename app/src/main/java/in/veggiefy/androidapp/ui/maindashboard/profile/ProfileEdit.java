package in.veggiefy.androidapp.ui.maindashboard.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Pattern;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;
import in.veggiefy.androidapp.ui.login.newuser.ChooseLocation;

import static android.widget.Toast.LENGTH_LONG;

public class ProfileEdit extends AppCompatActivity {

    //VIEWS
    //personal info
    EditText nameText,emailText;
    //business info
    EditText businessNameText,businessTypeText;
    TextView addressText,phoneText;
    ImageView shopImage;
    Button saveButton;
    Button changeLocationButton;
    ProgressDialog progressDialog;
    public Spinner spinner2;
    //VARIABLES personal info
    String name,email,phone,password;
    //VARIABLES business info
    String businessName,businessType,address,imageUrl,DC_KEY,DEPOT_KEY,hashcode;
    Double LAT,LANG;
    public Uri imageUri;
    boolean isConnected=false,imagechosen=false,imageUploaded=false;
    boolean dataUploaded=false;
    int REQUEST_CODE=1;
    //
    DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        getHooks();
        getSharedPreferenceData();
        setFieldViews();

        //Image clicked
        shopImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //change location clicked
        changeLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(ProfileEdit.this, ChooseLocation.class);
                intent.putExtra("CHANGING",true);
                intent.putExtra("PHONE",phone);
                startActivityForResult(intent,REQUEST_CODE);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set ProgressDialog
                progressDialog=new ProgressDialog(ProfileEdit.this);
                //show progressDialog
                progressDialog.show();
                //set contentView for progressDialog
                progressDialog.setContentView(R.layout.progress_dialog);
                //set transparent background
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent);
                getDataFromViews();
                if(!emptyFields()){
                    if(validateFields() ){
                        if(isConnected())
                        {
                            uploadDataToFirebase();
                        }
                        else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(ProfileEdit.this, "Please check your connection", Toast.LENGTH_SHORT).show();
                            }
                    }
                    else progressDialog.dismiss();
                }
                else progressDialog.dismiss();
            }
        });

    }

    private boolean isConnected() {

        return checkConnectionStatus(this);
    }

    private boolean checkConnectionStatus(ProfileEdit profileEdit) {
        ConnectivityManager connectivityManager=(ConnectivityManager) profileEdit.getSystemService(Context.CONNECTIVITY_SERVICE);

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

    private boolean emptyFields() {
        boolean empty=false;
//        password=passwordEditText.getText().toString().trim();

        if(name.equals("") || email.equals("") || password.equals("") || businessName.equals(""))
        {
            Toast.makeText(this, "Fields cant be empty", Toast.LENGTH_SHORT).show();
            empty=true;
        }
        return empty;
    }

    private boolean validateFields() {
        boolean valid=false;
        if(!isValidEmail(email)){
            Toast.makeText(this, "Incorrect email", Toast.LENGTH_SHORT).show();
        }
        else{
            if(isValidMobile()){
                Log.d("TAG", "validateFields: BUSINESS CATEGORY::"+businessType);
                if(!businessType.equals("Choose your Business Category")){
                    valid=true;
                }
                else {
                    Toast.makeText(this, "Choose your business category", Toast.LENGTH_SHORT).show();
                }
            }

        }
        return valid;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public boolean isValidMobile() {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length() == 10) {
                check = true;
            } else
                Toast.makeText(getApplicationContext(), "Incorrect phone number", LENGTH_LONG);

        } else
            Toast.makeText(getApplicationContext(), "Invalid phone number", LENGTH_LONG);
        return check;
    }

    private void getDataFromViews() {
        //personal
        name=nameText.getText().toString();
        email=emailText.getText().toString();
        //business
        businessName=businessNameText.getText().toString();
        businessType=spinner2.getSelectedItem().toString();
        Log.d("TAG", "getDataFromViews: BUSINESS TYPE::"+businessType);
        address=addressText.getText().toString();

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
            shopImage.setImageURI(imageUri);
            imagechosen=true;
        }
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
                Toast.makeText(ProfileEdit.this, "Umm..something went wrong", Toast.LENGTH_SHORT).show();
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
                        storeInSharedPreference();

                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEdit.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        return imageUploaded;
    }

    private void setFieldViews() {
        //SET TEXFIELDS
        businessNameText.setText(businessName);
//        businessTypeText.setText(businessType);
        addressText.setText(address);
        Glide.with(getApplicationContext()).load(imageUrl).placeholder(R.mipmap.placeholder_png_foreground).into(shopImage);
        //SET TEXFIELDS
        nameText.setText(name);
        phoneText.setText(phone);
        emailText.setText(email);

        //SPINNER
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ProfileEdit.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.vendor_type));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter);
    }

    private void getHooks() {
        //HOOKS   (PERSONAL INFO)
        nameText=findViewById(R.id.name_profileEdit_EditText);
        phoneText=findViewById(R.id.phone_profileEdit_EditText);
        emailText=findViewById(R.id.email_profileEdit_EditText);
        //HOOKS  (BUSINESS INFO)
        businessNameText=findViewById(R.id.business_name_profileEdit_EditText);
//        businessTypeText=findViewById(R.id.business_type_profileEdit_EditText);
        addressText=findViewById(R.id.address_profileEdit_textView);
        changeLocationButton=findViewById(R.id.change_location_profileEdit_button);
        saveButton=findViewById(R.id.delete_account_button);
        spinner2=findViewById(R.id.spinner2);
        shopImage=findViewById(R.id.shop_image_profile_edit_imageView);
        //
        //firebase hooks
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
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
        DC_KEY=sharedPreferences.getString("DCKEY",null);
        DEPOT_KEY=sharedPreferences.getString("DEPOTKEY",null);
        imageUrl=sharedPreferences.getString("IMAGEURL",null);
        hashcode=sharedPreferences.getString("HASHCODE",null);
        LAT=Double.valueOf(sharedPreferences.getString("LAT",null));
        LANG=Double.valueOf(sharedPreferences.getString("LANG",null));

    }

    private boolean uploadDataToFirebase() {
        Log.d("INSIDE------>", "uploaddataTOFIREBASE: ");

        //Firebase reference

        databaseReference= FirebaseInit.getDatabase().getReference().child("USERS").child(phone);
        //set personal details
        databaseReference.child("profile").child("personal").child("name").setValue(name);
        databaseReference.child("profile").child("personal").child("email").setValue(email);
        databaseReference.child("profile").child("personal").child("password").setValue(password);
        databaseReference.child("profile").child("personal").child("phone").setValue(phone);
        databaseReference.child("profile").child("personal").child("dckey").setValue(DC_KEY);
        databaseReference.child("profile").child("personal").child("depotkey").setValue(DEPOT_KEY);
        //set business details
        databaseReference.child("profile").child("business").child("name").setValue(businessName);
        databaseReference.child("profile").child("business").child("type").setValue(businessType);
        databaseReference.child("profile").child("business").child("address").setValue(address);
        databaseReference.child("profile").child("business").child("image").setValue(imageUrl);
        databaseReference.child("profile").child("business").child("location").child("g").setValue(hashcode);
        databaseReference.child("profile").child("business").child("location").child("l").child("0").setValue(LAT);
        databaseReference.child("profile").child("business").child("location").child("l").child("1").setValue(LANG).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(NewRegistrationBusiness.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                dataUploaded=true;
                Log.d("INSIDE------>", "uploaddataTOFIREBASE: SUCCESS!!!!");
                if(imageUri!=null){
                    uploadImage();
                }
                else{
                    storeInSharedPreference();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileEdit.this, "Something went wrong..", Toast.LENGTH_SHORT).show();
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
        editor.putString("LAT",LAT.toString());
        editor.putString("LANG",LANG.toString());
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
        Intent intent = new Intent(ProfileEdit.this, Profile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
    }
}