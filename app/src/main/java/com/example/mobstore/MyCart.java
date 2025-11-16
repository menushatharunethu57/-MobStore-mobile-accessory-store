package com.example.mobstore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MyCart extends AppCompatActivity implements CartAdapter.OnCartUpdateListener {

    RecyclerView recyclerView;
    TextView emptyText, totalPriceText;
    DbHelper dbHelper;
    CartAdapter adapter;
    Button homepg, purchaseAllBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_mycart);

            // Initialize ALL views
            homepg = findViewById(R.id.home);
            purchaseAllBtn = findViewById(R.id.purchase);
            recyclerView = findViewById(R.id.recyclerViewCart);
            emptyText = findViewById(R.id.emptyText);
            totalPriceText = findViewById(R.id.totalPriceText);

            // Check if views are properly initialized
            if (recyclerView == null) {
                Log.e("MyCart", "RecyclerView is null!");
                Toast.makeText(this, "Error: RecyclerView not found", Toast.LENGTH_LONG).show();
                return;
            }

            if (emptyText == null) {
                Log.e("MyCart", "EmptyText is null!");
                Toast.makeText(this, "Error: EmptyText not found", Toast.LENGTH_LONG).show();
                return;
            }

            if (totalPriceText == null) {
                Log.e("MyCart", "TotalPriceText is null!");
                Toast.makeText(this, "Error: TotalPriceText not found", Toast.LENGTH_LONG).show();
                return;
            }

            if (purchaseAllBtn == null) {
                Log.e("MyCart", "PurchaseAllBtn is null!");
                Toast.makeText(this, "Error: PurchaseAllBtn not found", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d("MyCart", "All views initialized successfully");

            // Home button
            homepg.setOnClickListener(v -> {
                Intent i = new Intent(MyCart.this, MainActivity.class);
                startActivity(i);
            });

            // Purchase All button - Purchase all items in cart
            purchaseAllBtn.setOnClickListener(v -> {
                if (dbHelper.getTotalItemCount() > 0) {
                    Intent intent = new Intent(MyCart.this, PurchasePage.class);
                    intent.putExtra("singleItem", false); // Purchasing all items
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                }
            });

            // Initialize database
            dbHelper = new DbHelper(this);
            Log.d("MyCart", "DbHelper initialized");

            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            Log.d("MyCart", "LayoutManager set");

            // Load cart items
            loadCart();

        } catch (Exception e) {
            Log.e("MyCart", "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadCart() {
        Cursor cursor = null;
        try {
            Log.d("MyCart", "Loading cart...");
            cursor = dbHelper.getAllCartItems();

            if (cursor == null) {
                Log.e("MyCart", "Cursor is null!");
                showEmptyCart();
                return;
            }

            int count = cursor.getCount();
            Log.d("MyCart", "Cart items count: " + count);

            if (count > 0) {
                // Show RecyclerView and controls, hide empty message
                recyclerView.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);
                totalPriceText.setVisibility(View.VISIBLE);
                purchaseAllBtn.setVisibility(View.VISIBLE);

                // Update total price
                updateTotalPrice();

                // Create and set adapter
                adapter = new CartAdapter(this, cursor, dbHelper, this);
                recyclerView.setAdapter(adapter);
                Log.d("MyCart", "Adapter set with " + count + " items");
            } else {
                // No items in cart
                showEmptyCart();
                if (cursor != null) {
                    cursor.close();
                }
            }

        } catch (Exception e) {
            Log.e("MyCart", "Error loading cart: " + e.getMessage());
            e.printStackTrace();
            showEmptyCart();
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalPrice() {
        try {
            double total = dbHelper.getTotalCartPrice();
            totalPriceText.setText(String.format("Total: $%.2f", total));
        } catch (Exception e) {
            Log.e("MyCart", "Error updating total price: " + e.getMessage());
            totalPriceText.setText("Total: $0.00");
        }
    }

    private void showEmptyCart() {
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        totalPriceText.setVisibility(View.GONE);
        purchaseAllBtn.setVisibility(View.GONE);
        emptyText.setText("Your cart is empty");
    }

    @Override
    public void onCartUpdated() {
        // Reload cart when item is updated or deleted
        loadCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart when returning from purchase page
        loadCart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (adapter != null) {
                Cursor cursor = adapter.getCursor();
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e("MyCart", "Error in onDestroy: " + e.getMessage());
        }
    }
}
