package com.daisa.qreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import static android.provider.BaseColumns._ID;
import static com.daisa.qreader.Constants.CAMERA_TABLE;
import static com.daisa.qreader.Constants.DB_NAME;
import static com.daisa.qreader.Constants.HISTORY_TABLE;
import static com.daisa.qreader.Constants.LAST_CAMERA_USED;
import static com.daisa.qreader.Constants.LINK_TEXT;
import static com.daisa.qreader.Constants.SCAN_DATE;
import static com.daisa.qreader.Util.getActualDate;

public class Database extends SQLiteOpenHelper {

    private static final int VERSION = 2;

    public Database(Context contexto) {
        super(contexto, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CAMERA_TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LAST_CAMERA_USED + " TEXT)");
        db.execSQL("CREATE TABLE " + HISTORY_TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LINK_TEXT + " TEXT, " + SCAN_DATE + " TEXT)");
        addData(db);
    }

    private void addData(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(LAST_CAMERA_USED, "0");

        db.insertOrThrow(CAMERA_TABLE, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CAMERA_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
        onCreate(db);
    }

    public void updateLastCameraUsed(String cameraID) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LAST_CAMERA_USED, cameraID);

        String[] argumentos = new String[]{};
        db.update(CAMERA_TABLE, values, "_id = 1", argumentos);
        db.close();
    }

    public String selectLastCameraUsed() {
        final String[] SELECT = {LAST_CAMERA_USED};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(CAMERA_TABLE, SELECT, null, null, null, null,
                null);
        String lastCameraUsed;

        if (cursor != null && cursor.moveToFirst()) {
            lastCameraUsed = cursor.getString(0);
            cursor.close();
        } else {
            Log.d("DEBUG", "Cursor null");
            lastCameraUsed = "0";
        }

        db.close();

        return lastCameraUsed;
    }

    public void insertLinkToHistory(String linkText){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LINK_TEXT, linkText);
        values.put(SCAN_DATE, getActualDate());

        db.insert(HISTORY_TABLE, null, values);

        db.close();
    }

    public Collection<HistoryElement> getLinks() {
        Collection<HistoryElement> elements = new ArrayList<>();
        final String[] SELECT = {_ID, LINK_TEXT, SCAN_DATE};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(HISTORY_TABLE, SELECT, null, null, null, null,
                null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String text = cursor.getString(cursor.getColumnIndex(LINK_TEXT));
                String date = cursor.getString(cursor.getColumnIndex(SCAN_DATE));
                HistoryElement element = new HistoryElement(text, date, false);
                elements.add(element);
                Log.d("DEBUG Historial", id + ", " + text + ", " + date);
            }
        }
        cursor.close();//Close the cursor

        return elements;

    }
}
