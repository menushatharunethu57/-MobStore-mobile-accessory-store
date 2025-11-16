package com.example.mobstore;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;
    private DbHelper dbHelper;
    private OnCartUpdateListener updateListener;

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public CartAdapter(Context context, Cursor cursor, DbHelper dbHelper, OnCartUpdateListener listener) {
        this.context = context;
        this.cursor = cursor;
        this.dbHelper = dbHelper;
        this.updateListener = listener;
        Log.d("CartAdapter", "Adapter created with cursor count: " + (cursor != null ? cursor.getCount() : "null"));
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void swapCursor(Cursor newCursor) {
        Cursor oldCursor = cursor;
        this.cursor = newCursor;

        if (oldCursor != null && !oldCursor.isClosed()) {
            oldCursor.close();
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
            Log.d("CartAdapter", "ViewHolder created");
            return new ViewHolder(view);
        } catch (Exception e) {
            Log.e("CartAdapter", "Error creating ViewHolder: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (cursor == null) {
                Log.e("CartAdapter", "Cursor is null in onBindViewHolder");
                holder.name.setText("Error: No data");
                return;
            }

            if (!cursor.moveToPosition(position)) {
                Log.e("CartAdapter", "Failed to move cursor to position: " + position);
                holder.name.setText("Error loading item");
                return;
            }

            // Get column indices
            int idCol = cursor.getColumnIndex("id");
            int nameCol = cursor.getColumnIndex("productName");
            int imgCol = cursor.getColumnIndex("productImage");
            int priceCol = cursor.getColumnIndex("price");
            int qtyCol = cursor.getColumnIndex("quantity");

            // Check if columns exist
            if (idCol == -1 || nameCol == -1 || imgCol == -1 || priceCol == -1 || qtyCol == -1) {
                Log.e("CartAdapter", "Column not found!");
                holder.name.setText("Database error");
                return;
            }

            // Get values
            int itemId = cursor.getInt(idCol);
            String name = cursor.getString(nameCol);
            int image = cursor.getInt(imgCol);
            double price = cursor.getDouble(priceCol);
            int qty = cursor.getInt(qtyCol);

            Log.d("CartAdapter", "Binding item: " + name + " | $" + price + " | Qty: " + qty);

            // Set values to views
            holder.name.setText(name != null ? name : "Unknown");
            holder.price.setText(String.format("$%.2f", price));
            holder.quantity.setText("Qty: " + qty);

            // Set image with error handling
            try {
                holder.image.setImageResource(image);
            } catch (Exception e) {
                Log.e("CartAdapter", "Error setting image: " + e.getMessage());
                holder.image.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // UPDATE BUTTON - Show dialog to change quantity
            holder.updateBtn.setOnClickListener(v -> {
                showUpdateDialog(itemId, name, qty);
            });

            // DELETE BUTTON - Remove item from cart
            holder.deleteBtn.setOnClickListener(v -> {
                showDeleteDialog(itemId, name);
            });

            // PURCHASE/BUY BUTTON - Go to purchase page for this single item
            holder.purchaseBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, PurchasePage.class);
                intent.putExtra("singleItem", true);
                intent.putExtra("itemId", itemId);
                intent.putExtra("itemName", name);
                intent.putExtra("itemPrice", price);
                intent.putExtra("itemQuantity", qty);
                intent.putExtra("itemImage", image);
                context.startActivity(intent);
            });

        } catch (Exception e) {
            Log.e("CartAdapter", "Error in onBindViewHolder: " + e.getMessage());
            e.printStackTrace();
            holder.name.setText("Error loading item");
            holder.price.setText("$0.00");
            holder.quantity.setText("Qty: 0");
        }
    }

    // Show dialog to update quantity
    private void showUpdateDialog(int itemId, String itemName, int currentQty) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Update Quantity");
        builder.setMessage("Update quantity for " + itemName);

        // Create EditText for quantity input
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(currentQty));
        input.setSelection(input.getText().length()); // Move cursor to end
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String qtyStr = input.getText().toString().trim();
            if (qtyStr.isEmpty()) {
                Toast.makeText(context, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int newQty = Integer.parseInt(qtyStr);
                if (newQty < 1) {
                    Toast.makeText(context, "Quantity must be at least 1", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dbHelper.updateQuantityById(itemId, newQty)) {
                    Toast.makeText(context, "Quantity updated to " + newQty, Toast.LENGTH_SHORT).show();
                    if (updateListener != null) {
                        updateListener.onCartUpdated();
                    }
                } else {
                    Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Invalid quantity", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Show confirmation dialog before deleting
    private void showDeleteDialog(int itemId, String itemName) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to remove " + itemName + " from cart?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteCartItemById(itemId)) {
                        Toast.makeText(context, itemName + " removed from cart", Toast.LENGTH_SHORT).show();
                        if (updateListener != null) {
                            updateListener.onCartUpdated();
                        }
                    } else {
                        Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        int count = (cursor != null) ? cursor.getCount() : 0;
        Log.d("CartAdapter", "getItemCount: " + count);
        return count;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, quantity;
        Button updateBtn, deleteBtn, purchaseBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                image = itemView.findViewById(R.id.cartItemImage);
                name = itemView.findViewById(R.id.cartItemName);
                price = itemView.findViewById(R.id.cartItemPrice);
                quantity = itemView.findViewById(R.id.cartItemQuantity);
                updateBtn = itemView.findViewById(R.id.update);
                deleteBtn = itemView.findViewById(R.id.delete);
                purchaseBtn = itemView.findViewById(R.id.purchase);

                if (image == null) Log.e("CartAdapter", "cartItemImage not found");
                if (name == null) Log.e("CartAdapter", "cartItemName not found");
                if (price == null) Log.e("CartAdapter", "cartItemPrice not found");
                if (quantity == null) Log.e("CartAdapter", "cartItemQuantity not found");
                if (updateBtn == null) Log.e("CartAdapter", "update button not found");
                if (deleteBtn == null) Log.e("CartAdapter", "delete button not found");
                if (purchaseBtn == null) Log.e("CartAdapter", "purchase button not found");

            } catch (Exception e) {
                Log.e("CartAdapter", "Error in ViewHolder constructor: " + e.getMessage());
            }
        }
    }
}
