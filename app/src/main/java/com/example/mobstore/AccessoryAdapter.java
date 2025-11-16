package com.example.mobstore;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccessoryAdapter extends RecyclerView.Adapter<AccessoryAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> products;
    private final DbHelper dbHelper;

    public AccessoryAdapter(Context context, List<Product> products, DbHelper dbHelper) {
        this.context = context;
        this.products = products;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_accessory, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        Product p = products.get(i);
        h.imageView.setImageResource(p.image);
        h.textView.setText(p.name);

        h.cartButton.setOnClickListener(v -> {
            if (dbHelper.isProductInCart(p.name)) {
                int qty = getQty(p.name) + 1;
                dbHelper.updateQuantity(p.name, qty);
                Toast.makeText(context, "Qty: " + qty, Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.insertCartItem(p.name, p.image, p.price, 1);
                Toast.makeText(context, "Added!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getQty(String name) {
        Cursor c = dbHelper.getAllCartItems();
        if (c != null && c.moveToFirst()) {
            do {
                if (name.equals(c.getString(c.getColumnIndexOrThrow("productName"))))
                    return c.getInt(c.getColumnIndexOrThrow("quantity"));
            } while (c.moveToNext());
        }
        return 0;
    }

    @Override public int getItemCount() { return products.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView = itemView.findViewById(R.id.itemImage);
        TextView textView = itemView.findViewById(R.id.itemName);
        Button cartButton = itemView.findViewById(R.id.cartButton);
        ViewHolder(@NonNull View itemView) { super(itemView); }
    }
}