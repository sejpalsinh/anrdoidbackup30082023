package com.i2i.firebasenotify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void setToken(View view) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users/tokens");
        myRef.child(Objects.requireNonNull(FirebaseInstanceId.getInstance().getToken())).setValue("1");
        System.out.println("Firease id : ");
    }
}
