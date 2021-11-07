package in.veggiefy.androidapp.ui.login.newuser;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import in.veggiefy.androidapp.R;

public class NewRegistrationPersonal extends AppCompatActivity{


    public Button nextpage;
    String phonenumber;
    EditText nameEditText;
    EditText emailEditText;
    ProgressDialog progressDialog;
    //Variables
    String name;
    String email;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_registration_personal);

        //Hooks
        nextpage = findViewById(R.id.next_page_button);
        nameEditText=findViewById(R.id.name_Text);
        emailEditText=findViewById(R.id.email_Text);

        //Variables
        Bundle bundle=getIntent().getExtras();
        phonenumber=bundle.getString("Phone");

        nextpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set ProgressDialog
                progressDialog=new ProgressDialog(NewRegistrationPersonal.this);
                //show progressDialog
                progressDialog.show();
                //set contentView for progressDialog
                progressDialog.setContentView(R.layout.progress_dialog);
                //set transparent background
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                  //variables
                  name=nameEditText.getText().toString().trim();
                  email=emailEditText.getText().toString().trim();

                if(checkIfFieldIsNull()){
                    progressDialog.dismiss();
                    Toast.makeText(NewRegistrationPersonal.this, "Fields can't be empty", Toast.LENGTH_SHORT).show();
                }

                else if (isValidEmail(email))
                {
                        Intent intent = new Intent(NewRegistrationPersonal.this, ChooseLocation.class);
                        intent.putExtra("Phone",phonenumber);
                        intent.putExtra("Name",name);
                        intent.putExtra("Email",email);
                        intent.putExtra("CHANGING",false);
                        startActivity(intent);
                        progressDialog.dismiss();

                }
                else
                {
                    progressDialog.dismiss();
                    Toast.makeText(NewRegistrationPersonal.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkIfFieldIsNull() {
        //return variable
        boolean empty=false;
        //variables
        String name=nameEditText.getText().toString().trim();
        String email=emailEditText.getText().toString().trim();
        if(name.equals("") || email.equals(""))
        {
            empty=true;
        }
        return empty;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit?")
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