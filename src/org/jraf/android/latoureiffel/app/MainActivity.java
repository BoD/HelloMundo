package org.jraf.android.latoureiffel.app;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.latoureiffel.util.HttpUtil;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class MainActivity extends SherlockActivity {
    private boolean mDisplayedPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (savedInstanceState != null) {
            mDisplayedPreview = savedInstanceState.getBoolean("mDisplayedPreview");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mDisplayedPreview) {
            displayPreview();
            mDisplayedPreview = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("mDisplayedPreview", mDisplayedPreview);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    private void displayPreview() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                final String url = "http://87.98.182.216/is/resize/www.parisrama.com/webcam9.jpg?__width=625&__height=475";
                try {
                    final InputStream inputStream = HttpUtil.getAsStream(url);
                    return BitmapFactory.decodeStream(inputStream);
                } catch (final IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result == null) {

                } else {
                    ((ImageView) findViewById(R.id.imgPreview)).setImageBitmap(result);
                }
            }
        }.execute();
    }
}