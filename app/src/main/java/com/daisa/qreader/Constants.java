package com.daisa.qreader;


/**
 * Constants used throughout the application.
 */
public class Constants {

    /**
     * Max preview width guaranteed by Camera2 API.
     */
    public static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height guaranteed by Camera2 API.
     */
    public static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * Camera's permission request ID.
     */
    public static final int REQUEST_CAMERA_PERMISSION = 1;

    /**
     * ID to request read-only access to the external storage.
     */
    public static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 2;

    /**
     * ID to retrieve an image from the gallery.
     */
    public static final int RESULT_LOAD_IMG = 10;

    /**
     * Zoom value if the camera device doesn't support zoom.
     */
    public static final float DEFAULT_ZOOM_FACTOR = 1.0f;

    /**
     * Value used to smooth the zoom transition.
     */
    public static final int DEFAULT_ZOOM_SMOOTHER_VALUE = 10;

    /**
     * Zoom progress value.
     */
    public static final float DEFAULT_ZOOM_BAR_PROGRESS = 1f / DEFAULT_ZOOM_SMOOTHER_VALUE;

    /**
     * Constant that changes the zoom when clicking on the icons.
     */
    public static final int ZOOM_CHANGE_VALUE = 1;

    /**
     * Id of our front camera.
     */
    public static final String CAMERA_FRONT = "1";

    /**
     * Id of our back camera.
     */
    public static final String CAMERA_BACK = "0";


    public static final String DB_NAME = "QReaderDB.db";
    //Constants to store last camera used.
    public static final String CAMERA_TABLE = "camera";
    public static final String LAST_CAMERA_USED = "last_camera";
    //Constants to store all links scanned.
    public static final String HISTORY_TABLE = "history";
    public static final String LINK_TEXT = "link_text";
    public static final String SCAN_DATE = "scan_date";
}
