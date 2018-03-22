package ja.burhanrashid52.photoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import ja.burhanrashid52.utils.BitmapUtils;
import ja.burhanrashid52.views.imagezoom.ImageViewTouch;
import ja.burhanrashid52.views.imagezoom.ImageViewTouchBase;


/**
 * Created by Burhanuddin Rashid on 1/18/2018.
 */

public class PhotoEditorView extends RelativeLayout {

    private ImageViewTouch mImgSource;
    private BrushDrawingView mBrushDrawingView;
    private static final int imgSrcId = 1, brushSrcId = 2;
    private Bitmap mBitmap;
    private LoadImageTask mLoadImageTask;

    private int imageWidth, imageHeight;

    public PhotoEditorView(Context context) {
        super(context);
        init(null);
    }

    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PhotoEditorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    @SuppressLint("Recycle")
    private void init(@Nullable AttributeSet attrs) {
        //Setup image attributes
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        imageWidth = metrics.widthPixels / 2;
        imageHeight = metrics.heightPixels / 2;

        mImgSource = new ImageViewTouch(getContext(), attrs);
        mImgSource.setId(imgSrcId);
        RelativeLayout.LayoutParams imgSrcParam = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imgSrcParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        if (attrs != null) {

        }

        //Setup brush view
        mBrushDrawingView = new BrushDrawingView(getContext());
        mBrushDrawingView.setVisibility(GONE);
        mBrushDrawingView.setId(brushSrcId);
        //Align brush to the size of image view
        RelativeLayout.LayoutParams brushParam = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        brushParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        brushParam.addRule(RelativeLayout.ALIGN_TOP, imgSrcId);
        brushParam.addRule(RelativeLayout.ALIGN_BOTTOM, imgSrcId);


        //Add image source
        addView(mImgSource, imgSrcParam);
        //Add brush view
        addView(mBrushDrawingView, brushParam);
    }

    /**
     * Source image which you want to edit
     *
     * @return source ImageView
     */
    public ImageViewTouch getSource() {
        return mImgSource;
    }


    public Bitmap getMainBitmap() {
        return mBitmap;
    }

    public void updateBitmap(Bitmap bitmap) {
        mBitmap = bitmap;

        mImgSource.setImageBitmap(bitmap);
        mImgSource.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
    }

    public void loadImage(String filePath) {
        if (mLoadImageTask != null) {
            mLoadImageTask.cancel(true);
        }
        mLoadImageTask = new LoadImageTask();
        mLoadImageTask.execute(filePath);
    }

    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return BitmapUtils.getSampledBitmap(params[0], imageWidth,
                    imageHeight);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            updateBitmap(result);
        }
    }// end inner class

    BrushDrawingView getBrushDrawingView() {
        return mBrushDrawingView;
    }
}
