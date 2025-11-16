package com.example.mobstore;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PurchaseSummaryAdapter extends RecyclerView.Adapter<PurchaseSummaryAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;

    public PurchaseSummaryAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
        Log.d("PurchaseSummaryAdapter", "Adapter created with cursor count: " +
                (cursor != null ? cursor.getCount() : "null"));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchase_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (cursor == null || !cursor.moveToPosition(position)) {
                Log.e("PurchaseSummaryAdapter", "Failed to move cursor to position: " + position);
                return;
            }

            // Get data from cursor
            String name = cursor.getString(cursor.getColumnIndexOrThrow("productName"));
            int image = cursor.getInt(cursor.getColumnIndexOrThrow("productImage"));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            double subtotal = price * qty;

            // Set data to views
            holder.name.setText(name);
            holder.price.setText(String.format("$%.2f", price));
            holder.quantity.setText("x " + qty);
            holder.subtotal.setText(String.format("$%.2f", subtotal));
            holder.image.setImageResource(image);

        } catch (Exception e) {
            Log.e("PurchaseSummaryAdapter", "Error in onBindViewHolder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return (cursor != null) ? cursor.getCount() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, quantity, subtotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.summaryItemImage);
            name = itemView.findViewById(R.id.summaryItemName);
            price = itemView.findViewById(R.id.summaryItemPrice);
            quantity = itemView.findViewById(R.id.summaryItemQuantity);
            subtotal = itemView.findViewById(R.id.summaryItemSubtotal);
        }
    }
}
