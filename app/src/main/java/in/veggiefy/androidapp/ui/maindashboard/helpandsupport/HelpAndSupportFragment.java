package in.veggiefy.androidapp.ui.maindashboard.helpandsupport;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import in.veggiefy.androidapp.firebase.FirebaseInit;
import in.veggiefy.androidapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HelpAndSupportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HelpAndSupportFragment extends Fragment {

    TextView phoneText;
    TextView supportEmailText;
    //variables
    String supportPhone="6382411584",supportMail="support@veggiefy.in";
    DatabaseReference supportDetailsRef;
    public static HelpAndSupportFragment newInstance(String param1, String param2) {
        HelpAndSupportFragment fragment = new HelpAndSupportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root= inflater.inflate(R.layout.fragment_help_and_support, container, false);
        // Inflate the layout for this fragment
        Activity activity=this.getActivity();
        Toolbar toolbar=(Toolbar)activity.findViewById(R.id.toolbar);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                toolbar.setTitle("SUPPORT");
            }
        });
        phoneText=root.findViewById(R.id.support_phone_fragment_textView);
        supportEmailText=root.findViewById(R.id.support_mail_fragment_textView);

        supportDetailsRef= FirebaseInit.getDatabase().getReference("MARKET/RUGULATIONS/administration");
        supportDetailsRef.keepSynced(true);
        supportDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onDataChange: SNAPAT FRAGMNT::"+snapshot);
                supportPhone=snapshot.child("supportphone").getValue(String.class);
                supportMail=snapshot.child("supportmail").getValue(String.class);
                phoneText.setText("Please call our toll-free number "+supportPhone);
                supportEmailText.setText("Drop a mail to "+supportMail+" our support team will respond soon");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return root;
    }
}