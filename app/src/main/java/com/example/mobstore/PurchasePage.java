
package com.example.mobstore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PurchasePage extends AppCompatActivity {

    TextView totalAmountText, itemCountText, pageTitle;
    EditText nameInput, addressInput, phoneInput;
    Button confirmBtn, cancelBtn;
    RecyclerView summaryRecyclerView;
    DbHelper dbHelper;

    // For single item purchase
    boolean isSingleItem = false;
    int singleItemId;
    String singleItemName;
    double singleItemPrice;
    int singleItemQuantity;
    int singleItemImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_page);


        pageTitle = findViewById(R.id.titleText);
        totalAmountText = findViewById(R.id.totalAmountText);
        itemCountText = findViewById(R.id.itemCountText);
        nameInput = findViewById(R.id.nameInput);
        addressInput = findViewById(R.id.addressInput);
        phoneInput = findViewById(R.id.phoneInput);
        confirmBtn = findViewById(R.id.confirmBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        summaryRecyclerView = findViewById(R.id.summaryRecyclerView);


        dbHelper = new DbHelper(this);


        Intent intent = getIntent();
        isSingleItem = intent.getBooleanExtra("singleItem", false);

        if (isSingleItem) {
            // Get single item details
            singleItemId = intent.getIntExtra("itemId", -1);
            singleItemName = intent.getStringExtra("itemName");
            singleItemPrice = intent.getDoubleExtra("itemPrice", 0.0);
            singleItemQuantity = intent.getIntExtra("itemQuantity", 1);
            singleItemImage = intent.getIntExtra("itemImage", 0);

            pageTitle.setText("Purchase: " + singleItemName);
            loadSingleItemSummary();
        } else {

            pageTitle.setText("Complete Your Purchase");
            summaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            loadOrderSummary();
        }


        loadOrderDetails();


        cancelBtn.setOnClickListener(v -> finish());


        confirmBtn.setOnClickListener(v -> confirmPurchase());
    }

    private void loadSingleItemSummary() {
        summaryRecyclerView.setVisibility(View.GONE);
    }

    private void loadOrderSummary() {
        try {
            Cursor cursor = dbHelper.getAllCartItems();
            if (cursor != null && cursor.getCount() > 0) {
                PurchaseSummaryAdapter adapter = new PurchaseSummaryAdapter(this, cursor);
                summaryRecyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e("PurchasePage", "Error loading summary: " + e.getMessage());
        }
    }

    private void loadOrderDetails() {
        try {
            if (isSingleItem) {
                // Single item purchase
                double total = singleItemPrice * singleItemQuantity;
                totalAmountText.setText(String.format("$%.2f", total));
                itemCountText.setText("1 item");
            } else {
                // All items purchase
                double total = dbHelper.getTotalCartPrice();
                int itemCount = dbHelper.getTotalItemCount();
                totalAmountText.setText(String.format("$%.2f", total));
                itemCountText.setText(itemCount + " item(s)");
            }
        } catch (Exception e) {
            Log.e("PurchasePage", "Error loading order details: " + e.getMessage());
            totalAmountText.setText("$0.00");
            itemCountText.setText("0 item(s)");
        }
    }

    private void confirmPurchase() {

        String name = nameInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();


        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            addressInput.setError("Address is required");
            addressInput.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }


        new AlertDialog.Builder(this)
                .setTitle("Confirm Purchase")
                .setMessage("Are you sure you want to complete this purchase?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Process purchase
                    processPurchase(name, address, phone);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void processPurchase(String name, String address, String phone) {
        try {
            double totalAmount;
            int itemCount;
            String orderDetails;

            if (isSingleItem) {
                totalAmount = singleItemPrice * singleItemQuantity;
                itemCount = 1;
                orderDetails = "Item: " + singleItemName + "\n" +
                        "Quantity: " + singleItemQuantity + "\n" +
                        "Price: $" + String.format("%.2f", singleItemPrice) + "\n" +
                        "Subtotal: $" + String.format("%.2f", totalAmount);


                dbHelper.deleteCartItemById(singleItemId);

                Log.d("PurchasePage", "Single Item Purchase: " + singleItemName);
            } else {
                totalAmount = dbHelper.getTotalCartPrice();
                itemCount = dbHelper.getTotalItemCount();
                orderDetails = "Total Items: " + itemCount + "\n" +
                        "Total Amount: $" + String.format("%.2f", totalAmount);


                dbHelper.clearCart();

                Log.d("PurchasePage", "All Items Purchase");
            }

            Log.d("PurchasePage", "Purchase Details:");
            Log.d("PurchasePage", "Name: " + name);
            Log.d("PurchasePage", "Address: " + address);
            Log.d("PurchasePage", "Phone: " + phone);
            Log.d("PurchasePage", "Total: $" + totalAmount);


            new AlertDialog.Builder(this)
                    .setTitle("Purchase Successful!")
                    .setMessage("Thank you for your purchase!\n\n" +
                            "Order Details:\n" +
                            orderDetails + "\n\n" +
                            "Customer Details:\n" +
                            "Name: " + name + "\n" +
                            "Phone: " + phone + "\n\n" +
                            "Delivery Address:\n" + address + "\n\n" +
                            "Your order will be delivered soon!")
                    .setPositiveButton("OK", (dialog, which) -> {

                        Intent intent = new Intent(PurchasePage.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();

        } catch (Exception e) {
            Log.e("PurchasePage", "Error processing purchase: " + e.getMessage());
            Toast.makeText(this, "Error processing purchase. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
}