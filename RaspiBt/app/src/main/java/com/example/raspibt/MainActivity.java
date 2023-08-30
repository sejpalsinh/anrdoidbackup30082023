package com.example.raspibt;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference myRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("vehical1/direction");
    }


    public void send_data(View view){ // on click event for sending data to bluetooth
        myRef.setValue(Integer.parseInt(view.getTag().toString()));
        Toast.makeText(getApplicationContext(),view.getTag().toString(), Toast.LENGTH_SHORT).show();
    }
}
