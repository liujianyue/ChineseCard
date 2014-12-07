package com.codemany.chinesecard;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    private static final String DB_NAME = "xzqh.db";
    private static final int DB_VERSION = 1;

    private static DBHelper helper;
    private static SQLiteDatabase db;

    private Context context;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    public static DBHelper getInstance(Context context) {
        if (helper == null) {
            helper = new DBHelper(context);
            helper.openDataBase();

            if (db == null) {
                try {
                    db = helper.getWritableDatabase();
                    helper.copyDatabase();
                }
                catch (Exception e) {
                    Log.e(TAG, "Error in database creation");
                }

                helper.openDataBase();
            }
        }
        return helper;
    }

    private void copyDatabase() throws IOException {
        InputStream is = context.getAssets().open(DB_NAME);
        OutputStream os = new FileOutputStream(context.getDatabasePath(DB_NAME));
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }

        os.flush();
        os.close();
        is.close();
    }

    private void openDataBase() {
        try {
            db = SQLiteDatabase.openDatabase(
                    context.getDatabasePath(DB_NAME).getAbsolutePath(),
                    null,
                    SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        } catch (SQLiteException e) {
            // database does't exist yet
        }
    }

    public SimpleCursorAdapter getListByParentCode(Context context, String parentCode) {
        SimpleCursorAdapter list = null;
        DBHelper dHelper = new DBHelper(context);
        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT code as _id, division FROM xzqhdm WHERE parent_code = ?", new String[] {parentCode});
        if (cursor.getCount() > 0) {
            list = new SimpleCursorAdapter(context,
                    android.R.layout.simple_spinner_item,
                    cursor,
                    new String[] {"division"},
                    new int[] {android.R.id.text1},
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
        return list;
    }

    public String getAddress(Context context, String code) {
        DBHelper dHelper = new DBHelper(context);
        SQLiteDatabase db = dHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT address FROM xzqhdm WHERE code = ?", new String[] {code});
        String address = "";
        if (cursor.moveToFirst()) {
            address = cursor.getString(cursor.getColumnIndex("address"));
        }
        cursor.close();
        return address;
    }

    @Override
    public synchronized void close() {
        if (db != null) {
            db.close();
        }
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
