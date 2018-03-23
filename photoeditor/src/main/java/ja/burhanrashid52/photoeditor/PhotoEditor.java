package ja.burhanrashid52.photoeditor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import ja.burhanrashid52.task.StickerTask;
import ja.burhanrashid52.utils.BitmapUtils;
import ja.burhanrashid52.views.CustomPaintView;
import ja.burhanrashid52.views.StickerItem;
import ja.burhanrashid52.views.StickerView;
import ja.burhanrashid52.views.TextStickerView;

/**
 * Created by Burhanuddin Rashid on 18/01/2017.
 */

public class PhotoEditor {

    private static final String TAG = PhotoEditor.class.getSimpleName();
    private final LayoutInflater mLayoutInflater;
    private Context context;
    private RelativeLayout parentView;
    private ImageView imageView;
    private View deleteView;
    private CustomPaintView brushDrawingView;
    private List<View> addedViews;
    private List<View> redoViews;
    private OnPhotoEditorListener mOnPhotoEditorListener;
    private boolean isTextPinchZoomable;
    private Typeface mDefaultTextTypeface;
    private Typeface mDefaultEmojiTypeface;

    private StickerTask mSaveTask;
    private SaveFinalImageTask mSaveImageTask;

    private PhotoEditor(Builder builder) {
        this.context = builder.context;
        this.parentView = builder.parentView;
        this.imageView = builder.imageView;
        this.deleteView = builder.deleteView;
        this.brushDrawingView = builder.brushDrawingView;
        this.isTextPinchZoomable = builder.isTextPinchZoomable;
        this.mDefaultTextTypeface = builder.textTypeface;
        this.mDefaultEmojiTypeface = builder.emojiTypeface;

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //brushDrawingView.setBrushViewChangeListener(this);
        addedViews = new ArrayList<>();
        redoViews = new ArrayList<>();
    }

    public void addImage(final Bitmap desiredImage) {

        hideHelpBoxes(null);

        final View imageRootView = getLayout(ViewType.IMAGE);
        final StickerView imageView = imageRootView.findViewById(R.id.imgPhotoEditorImage);

        imageView.post(new Runnable() {
            @Override
            public void run() {
                imageView.addBitImage(desiredImage);
            }
        });

        addViewToParent(imageRootView, ViewType.IMAGE);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addText(String text, final int colorCodeTextView) {
        addText(null, text, colorCodeTextView);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void addText(@Nullable Typeface textTypeface, String text, final int colorCodeTextView) {

        hideHelpBoxes(null);

        brushDrawingView.setBrushDrawingMode(false);

        final View textRootView = getLayout(ViewType.TEXT);
        final TextStickerView textInputTv = textRootView.findViewById(R.id.tvPhotoEditorText);

        textInputTv.setText(text);
        textInputTv.setTextColor(colorCodeTextView);
        textInputTv.setTextListener(new TextStickerView.Listener() {
            @Override
            public void onTextSelected(TextStickerView currentTextView) {
                hideHelpBoxes(currentTextView);
            }

            @Override
            public void onTextDeleted(TextStickerView view) {
                removeView(view);
            }

            @Override
            public void onDoubleTap(TextStickerView view) {
                if (mOnPhotoEditorListener != null) {
                    final String textInput = view.getText();
                    final int currentTextColor = view.getTextColor();

                    mOnPhotoEditorListener.onEditTextChangeListener(textRootView, textInput, currentTextColor);
                }
            }
        });

        if (textTypeface != null) {
            //textInputTv.setTypeface(textTypeface);
        }

        addViewToParent(textRootView, ViewType.TEXT);
    }

    public void editText(View view, String inputText, int colorCode) {
        editText(view, null, inputText, colorCode);
    }

    /**
     * This will update the text and color on provided view
     *
     * @param view         root view where text view is a child
     * @param textTypeface optional if provided
     * @param inputText    text to update textview
     * @param colorCode    color to update on textview
     */
    public void editText(View view, Typeface textTypeface, String inputText, int colorCode) {
        TextStickerView inputTextView = view.findViewById(R.id.tvPhotoEditorText);
        if (inputTextView != null && addedViews.contains(view) && !TextUtils.isEmpty(inputText)) {
            inputTextView.setText(inputText);
            if (textTypeface != null) {
                //inputTextView.setTypeface(textTypeface);
            }
            inputTextView.setTextColor(colorCode);
            parentView.updateViewLayout(view, view.getLayoutParams());
            int i = addedViews.indexOf(view);
            if (i > -1) addedViews.set(i, view);
        }
    }

    public void addEmoji(String emojiName) {
        addEmoji(null, emojiName);
    }

    public void addEmoji(Typeface emojiTypeface, String emojiName) {
        // Disable all text help box
        hideHelpBoxes(null);

        final View emojiRootView = getLayout(ViewType.EMOJI);
        final TextStickerView emojiTextView = emojiRootView.findViewById(R.id.tvPhotoEditorText);

        if (emojiTypeface != null) {
            //emojiTextView.setTypeface(emojiTypeface);
        }

        emojiTextView.setText(emojiName);
        emojiTextView.setTextListener(new TextStickerView.Listener() {
            @Override
            public void onTextSelected(TextStickerView currentTextView) {
                hideHelpBoxes(currentTextView);
            }

            @Override
            public void onTextDeleted(TextStickerView view) {
                removeView(view);
            }

            @Override
            public void onDoubleTap(TextStickerView view) {
                if (mOnPhotoEditorListener != null) {
                    final String textInput = view.getText();
                    final int currentTextColor = view.getTextColor();

                    mOnPhotoEditorListener.onEditTextChangeListener(emojiRootView, textInput, currentTextColor);
                }
            }
        });

        addViewToParent(emojiRootView, ViewType.EMOJI);
    }


    /**
     * Add to root view from image,emoji and text to our parent view
     *
     * @param rootView rootview of image,text and emoji
     */
    private void addViewToParent(View rootView, ViewType viewType) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        parentView.addView(rootView, params);
        addedViews.add(rootView);
        if (mOnPhotoEditorListener != null)
            mOnPhotoEditorListener.onAddViewListener(viewType, addedViews.size());
    }

    /**
     * Get root view by its type i.e image,text and emoji
     *
     * @param viewType image,text or emoji
     * @return rootview
     */
    private View getLayout(ViewType viewType) {
        View rootView = null;
        switch (viewType) {
            case TEXT:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
                TextStickerView txtText = rootView.findViewById(R.id.tvPhotoEditorText);
                if (txtText != null && mDefaultTextTypeface != null) {
                    //txtText.setGravity(Gravity.CENTER);
                    if (mDefaultEmojiTypeface != null) {
                        //txtText.setTypeface(mDefaultTextTypeface);
                    }
                }
                break;
            case IMAGE:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_image, null);
                break;
            case EMOJI:
                rootView = mLayoutInflater.inflate(R.layout.view_photo_editor_text, null);
                TextStickerView txtTextEmoji = rootView.findViewById(R.id.tvPhotoEditorText);
                if (txtTextEmoji != null) {
                    txtTextEmoji.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                break;
        }

        return rootView;
    }

    public void setBrushDrawingMode(boolean brushDrawingMode) {
        if (brushDrawingView != null)
            brushDrawingView.setBrushDrawingMode(brushDrawingMode);
    }

    public Boolean getBrushDrawableMode() {
        return brushDrawingView != null && brushDrawingView.getBrushDrawingMode();
    }

    public void setBrushSize(float size) {
        if (brushDrawingView != null)
            brushDrawingView.setWidth(size);
    }

    public void setOpacity(@IntRange(from = 0, to = 100) int opacity) {
        if (brushDrawingView != null) {
            opacity = (int) ((opacity / 100.0) * 255.0);
            brushDrawingView.setOpacity(opacity);
        }
    }

    public void setBrushColor(@ColorInt int color) {
        if (brushDrawingView != null)
            brushDrawingView.setColor(color);
    }

    public void brushEraser(boolean eraser) {
        if (brushDrawingView != null)
            brushDrawingView.setEraser(eraser);
    }

    private void viewUndo() {
        if (addedViews.size() > 0) {
            parentView.removeView(addedViews.remove(addedViews.size() - 1));
            if (mOnPhotoEditorListener != null)
                mOnPhotoEditorListener.onRemoveViewListener(addedViews.size());
        }
    }

    private void viewUndo(View removedView) {
        if (addedViews.size() > 0) {
            if (addedViews.contains(removedView)) {
                parentView.removeView(removedView);
                addedViews.remove(removedView);
                redoViews.add(removedView);
                if (mOnPhotoEditorListener != null)
                    mOnPhotoEditorListener.onRemoveViewListener(addedViews.size());
            }
        }
    }

    public boolean undo() {
        if (addedViews.size() > 0) {
            View removeView = addedViews.get(addedViews.size() - 1);
            if (removeView instanceof CustomPaintView) {
                //return brushDrawingView != null && brushDrawingView.reset();
                return true;
            } else {
                addedViews.remove(addedViews.size() - 1);
                parentView.removeView(removeView);
                redoViews.add(removeView);
            }
            if (mOnPhotoEditorListener != null) {
                mOnPhotoEditorListener.onRemoveViewListener(addedViews.size());
            }
        }
        return addedViews.size() != 0;
    }

    public boolean redo() {
        if (redoViews.size() > 0) {
            View redoView = redoViews.get(redoViews.size() - 1);
            if (redoView instanceof CustomPaintView) {
                //return brushDrawingView != null && brushDrawingView.redo();
                return true;
            } else {
                redoViews.remove(redoViews.size() - 1);
                parentView.addView(redoView);
                addedViews.add(redoView);
            }
        }
        return redoViews.size() != 0;
    }

    private void clearBrushAllViews() {
        if (brushDrawingView != null)
            brushDrawingView.reset();
    }

    public void clearAllViews() {
        for (int i = 0; i < addedViews.size(); i++) {
            parentView.removeView(addedViews.get(i));
        }
        if (addedViews.contains(brushDrawingView)) {
            parentView.addView(brushDrawingView);
        }
        addedViews.clear();
        redoViews.clear();
    }

    public interface OnSaveListener {
        void onSuccess(@NonNull String imagePath);

        void onFailure(@NonNull Exception exception);
    }

    public void saveImage(@NonNull final String imagePath, @NonNull final OnSaveListener onSaveListener) {
        final List<View> stickerViews = new ArrayList<>();

        for (View view : addedViews) {
            if (view instanceof FrameLayout) {
                final TextStickerView textStickerView = view.findViewById(R.id.tvPhotoEditorText);
                final StickerView stickerView = view.findViewById(R.id.imgPhotoEditorImage);

                if (textStickerView != null) {
                    stickerViews.add(textStickerView);
                }

                if (stickerView != null) {
                    stickerViews.add(stickerView);
                }
            }
        }

        if (mSaveTask != null) {
            mSaveTask.cancel(true);
        }

        if ((stickerViews.size() > 0 || brushDrawingView.getPaintBit() != null) && parentView instanceof PhotoEditorView) {
            clearAllViews();

            mSaveTask = new SaveStickerTask(imagePath, stickerViews, imageView, onSaveListener);

            final PhotoEditorView editorView = (PhotoEditorView) parentView;

            mSaveTask.execute(editorView.getMainBitmap());
        }
    }

    private final class SaveStickerTask extends StickerTask {

        private final List<View> mStickerViews;
        private final String mImagePath;
        private final OnSaveListener mListener;

        public SaveStickerTask(String path, List<View> stickerViews, ImageView imageView, OnSaveListener listener) {
            super(imageView);

            mStickerViews = stickerViews;

            mImagePath = path;
            mListener = listener;
        }

        @Override
        public void handleImage(Canvas canvas, Matrix m) {

            for (View view : mStickerViews) {

                if (view instanceof TextStickerView) {
                    float[] f = new float[9];
                    m.getValues(f);
                    int dx = (int) f[Matrix.MTRANS_X];
                    int dy = (int) f[Matrix.MTRANS_Y];
                    float scale_x = f[Matrix.MSCALE_X];
                    float scale_y = f[Matrix.MSCALE_Y];
                    canvas.save();
                    canvas.translate(dx, dy);
                    canvas.scale(scale_x, scale_y);

                    final TextStickerView textStickerView = (TextStickerView) view;

                    textStickerView.drawText(canvas, textStickerView.layout_x, textStickerView.layout_y, textStickerView.getScale(), textStickerView.getRotateAngle());

                    canvas.restore();
                } else if (view instanceof StickerView) {
                    final StickerView stickerView = (StickerView) view;

                    LinkedHashMap<Integer, StickerItem> addItems = stickerView.getBank();

                    for (Integer id : addItems.keySet()) {
                        StickerItem item = addItems.get(id);
                        item.matrix.postConcat(m);
                        canvas.drawBitmap(item.bitmap, item.matrix, null);
                    }
                }
            }

            if (brushDrawingView.getPaintBit() != null) {
                float[] f = new float[9];
                m.getValues(f);
                int dx = (int) f[Matrix.MTRANS_X];
                int dy = (int) f[Matrix.MTRANS_Y];
                float scale_x = f[Matrix.MSCALE_X];
                float scale_y = f[Matrix.MSCALE_Y];
                canvas.save();
                canvas.translate(dx, dy);
                canvas.scale(scale_x, scale_y);

                canvas.drawBitmap(brushDrawingView.getPaintBit(), 0, 0, null);

                canvas.restore();
            }
        }

        @Override
        public void onPostResult(Bitmap result) {
            clearBrushAllViews();

            if (mSaveImageTask != null) {
                mSaveImageTask.cancel(true);
            }

            mSaveImageTask = new SaveFinalImageTask(mImagePath, mListener);
            mSaveImageTask.execute(result);

            final PhotoEditorView editorView = (PhotoEditorView) parentView;

            editorView.setImageBitmap(result);
        }
    }

    private final class SaveFinalImageTask extends AsyncTask<Bitmap, Void, Boolean> {

        private final String mSaveFilePath;
        private final OnSaveListener mListener;

        public SaveFinalImageTask(String saveFilePath, OnSaveListener listener) {
            mSaveFilePath = saveFilePath;
            mListener = listener;
        }

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            if (TextUtils.isEmpty(mSaveFilePath)) {
                return false;
            }

            return BitmapUtils.saveBitmap(params[0], mSaveFilePath);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (mListener != null) {
                if (result) {
                    mListener.onSuccess(mSaveFilePath);
                } else {
                    mListener.onFailure(new IllegalStateException("Unable to save image"));
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @RequiresPermission(allOf = {Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void savePreview(@NonNull final String imagePath, @NonNull final OnSaveListener onSaveListener) {
        Log.d(TAG, "Image Path: " + imagePath);
        new AsyncTask<String, String, Exception>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                hideHelpBoxes(null);
                parentView.setDrawingCacheEnabled(false);
            }

            @SuppressLint("MissingPermission")
            @Override
            protected Exception doInBackground(String... strings) {
                // Create a media file name
                File file = new File(imagePath);
                try {
                    FileOutputStream out = new FileOutputStream(file, false);
                    if (parentView != null) {
                        parentView.setDrawingCacheEnabled(true);
                        Bitmap drawingCache = parentView.getDrawingCache();
                        drawingCache.compress(Bitmap.CompressFormat.PNG, 100, out);
                    }
                    out.flush();
                    out.close();
                    Log.d(TAG, "Filed Saved Successfully");
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "Failed to save File");
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Exception e) {
                super.onPostExecute(e);
                if (e == null) {
                    clearAllViews();
                    onSaveListener.onSuccess(imagePath);
                } else {
                    onSaveListener.onFailure(e);
                }
            }

        }.execute();
    }

    private void removeView(@NonNull View currentView) {

        Iterator<View> iterator = addedViews.iterator();

        while (iterator.hasNext()) {
            View view = iterator.next();

            if (view instanceof FrameLayout) {
                final TextStickerView textStickerView = view.findViewById(R.id.tvPhotoEditorText);
                final StickerView stickerView = view.findViewById(R.id.imgPhotoEditorImage);

                if (currentView == textStickerView || currentView == stickerView) {
                    addedViews.remove(view);
                    parentView.removeView(view);

                    break;
                }
            }
        }
    }

    private void hideHelpBoxes(@Nullable View currentView) {
        // Disable all text help box
        for (View view : addedViews) {
            if (view instanceof FrameLayout) {
                final TextStickerView textStickerView = view.findViewById(R.id.tvPhotoEditorText);
                final StickerView stickerView = view.findViewById(R.id.imgPhotoEditorImage);

                if (currentView == null) {
                    if (textStickerView != null) textStickerView.hideHelpBox();
                    if (stickerView != null) stickerView.hideHelpBox();
                } else {
                    if (textStickerView != null && currentView != textStickerView) {
                        textStickerView.hideHelpBox();
                    }

                    if (stickerView != null && currentView != stickerView) {
                        stickerView.hideHelpBox();
                    }
                }
            }
        }
    }

    private boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    private static String convertEmoji(String emoji) {
        String returnedEmoji;
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = getEmojiByUnicode(convertEmojiToInt);
        } catch (NumberFormatException e) {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }

    private static String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    public void setOnPhotoEditorListener(@NonNull OnPhotoEditorListener onPhotoEditorListener) {
        this.mOnPhotoEditorListener = onPhotoEditorListener;
    }

    /**
     * Check if any changes made need to save
     *
     * @return true is nothing is there to change
     */
    public boolean isCacheEmpty() {
        return addedViews.size() == 0 && redoViews.size() == 0;
    }

    public static class Builder {

        private Context context;
        private RelativeLayout parentView;
        private ImageView imageView;
        private View deleteView;
        private CustomPaintView brushDrawingView;
        private Typeface textTypeface;
        private Typeface emojiTypeface;
        //By Default pinch zoom on text is enabled
        private boolean isTextPinchZoomable = true;

        public Builder(Context context, PhotoEditorView photoEditorView) {
            this.context = context;
            parentView = photoEditorView;
            imageView = photoEditorView.getSource();
            brushDrawingView = photoEditorView.getBrushDrawingView();
        }

        Builder setDeleteView(View deleteView) {
            this.deleteView = deleteView;
            return this;
        }

        public Builder setDefaultTextTypeface(Typeface textTypeface) {
            this.textTypeface = textTypeface;
            return this;
        }

        public Builder setDefaultEmojiTypeface(Typeface emojiTypeface) {
            this.emojiTypeface = emojiTypeface;
            return this;
        }

        public Builder setPinchTextScalable(boolean isTextPinchZoomable) {
            this.isTextPinchZoomable = isTextPinchZoomable;
            return this;
        }

        Builder setBrushDrawingView(CustomPaintView brushDrawingView) {
            this.brushDrawingView = brushDrawingView;
            return this;
        }

        public PhotoEditor build() {
            return new PhotoEditor(this);
        }
    }

    public static ArrayList<String> getEmojis(Context context) {
        ArrayList<String> convertedEmojiList = new ArrayList<>();
        String[] emojiList = context.getResources().getStringArray(R.array.photo_editor_emoji);
        for (String emojiUnicode : emojiList) {
            convertedEmojiList.add(convertEmoji(emojiUnicode));
        }
        return convertedEmojiList;
    }
}
