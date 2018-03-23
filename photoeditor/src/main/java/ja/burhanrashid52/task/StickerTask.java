package ja.burhanrashid52.task;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import ja.burhanrashid52.utils.Matrix3;

/**
 * Created by panyi on 2016/8/14.
 * <p/>
 * 贴图合成任务 抽象类
 */
public abstract class StickerTask extends AsyncTask<Bitmap, Void, Bitmap> {

    private final ImageView mImageView;

    public StickerTask(@NonNull ImageView imageView) {
        mImageView = imageView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(Bitmap... params) {
        // System.out.println("保存贴图!");
        Matrix touchMatrix = mImageView.getImageMatrix();

        Bitmap resultBit = Bitmap.createBitmap(params[0]).copy(
                Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(resultBit);

        float[] data = new float[9];
        touchMatrix.getValues(data);// 底部图片变化记录矩阵原始数据
        Matrix3 cal = new Matrix3(data);// 辅助矩阵计算类
        Matrix3 inverseMatrix = cal.inverseMatrix();// 计算逆矩阵
        Matrix m = new Matrix();
        m.setValues(inverseMatrix.getValues());

        handleImage(canvas, m);

        return resultBit;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCancelled(Bitmap result) {
        super.onCancelled(result);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);

        onPostResult(result);
    }

    public abstract void handleImage(Canvas canvas, Matrix m);

    public abstract void onPostResult(Bitmap result);

}//end class
