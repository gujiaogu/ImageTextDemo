package com.tyrese.imagetextdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_FOR_IMAGE = 1;

    private EditText mEditText;
    private TextView mTextImage;
    private DisplayMetrics dm;
    private ImageView mImage;
    private final LruCache<String, Bitmap>
            cache = new LruCache<>(20);
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dm = getResources().getDisplayMetrics();
        mTextImage = (TextView) findViewById(R.id.text_image);
        mImage = (ImageView) findViewById(R.id.image);
        setImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_choose_image:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_CODE_FOR_IMAGE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (REQUEST_CODE_FOR_IMAGE == requestCode) {
//                Uri uri = data.getData();
//                int index = mEditText.getSelectionStart();
            }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String url,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream is = null;
        Rect rect = new Rect();
        Bitmap bitmap = null;
        try {
            URL u = new URL(url);
            URLConnection connection = u.openConnection();
            connection.connect();
            is = connection.getInputStream();
            BitmapFactory.decodeStream(is, rect, options);
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            connection = u.openConnection();
            is = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is, rect, options);
            StreamUtil.close(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return bitmap;
    }

    public void getWithHeight(View view) {
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();
        Toast.makeText(this, "width : " + width + " height : " + height, Toast.LENGTH_SHORT).show();
    }

    private class BitmapTask extends Thread {

        String url;
        LruCache cacheBitmap;

        public BitmapTask(String url, LruCache cache) {
            this.url = url;
            this.cacheBitmap = cache;
        }

        @Override
        public void run() {
            super.run();
            Bitmap bitmap = decodeSampledBitmapFromResource(url, dm.widthPixels, dm.heightPixels);
            if (bitmap != null) {
                this.cacheBitmap.put(url, bitmap);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setImage();
                    }
                });
            }
        }
    }

    private void setImage() {
        mTextImage.setText(Html.fromHtml("<p style=\"text-align:center;\"><img src=\"http://n1.itc.cn/img8/wb/smccloud/fetch/2015/07/01/72592671958821161.JPEG\" /></p>贾克斯", new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String s) {
                if (TextUtils.isEmpty(s) || !s.startsWith("http")) {
                    return null;
                }
                Bitmap bitmap = cache.get(s);
                Drawable drawable;
                if (bitmap == null) {
                    InputStream is = getResources().openRawResource(R.raw.test);
                    drawable = Drawable.createFromStream(is, "src");
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight());
                    StreamUtil.close(is);
                    new BitmapTask(s, cache).start();
                } else {
                    bitmap = cache.get(s);
                    drawable = new BitmapDrawable(getResources(), bitmap);
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight());
                }
                return drawable;
            }
        }, null));
    }
}
