package com.daisa.qreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.provider.BaseColumns._ID;
import static com.daisa.qreader.Constants.CAMERA_TABLE;
import static com.daisa.qreader.Constants.DB_NAME;
import static com.daisa.qreader.Constants.LAST_CAMERA_USED;

public class Database extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    public Database(Context contexto) {
        super(contexto, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CAMERA_TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LAST_CAMERA_USED + " TEXT)");
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
        onCreate(db);
    }

    public void updateData(String cameraID) {
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
}
