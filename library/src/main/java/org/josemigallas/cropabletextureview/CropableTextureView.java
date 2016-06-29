package org.josemigallas.cropabletextureview;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

public class CropableTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private final int videoRes;

    private MediaPlayer player;
    private int videoHeight;
    private int videoWidth;

    public CropableTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CropableTextureView,
                0,
                0
        );

        try {
            videoRes = typedArray.getInt(R.styleable.CropableTextureView_src, 0);
        } finally {
            typedArray.recycle();
        }
    }

    private void init() {
        setSurfaceTextureListener(this);
    }

    private void calculateVideoSize() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

        AssetFileDescriptor fileDescriptor = getResources().openRawResourceFd(videoRes);
        metaRetriever.setDataSource(
                fileDescriptor.getFileDescriptor(),
                fileDescriptor.getStartOffset(),
                fileDescriptor.getLength()
        );

        String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

        videoHeight = Integer.parseInt(height);
        videoWidth = Integer.parseInt(width);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        calculateVideoSize();
        centerCrop(width, height, videoWidth, videoHeight);

        player = MediaPlayer.create(getContext(), videoRes);
        player.setSurface(new Surface(surfaceTexture));
        player.setLooping(true);
        player.start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void centerCrop(int screenWidth, int screenHeight, int videoWidth, int videoHeight) {
        Matrix matrix = new Matrix();

        float screenRatio = (float) screenWidth / screenHeight;
        float videoRatio = (float) videoWidth / videoHeight;

        if (screenRatio < videoRatio) {
            float heightScale = (float) screenHeight / videoHeight;
            float newWidth = videoWidth * heightScale;

            float widthScale = newWidth / screenWidth;
            float offsetX = -(newWidth - screenWidth) / 2;

            getTransform(matrix);
            matrix.setScale(widthScale, 1);
            matrix.postTranslate(offsetX, 0);
        } else {
            float widthScale = (float) screenWidth / videoWidth;
            float newHeight = videoHeight * widthScale;

            float heightScale = newHeight / screenHeight;
            float offsetY = -(newHeight - screenHeight) / 2;

            getTransform(matrix);
            matrix.setScale(1, heightScale);
            matrix.postTranslate(0, offsetY);
        }

        setTransform(matrix);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (player != null) {
            player.release();
        }
    }
}
