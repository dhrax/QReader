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
import static com.daisa.qreader.Constants.FAVORITE;
import static com.daisa.qreader.Constants.HISTORY_TABLE;
import static com.daisa.qreader.Constants.LAST_CAMERA_USED;
import static com.daisa.qreader.Constants.LINK_TEXT;
import static com.daisa.qreader.Constants.SCAN_DATE;
import static com.daisa.qreader.Util.getActualDate;

public class Database extends SQLiteOpenHelper {

    private static final int VERSION = 2;

    public Database(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + CAMERA_TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LAST_CAMERA_USED + " TEXT)");
        db.execSQL("CREATE TABLE " + HISTORY_TABLE + "(" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LINK_TEXT + " TEXT, " + SCAN_DATE + " TEXT, " + FAVORITE + " INTEGER)");
        addData(db);
    }

    /**
     * Last camera used preference so we always have data.
     * @param db Current {@link Database} instance.
     */
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

        String[] arguments = new String[]{};
        db.update(CAMERA_TABLE, values, "_id = 1", arguments);
        db.close();
    }

    private boolean updateScanDate(String link) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(SCAN_DATE, getActualDate());

        String[] arguments = new String[]{};
        int rowsAffected = db.update(HISTORY_TABLE, values, LINK_TEXT + " = '" + link + "';", arguments);
        db.close();

        return rowsAffected > 0;
    }

    public boolean updateFavoriteStatus(String link, boolean isFavorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FAVORITE, isFavorite);

        String[] arguments = new String[]{};
        int rowsAffected = db.update(HISTORY_TABLE, values, LINK_TEXT + " = '" + link + "';", arguments);
        db.close();

        return rowsAffected > 0;
    }

    public boolean updateFavoriteStatus(int id, boolean isFavorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FAVORITE, isFavorite);

        String[] arguments = new String[]{};
        int rowsAffected = db.update(HISTORY_TABLE, values, _ID + " = " + id + ";", arguments);
        db.close();

        return rowsAffected > 0;
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
            Log.d("DEBUG selectLastCameraUsed", "Cursor null or no data");
            lastCameraUsed = "0";
        }

        db.close();

        return lastCameraUsed;
    }

    public void insertLinkToHistory(String link) {
        //If the link wasn't scanned before we add it to the database.
        if (!linkAlreadyExists(link)) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(LINK_TEXT, link);
            values.put(SCAN_DATE, getActualDate());

            db.insert(HISTORY_TABLE, null, values);

            db.close();
        //if it was already scanned, we just update the last scan date to the current time.
        } else {
            Log.d("DEBUG insertLinkToHistory", "Link added before");
            if(updateScanDate(link)){
                Log.d("DEBUG insertLinkToHistory", "Scan date updated");
            }else{
                Log.d("DEBUG insertLinkToHistory", "[ERROR] Couldn't upload the scan date");
            }
        }
    }

    /**
     * @param link link to check.
     * @return whether the link was already scanned or not.
     */
    private boolean linkAlreadyExists(String link) {
        final String[] SELECT = {_ID};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(HISTORY_TABLE, SELECT, LINK_TEXT + " = '" + link + "';", null, null, null,
                null);

        boolean exists;
        if (cursor != null && cursor.moveToFirst()) {
            exists = true;
            cursor.close();
        } else {
            exists = false;
            Log.d("DEBUG linkAlreadyExists", "Link doesn't exists");
        }

        db.close();

        return exists;
    }

    public boolean getFavoriteStatus(String link) {
        final String[] SELECT = {FAVORITE};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(HISTORY_TABLE, SELECT, LINK_TEXT + " = '" + link + "';", null, null, null,
                null);
        boolean isFavorite;
        if (cursor != null && cursor.moveToFirst()) {
            isFavorite = cursor.getInt(cursor.getColumnIndex(FAVORITE)) == 1;
            cursor.close();
        } else {
            Log.d("DEBUG getFavoriteStatus", "Cursor null or no rows");
            isFavorite = false;
        }

        db.close();

        return isFavorite;
    }

    /**
     * Method used to retrieve links from the database.
     * @param selectionLinks selection of links.
     * @return links that fit the selection clause from the database.
     */
    public Collection<HistoryElement> getLinks(String selectionLinks){
        String selection;
        switch (selectionLinks.toLowerCase()){
            case "favorites":
                selection = FAVORITE + " = 1;";
                break;
            case "all":
            default:
                selection = null;
                break;
        }

        ArrayList<HistoryElement> elements = new ArrayList<>();
        final String[] SELECT = {_ID, LINK_TEXT, SCAN_DATE, FAVORITE};
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(HISTORY_TABLE, SELECT, selection, null, null, null,
                _ID + " DESC");
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(_ID));
            String text = cursor.getString(cursor.getColumnIndex(LINK_TEXT));
            String date = cursor.getString(cursor.getColumnIndex(SCAN_DATE));
            boolean favorite = cursor.getInt(cursor.getColumnIndex(FAVORITE)) == 1;
            HistoryElement element = new HistoryElement(id, text, date, favorite);
            elements.add(element);
            Log.d("DEBUG getFavoriteLinks", id + ", " + text + ", " + date + ", " + favorite);
        }
        cursor.close();

        HistoryElementSorter sorter = new HistoryElementSorter(elements);

        return sorter.getSortedElementByID();
    }
}
