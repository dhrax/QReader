package com.daisa.qreader;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.math.MathUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.graphics.ImageFormat.YUV_420_888;
import static android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE;
import static android.hardware.camera2.CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM;
import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
import static android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN;
import static android.hardware.camera2.CaptureRequest.CONTROL_AF_MODE;
import static android.hardware.camera2.CaptureRequest.SCALER_CROP_REGION;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static com.daisa.qreader.Constants.CAMERA_BACK;
import static com.daisa.qreader.Constants.CAMERA_FRONT;
import static com.daisa.qreader.Constants.DEFAULT_ZOOM_BAR_PROGRESS;
import static com.daisa.qreader.Constants.DEFAULT_ZOOM_FACTOR;
import static com.daisa.qreader.Constants.DEFAULT_ZOOM_SMOOTHER_VALUE;
import static com.daisa.qreader.Constants.MAX_PREVIEW_HEIGHT;
import static com.daisa.qreader.Constants.MAX_PREVIEW_WIDTH;
import static com.daisa.qreader.Constants.REQUEST_CAMERA_PERMISSION;
import static com.daisa.qreader.Constants.REQUEST_READ_EXTERNAL_STORAGE_PERMISSION;
import static com.daisa.qreader.Constants.RESULT_LOAD_IMG;

//fixme large images take too much time to process
public class CameraPreviewActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    private ImageButton zoomOut;

    /**
     * A {@link SeekBar} to manage the zoom level.
     */
    private SeekBar zoomBar;

    private ImageButton zoomIn;

    /**
     * An {@link ImageButton} to switch between our front and our back cameras.
     */
    private ImageButton switchCamera;

    /**
     * An {@link ImageButton} to toggle the flash in our camera.
     */
    private ImageButton toggleFlash;

    /**
     * A {@link Space} used to separate {@link CameraPreviewActivity#switchCamera} and {@link CameraPreviewActivity#toggleFlash}.
     */
    private Space spacerTop;

    /**
     * A {@link NavigationView} to access other functions inside our {@link CameraPreviewActivity}.
     */
    NavigationView navigationView;

    /**
     * A {@link DrawerLayout} inside our {@link CameraPreviewActivity}.
     */
    DrawerLayout drawerLayout;

    /**
     * Integer that saves our camera facing direction.
     * <br>
     * See: {@link CameraCharacteristics#LENS_FACING}
     */
    //private int mCameraLensFacingDirection;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    /**
     * Whether the flash is turned on or off.
     */
    private boolean mIsFlashOn;

    /**
     * Whether the current camera device supports Zoom or not.
     */
    private boolean mZoomSupported;

    /**
     * Max zoom value supported by the {@link CameraDevice}.
     */
    public float maxZoom;

    /**
     * See {@link CameraCharacteristics#SENSOR_INFO_ACTIVE_ARRAY_SIZE}.
     */
    private Rect mSensorSize;

    /**
     * A {@link Rect}angle which takes the cropped size (in pixels) of our camera when the {@link CameraPreviewActivity#mSensorSize}
     * has been changed.
     */
    private Rect mCropRegion = new Rect();

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult result) {
                }

                @Override
                public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                                @NonNull CaptureResult partialResult) {
                    process(partialResult);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    process(result);
                }

            };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Instance of {@link QRCodeReader} used to decode the image of a QR code
     */
    private QRCodeReader mQrReader;

    /**
     * Instance of {@link Database} used to access the phone internal database.
     */
    private Database db;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved / processed.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    try (Image img = reader.acquireLatestImage()) {
                        if (img == null)
                            throw new NullPointerException("img cannot be null.");

                        int width = img.getWidth();
                        int height = img.getHeight();
                        ByteBuffer buffer = img.getPlanes()[0].getBuffer();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);

                        //Deprecated: PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height);
                        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false);
                        decodeBitmap(source);

                    } catch (NullPointerException | FormatException | ChecksumException | NotFoundException ex) {
                        ex.printStackTrace();
                    } finally {
                        mQrReader.reset();
                    }
                }

            };

    /**
     * Method used to decode an image ({@link BinaryBitmap}) into a link.
     *
     * @param source A {@link PlanarYUVLuminanceSource} object if we are decoding an image from the camera
     *               or a {@link RGBLuminanceSource} if we are decoding a still image (which isn't created by our camera).
     * @throws FormatException
     * @throws ChecksumException
     * @throws NotFoundException
     */
    private void decodeBitmap(LuminanceSource source) throws FormatException, ChecksumException, NotFoundException {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        onQRCodeRead(mQrReader.decode(bitmap).getText());
    }

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        initializeElements();
        createListeners();
    }

    private void initializeElements() {
        //Layout elements
        mTextureView = findViewById(R.id.texture);
        zoomOut = findViewById(R.id.zoomOut);
        zoomBar = findViewById(R.id.zoomBar);
        zoomIn = findViewById(R.id.zoomIn);

        switchCamera = findViewById(R.id.switchCamera);
        toggleFlash = findViewById(R.id.toggleFlash);
        spacerTop = findViewById(R.id.spacerTop);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        //other elements
        mIsFlashOn = false;

        mQrReader = new QRCodeReader();

        db = new Database(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void createListeners() {

        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (mZoomSupported && mPreviewRequestBuilder != null && mCaptureSession != null) {
                    //We add DEFAULT_ZOOM_SMOOTHER_VALUE to the progress so the first section (0 * DEFAULT_ZOOM_SMOOTHER_VALUE)
                    // and the second one (1 * DEFAULT_ZOOM_SMOOTHER_VALUE) don't stay in the same zoom level
                    setZoom((progress + DEFAULT_ZOOM_SMOOTHER_VALUE) * DEFAULT_ZOOM_BAR_PROGRESS);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

        });

        zoomOut.setOnClickListener(this);
        zoomIn.setOnClickListener(this);
        switchCamera.setOnClickListener(this);
        toggleFlash.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_gallery:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
                } else {
                    openGallery();
                }
                break;
            case R.id.nav_history:
                Intent historyIntent = new Intent(this, HistoryActivity.class);
                startActivity(historyIntent);
                break;
            case R.id.nav_favorites:
                Intent favoriteIntent = new Intent(this, FavoriteActivity.class);
                startActivity(favoriteIntent);
                break;
            case R.id.nav_share:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "QReader");
                    String shareMessage = "This app is great, you should check it out!!\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID;
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "Choose one"));
                } catch (Exception ignored) {

                }
                break;
            case R.id.nav_email:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"companydaisa@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "I would like to talk about...");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.nav_preferences:
                Intent preferencesIntent = new Intent(this, PreferencesActivity.class);
                startActivity(preferencesIntent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {
            try {
                final Uri imageUri = data.getData();
                if (imageUri == null)
                    throw new NullPointerException("imageUri cannot be null.");

                final Bitmap selectedImage = BitmapFactory.decodeFile(FetchPath.getPath(this, imageUri));
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                int[] intArray = new int[selectedImage.getWidth() * selectedImage.getHeight()];
                selectedImage.getPixels(intArray, 0, selectedImage.getWidth(), 0, 0, selectedImage.getWidth(), selectedImage.getHeight());
                LuminanceSource source = new RGBLuminanceSource(selectedImage.getWidth(), selectedImage.getHeight(), intArray);
                decodeBitmap(source);

            } catch (FormatException e) {
                Toast.makeText(this, "Barcode with errors detected.", LENGTH_LONG).show();
                e.printStackTrace();
            } catch (ChecksumException e) {
                Toast.makeText(this, "Barcode checksum failed.", LENGTH_LONG).show();
                e.printStackTrace();
            } catch (NotFoundException e) {
                Toast.makeText(this, "QR or barcode not found.", LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                mQrReader.reset();
            }

        } else {
            Toast.makeText(this, "You haven't picked an image.", LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switchCamera:
                switchCameras();
                break;
            case R.id.toggleFlash:
                if (mFlashSupported) {
                    mIsFlashOn = !mIsFlashOn;
                    setFlash();
                }
                break;
            case R.id.zoomOut:
                if (zoomBar.getProgress() >= Constants.ZOOM_CHANGE_VALUE) {
                    zoomBar.setProgress(zoomBar.getProgress() - Constants.ZOOM_CHANGE_VALUE);
                    setZoom((zoomBar.getProgress() + DEFAULT_ZOOM_SMOOTHER_VALUE) * DEFAULT_ZOOM_BAR_PROGRESS);
                } else if (zoomBar.getProgress() > 0) {
                    setZoom(0);
                }
                break;
            case R.id.zoomIn:
                if (zoomBar.getProgress() <= zoomBar.getMax() - Constants.ZOOM_CHANGE_VALUE) {
                    zoomBar.setProgress(zoomBar.getProgress() + Constants.ZOOM_CHANGE_VALUE);
                    setZoom((zoomBar.getProgress() + DEFAULT_ZOOM_SMOOTHER_VALUE) * DEFAULT_ZOOM_BAR_PROGRESS);
                } else if (zoomBar.getProgress() < zoomBar.getMax()) {
                    setZoom(zoomBar.getMax());
                }
                break;
            default:
                break;
        }
    }

    /**
     * Switch between our front camera and our back camera.
     */
    private void switchCameras() {
        Log.d("DEBUG", "Before last camera used update::" + db.selectLastCameraUsed());
        if (mCameraId.equals(CAMERA_BACK)) {
            db.updateLastCameraUsed(CAMERA_FRONT);
        } else {
            db.updateLastCameraUsed(CAMERA_BACK);
        }

        Log.d("DEBUG", "After last camera used update::" + db.selectLastCameraUsed());

        closeCamera();
        reopenCamera();
    }

    /**
     * Method that changes the zoom value of our {@link CameraDevice}.
     *
     * @param zoom new zoom value
     */
    private void setZoom(float zoom) {

        final float newZoom = MathUtils.clamp(zoom, DEFAULT_ZOOM_FACTOR, maxZoom);

        final int centerX = mSensorSize.width() / 2;
        final int centerY = mSensorSize.height() / 2;
        final int deltaX = (int) ((0.5f * mSensorSize.width()) / newZoom);
        final int deltaY = (int) ((0.5f * mSensorSize.height()) / newZoom);

        mCropRegion.set(centerX - deltaX,
                centerY - deltaY,
                centerX + deltaX,
                centerY + deltaY);

        mPreviewRequestBuilder.set(SCALER_CROP_REGION, mCropRegion);

        mPreviewRequest = mPreviewRequestBuilder.build();
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        reopenCamera();
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);
            Surface mImageSurface = mImageReader.getSurface();

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mImageSurface);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(Arrays.asList(mImageSurface, surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                mPreviewRequestBuilder.set(CONTROL_AF_MODE, CONTROL_AF_STATE_ACTIVE_SCAN);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Configure failed");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the camera specified by {@link CameraPreviewActivity#mCameraId}.
     */
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION);
            return;
        }
        //We get the last camera value stored in our database to keep it stored even if the app is closed.
        String lastCamera = db.selectLastCameraUsed();
        Log.d("DEBUG openCamera", "LastCameraUsed:" + lastCamera);
        if (lastCamera == null) {
            mCameraId = CAMERA_BACK;
        } else {
            mCameraId = lastCamera;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            assert manager != null;
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            //We subtract 1 to the maxZoom value because we start from 0
            zoomBar.setMax(((int) maxZoom - 1) * DEFAULT_ZOOM_SMOOTHER_VALUE);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            assert manager != null;
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);

            /*Integer facing = characteristics.get(LENS_FACING);
            //LENS_FACING_BACK == Front camera -- LENS_FACING_FRONT == Back camera
            if (facing != null)
                mCameraLensFacingDirection = facing;

             */

            StreamConfigurationMap map = characteristics.get(SCALER_STREAM_CONFIGURATION_MAP);

            zoomSettings(characteristics);
            flashSettings(characteristics);

            int mImageformat = YUV_420_888;

            // For still image captures, we use the largest available size.
            if (map != null) {
                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(mImageformat)), new Util.CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth() / 16, largest.getHeight() / 16, mImageformat, /*maxImages*/2);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);


                Point displaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(displaySize);
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH)
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT)
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;

                // Danger! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = Util.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, maxPreviewWidth, maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            Toast.makeText(CameraPreviewActivity.this, "Camera2 API not supported on this device", LENGTH_LONG).show();
        }
    }

    /**
     * Father method of all zoom's methods related.
     *
     * @param characteristics {@link CameraCharacteristics} of our {@link CameraDevice}
     *                        <br><br>
     *                        See also:
     *                        <br>
     *                        {@link #isZoomSupported(CameraCharacteristics)}
     *                        <br>
     *                        {@link #setUpZoomBar()}
     */
    private void zoomSettings(CameraCharacteristics characteristics) {
        isZoomSupported(characteristics);

        setUpZoomBar();
    }

    /**
     * Whether zoom is supported or not.
     *
     * @param characteristics {@link CameraCharacteristics} of our {@link CameraDevice}
     */
    private void isZoomSupported(CameraCharacteristics characteristics) {
        mSensorSize = characteristics.get(SENSOR_INFO_ACTIVE_ARRAY_SIZE);

        if (mSensorSize == null) {
            maxZoom = DEFAULT_ZOOM_FACTOR;
            mZoomSupported = false;
        } else {
            final Float value = characteristics.get(SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);

            maxZoom = ((value == null) || (value < DEFAULT_ZOOM_FACTOR)) ? DEFAULT_ZOOM_FACTOR : value;

            mZoomSupported = (Float.compare(maxZoom, DEFAULT_ZOOM_FACTOR) > 0);
        }
    }

    /**
     * Set the visibility of our {@link SeekBar} depending of whether zoom is supported or not by the actual {@link CameraDevice}.
     */
    public void setUpZoomBar() {
        if (mZoomSupported) {
            zoomBar.setVisibility(VISIBLE);
        } else {
            zoomBar.setVisibility(GONE);
        }
    }

    /**
     * Father method of all flash's methods related.
     *
     * @param characteristics {@link CameraCharacteristics} of our {@link CameraDevice}
     *                        See also:
     *                        <br>
     *                        {@link #isFlashSupported(CameraCharacteristics)}
     *                        <br>
     *                        {@link #setupFlashButton()}
     */
    private void flashSettings(CameraCharacteristics characteristics) {
        isFlashSupported(characteristics);

        setupFlashButton();
    }

    /**
     * Whether we have to turn the flash on or not
     */
    private void setFlash() {
        if (mIsFlashOn) {
            mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            toggleFlash.setImageResource(R.drawable.flash);
        } else {
            mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            try {
                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            toggleFlash.setImageResource(R.drawable.flash_off);
        }
    }

    /**
     * Set the visibility of our flash {@link ImageButton} depending of whether flash is supported or not by the actual {@link CameraDevice}.
     */
    public void setupFlashButton() {
        if (mCameraId.equals(CAMERA_BACK) && mFlashSupported) {
            toggleFlash.setVisibility(VISIBLE);
            spacerTop.setVisibility(VISIBLE);

            if (mIsFlashOn) {
                toggleFlash.setImageResource(R.drawable.flash);
            } else {
                toggleFlash.setImageResource(R.drawable.flash_off);
            }

        } else {
            toggleFlash.setVisibility(GONE);
            spacerTop.setVisibility(GONE);
        }
    }

    /**
     * Whether flash is supported or not.
     *
     * @param characteristics {@link CameraCharacteristics} of our {@link CameraDevice}
     */
    private void isFlashSupported(CameraCharacteristics characteristics) {
        Boolean available = characteristics.get(FLASH_INFO_AVAILABLE);
        mFlashSupported = available == null ? false : available;
    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    /**
     * Generic method to request permissions from the user.
     *
     * @param permission   android.permission.* style-string
     * @param permissionID ID of the permission we want to request
     */
    private void requestPermission(String permission, int permissionID) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, permissionID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CameraPreviewActivity.this, "ERROR: Camera permissions not granted", LENGTH_LONG).show();
                    finish();
                }
                break;
            case REQUEST_READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(CameraPreviewActivity.this, "ERROR: External storage permissions not granted", LENGTH_LONG).show();
                } else {
                    openGallery();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (mTextureView == null || mPreviewSize == null) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Creates the {@link ResultActivity} with the link of the QR decoded
     *
     * @param text link decoded
     */
    public void onQRCodeRead(final String text) {
        Intent intent = new Intent(this, ResultActivity.class);

        intent.putExtra("link", text);

        startActivity(intent);
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraPreviewActivity.this, text, LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Whether we have to open our camera or we have to wait until the surface is ready in
     * the {@link android.view.TextureView.SurfaceTextureListener}
     */
    private void reopenCamera() {
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

}