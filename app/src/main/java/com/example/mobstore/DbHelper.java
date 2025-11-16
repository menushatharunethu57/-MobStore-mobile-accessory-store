package com.example.mobstore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "CartDB4";
    private static final int DB_VERSION = 4;
    private static final String TABLE_CART = "cart";

    // Column names
    private static final String COL_ID = "id";
    private static final String COL_PRODUCT_NAME = "productName";
    private static final String COL_PRODUCT_IMAGE = "productImage";
    private static final String COL_PRICE = "price";
    private static final String COL_QUANTITY = "quantity";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String createTable = "CREATE TABLE " + TABLE_CART + "(" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_PRODUCT_NAME + " TEXT," +
                    COL_PRODUCT_IMAGE + " INTEGER," +
                    COL_PRICE + " REAL," +
                    COL_QUANTITY + " INTEGER DEFAULT 1)";
            db.execSQL(createTable);
            Log.d("DbHelper", "Table created successfully");
        } catch (Exception e) {
            Log.e("DbHelper", "Error creating table: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
            onCreate(db);
            Log.d("DbHelper", "Database upgraded from version " + oldVersion + " to " + newVersion);
        } catch (Exception e) {
            Log.e("DbHelper", "Error upgrading database: " + e.getMessage());
        }
    }

    public long insertCartItem(String name, int image, double price, int qty) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_PRODUCT_NAME, name);
            cv.put(COL_PRODUCT_IMAGE, image);
            cv.put(COL_PRICE, price);
            cv.put(COL_QUANTITY, qty);

            long result = db.insert(TABLE_CART, null, cv);
            Log.d("DbHelper", "Inserted item: " + name + " | Result: " + result);
            return result;
        } catch (Exception e) {
            Log.e("DbHelper", "Error inserting item: " + e.getMessage());
            return -1;
        }
    }


    public Cursor getAllCartItems() {
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART, null);
            Log.d("DbHelper", "Retrieved " + cursor.getCount() + " items from cart");
            return cursor;
        } catch (Exception e) {
            Log.e("DbHelper", "Error getting cart items: " + e.getMessage());
            return null;
        }
    }


    public Cursor getCartItemById(int id) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM " + TABLE_CART + " WHERE " + COL_ID + "=?",
                    new String[]{String.valueOf(id)});
            return cursor;
        } catch (Exception e) {
            Log.e("DbHelper", "Error getting item by ID: " + e.getMessage());
            if (cursor != null) cursor.close();
            return null;
        }
    }


    public boolean isProductInCart(String name) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT 1 FROM " + TABLE_CART + " WHERE " + COL_PRODUCT_NAME + "=?",
                    new String[]{name});
            boolean exists = c.getCount() > 0;
            return exists;
        } catch (Exception e) {
            Log.e("DbHelper", "Error checking if product in cart: " + e.getMessage());
            return false;
        } finally {
            if (c != null) c.close();
        }
    }


    public int getProductQuantity(String name) {
        Cursor c = null;
        try {
            c = getReadableDatabase().rawQuery(
                    "SELECT " + COL_QUANTITY + " FROM " + TABLE_CART +
                            " WHERE " + COL_PRODUCT_NAME + "=?",
                    new String[]{name});
            if (c != null && c.moveToFirst()) {
                int qty = c.getInt(0);
                return qty;
            }
            return 0;
        } catch (Exception e) {
            Log.e("DbHelper", "Error getting product quantity: " + e.getMessage());
            return 0;
        } finally {
            if (c != null) c.close();
        }
    }


    public double getTotalCartPrice() {
        double total = 0.0;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT " + COL_PRICE + ", " + COL_QUANTITY + " FROM " + TABLE_CART,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    double price = cursor.getDouble(0);
                    int quantity = cursor.getInt(1);
                    total += price * quantity;
                } while (cursor.moveToNext());
            }
            Log.d("DbHelper", "Total cart price: $" + String.format("%.2f", total));
        } catch (Exception e) {
            Log.e("DbHelper", "Error calculating total: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }


    public int getTotalItemCount() {
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = getAllCartItems();
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            Log.e("DbHelper", "Error getting item count: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }


    public int getTotalQuantity() {
        int total = 0;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.rawQuery("SELECT SUM(" + COL_QUANTITY + ") FROM " + TABLE_CART, null);
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
            Log.d("DbHelper", "Total quantity: " + total);
        } catch (Exception e) {
            Log.e("DbHelper", "Error getting total quantity: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }


    public int updateQuantity(String name, int qty) {
        SQLiteDatabase db = null;
        try {
            if (qty < 1) {
                Log.w("DbHelper", "Quantity must be at least 1");
                return 0;
            }

            db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_QUANTITY, qty);

            int result = db.update(TABLE_CART, cv, COL_PRODUCT_NAME + "=?",
                    new String[]{name});
            Log.d("DbHelper", "Updated quantity for " + name + " to " + qty + " | Rows affected: " + result);
            return result;
        } catch (Exception e) {
            Log.e("DbHelper", "Error updating quantity: " + e.getMessage());
            return 0;
        }
    }


    public boolean updateQuantityById(int id, int newQuantity) {
        SQLiteDatabase db = null;
        try {
            if (newQuantity < 1) {
                Log.w("DbHelper", "Quantity must be at least 1");
                return false;
            }

            db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_QUANTITY, newQuantity);

            int rowsAffected = db.update(TABLE_CART, cv, COL_ID + "=?",
                    new String[]{String.valueOf(id)});

            boolean success = rowsAffected > 0;
            Log.d("DbHelper", "Updated item ID " + id + " to quantity " + newQuantity +
                    " | Success: " + success);
            return success;
        } catch (Exception e) {
            Log.e("DbHelper", "Error updating quantity by ID: " + e.getMessage());
            return false;
        }
    }


    public boolean incrementQuantity(int id) {
        Cursor cursor = null;
        try {
            cursor = getCartItemById(id);
            if (cursor != null && cursor.moveToFirst()) {
                int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
                return updateQuantityById(id, currentQty + 1);
            }
            return false;
        } catch (Exception e) {
            Log.e("DbHelper", "Error incrementing quantity: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }


    public boolean decrementQuantity(int id) {
        Cursor cursor = null;
        try {
            cursor = getCartItemById(id);
            if (cursor != null && cursor.moveToFirst()) {
                int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
                if (currentQty > 1) {
                    return updateQuantityById(id, currentQty - 1);
                } else {
                    Log.w("DbHelper", "Cannot decrement below 1");
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("DbHelper", "Error decrementing quantity: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }


    public boolean deleteCartItem(String productName) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_CART, COL_PRODUCT_NAME + "=?",
                    new String[]{productName});

            boolean success = rowsDeleted > 0;
            Log.d("DbHelper", "Deleted " + rowsDeleted + " item(s): " + productName +
                    " | Success: " + success);
            return success;
        } catch (Exception e) {
            Log.e("DbHelper", "Error deleting item: " + e.getMessage());
            return false;
        }
    }


    public boolean deleteCartItemById(int id) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_CART, COL_ID + "=?",
                    new String[]{String.valueOf(id)});

            boolean success = rowsDeleted > 0;
            Log.d("DbHelper", "Deleted item with ID: " + id + " | Rows affected: " +
                    rowsDeleted + " | Success: " + success);
            return success;
        } catch (Exception e) {
            Log.e("DbHelper", "Error deleting item by ID: " + e.getMessage());
            return false;
        }
    }

    public void clearCart() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_CART, null, null);
            Log.d("DbHelper", "Cart cleared | " + rowsDeleted + " items removed");
        } catch (Exception e) {
            Log.e("DbHelper", "Error clearing cart: " + e.getMessage());
        }
    }


    public int deleteMultipleItems(int[] ids) {
        SQLiteDatabase db = null;
        int totalDeleted = 0;
        try {
            db = getWritableDatabase();
            db.beginTransaction();

            for (int id : ids) {
                int deleted = db.delete(TABLE_CART, COL_ID + "=?",
                        new String[]{String.valueOf(id)});
                totalDeleted += deleted;
            }

            db.setTransactionSuccessful();
            Log.d("DbHelper", "Deleted " + totalDeleted + " items");
        } catch (Exception e) {
            Log.e("DbHelper", "Error deleting multiple items: " + e.getMessage());
        } finally {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
        }
        return totalDeleted;
    }


    public boolean isCartEmpty() {
        return getTotalItemCount() == 0;
    }


    public String getCartSummary() {
        StringBuilder summary = new StringBuilder();
        Cursor cursor = null;
        try {
            cursor = getAllCartItems();
            if (cursor != null && cursor.moveToFirst()) {
                summary.append("=== CART SUMMARY ===\n");
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_NAME));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PRICE));
                    int qty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
                    summary.append(String.format("%s | $%.2f x %d = $%.2f\n",
                            name, price, qty, price * qty));
                } while (cursor.moveToNext());
                summary.append(String.format("TOTAL: $%.2f\n", getTotalCartPrice()));
                summary.append("====================");
            } else {
                summary.append("Cart is empty");
            }
        } catch (Exception e) {
            Log.e("DbHelper", "Error getting cart summary: " + e.getMessage());
            summary.append("Error loading cart");
        } finally {
            if (cursor != null) cursor.close();
        }
        return summary.toString();
    }
}