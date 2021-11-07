package in.veggiefy.androidapp.firebase;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseInitNonPersistent {

    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
        }

        return mDatabase;

    }
}
