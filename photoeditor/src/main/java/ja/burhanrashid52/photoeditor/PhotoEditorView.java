package ja.burhanrashid52.photoeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import ja.burhanrashid52.views.CustomPaintView;


/**
 * Created by Burhanuddin Rashid on 1/18/2018.
 */

public class PhotoEditorView extends RelativeLayout {

    private ImageView mImgSource;
    private CustomPaintView mBrushDrawingView;
    private static final int imgSrcId = 1, brushSrcId = 2;
    private Bitmap mBitmap;

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

        mImgSource = new ImageView(getContext());
        mImgSource.setId(imgSrcId);
        RelativeLayout.LayoutParams imgSrcParam = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imgSrcParam.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        //Setup brush view
        mBrushDrawingView = new CustomPaintView(getContext());
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
    public ImageView getSource() {
        return mImgSource;
    }

    public Bitmap getMainBitmap() {
        return mBitmap;
    }

    public void setImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap;

        mImgSource.setImageBitmap(bitmap);
    }

    CustomPaintView getBrushDrawingView() {
        return mBrushDrawingView;
    }
}
