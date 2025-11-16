package com.example.mobstore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView profile;
    TextView greeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        greeting = findViewById(R.id.greet);
        profile = findViewById(R.id.imageView);
        DbHelper dbHelper = new DbHelper(this);

        Button mcart = findViewById(R.id.button);
        mcart.setOnClickListener(v -> startActivity(new Intent(this, MyCart.class)));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<Product> products = new ArrayList<>();
        products.add(new Product("Headphones",   R.drawable.head, 12.99));
        products.add(new Product("Charger",      R.drawable.charger, 10.00));
        products.add(new Product("Phone Case",   R.drawable.back, 3.00));
        products.add(new Product("Power Bank",   R.drawable.bank, 49.99));
        products.add(new Product("Earbuds",      R.drawable.er, 6.88));
        products.add(new Product("Earphones",    R.drawable.ep, 100.99));
        products.add(new Product("Tempered",     R.drawable.tm, 2.99));
        products.add(new Product("SD Card",      R.drawable.sd, 10.99));

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setAdapter(new AccessoryAdapter(this, products, dbHelper));

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i1 = new Intent(MainActivity.this,login.class);
                startActivity(i1);
            }
        });

        updateGreeting();

        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", null);
        if (username != null) {
            greeting.setText("Hi, " + username);
            greeting.setVisibility(View.VISIBLE);
        } else {
            greeting.setText("Hi");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGreeting();
    }

    private void updateGreeting() {
        SharedPreferences sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", null);
        if (username != null && !username.isEmpty()) {
            greeting.setText("Hi, " + username);
        } else {
            greeting.setText("Hi");
        }
    }
}