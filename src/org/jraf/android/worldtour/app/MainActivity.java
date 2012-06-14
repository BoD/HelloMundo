package org.jraf.android.worldtour.app;

import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.jraf.android.latoureiffel.R;
import org.jraf.android.worldtour.Constants;
import org.jraf.android.worldtour.model.WebcamManager;
import org.jraf.android.worldtour.util.HttpUtil;
import org.jraf.android.worldtour.util.IoUtil;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockActivity {
    private static final String TAG = Constants.TAG + MainActivity.class.getSimpleName();

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
            //            mDisplayedPreview = true;

            // TODO remove
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        WebcamManager.get().refreshDatabaseFromNetwork(MainActivity.this);
                    } catch (final IOException e) {
                        Log.e(TAG, "doInBackground", e);
                    }
                    return null;
                }

            }.execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("mDisplayedPreview", mDisplayedPreview);
        super.onSaveInstanceState(outState);
    }


    /*
     * Action bar.
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pick:
                startActivity(new Intent(this, PickWebcamActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void displayPreview() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                final String url = "http://87.98.182.216/is/resize/www.parisrama.com/webcam9.jpg?__width=625&__height=475";
                try {
                    final InputStream inputStream = HttpUtil.getAsStream(url);
                    try {
                        return BitmapFactory.decodeStream(inputStream);
                    } finally {
                        IoUtil.close(inputStream);
                    }
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