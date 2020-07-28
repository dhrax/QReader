package com.example.qreader;


/**
 * Constants used throughout the application
 */
public class Constants {

    /**
     * Max preview width guaranteed by Camera2 API
     */
    public static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height guaranteed by Camera2 API
     */
    public static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * Camera's permission request ID
     */
    public static final int REQUEST_CAMERA_PERMISSION = 1;

    /**
     * Zoom value if the camera device doesn't support zoom
     */
    public static final float DEFAULT_ZOOM_FACTOR =  1.0f;

    /**
     * Value used to smooth the zoom transition
     */
    public static final int DEFAULT_ZOOM_SMOOTHER_VALUE = 10;

    /**
     * Zoom progress value
     */
    public static final float DEFAULT_ZOOM_BAR_PROGRESS = 1f / DEFAULT_ZOOM_SMOOTHER_VALUE;

    /**
     * Id of our front camera.
     */
    public static final String CAMERA_FRONT = "1";

    /**
     * Id of our back camera.
     */
    public static final String CAMERA_BACK = "0";
}
