package nl.computerhok.send2mpd;

import java.io.File;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public final static String EXTRA_MEDIAFILE = MainActivity.class.getPackage() + ".MEDIAFILE";
    public final static int ID_INTROTEXT = 4711;
    private MediaFile mediaFile;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "entering onCreate() ");

        Intent intent = getIntent();
        Log.e(TAG, "intent: " + intent);

        if (intent != null && intent.getExtras() != null) {
            setContentView(R.layout.activity_main);

            Bundle bundle = intent.getExtras();
            mediaFile = new MediaFile();
            Uri audioUri = bundle.getParcelable(Intent.EXTRA_STREAM);
            Log.e(TAG, "found stream " + audioUri);
            Set<String> bundleKeys = bundle.keySet();
            for (String key : bundleKeys) {
                Object value = bundle.get(key);
                Log.e(TAG, "key " + key + "=" + value + "  --  class:" + value.getClass());
            }

            String filename = "UNKNOWN_FILENAME";
            String fullpath = "UNKNOW_PATH";
            if ("file".equals(audioUri.getScheme())) {
                fullpath = audioUri.getPath();
                filename = Uri.decode(audioUri.getLastPathSegment());
            }

            if ("content".equals(audioUri.getScheme())) {
                fullpath = getRealPathFromURI(audioUri);
                filename = new File(fullpath).getName();
            }
            mediaFile.setFilename(filename);
            mediaFile.setFullpath(fullpath);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getApplicationContext(), audioUri);
            mediaFile.setAlbum(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            mediaFile.setArtist(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            mediaFile.setTitle(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            mediaFile.setBitrate(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
            mediaFile.setDuration(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            EditText editedField = (EditText) findViewById(R.id.filename);
            editedField.setText(filename, TextView.BufferType.NORMAL);

            editedField = (EditText) findViewById(R.id.album);
            editedField.setText(mediaFile.getAlbum(), TextView.BufferType.NORMAL);

            editedField = (EditText) findViewById(R.id.artist);
            editedField.setText(mediaFile.getArtist(), TextView.BufferType.NORMAL);

            editedField = (EditText) findViewById(R.id.title);
            editedField.setText(mediaFile.getTitle(), TextView.BufferType.NORMAL);

            TextView viewField = (TextView) findViewById(R.id.duration);
            viewField.setText(mediaFile.getDuration(), TextView.BufferType.NORMAL);

            viewField = (TextView) findViewById(R.id.bitrate);
            viewField.setText(mediaFile.getBitrate(), TextView.BufferType.SPANNABLE);

        } else {
//            Log.e(TAG, "intent was null, we were probably started from launcher");
            // remove the usual views and add an intro text and a button to the prefs
            LayoutInflater inflater = getLayoutInflater();
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.activity_main, null);
            layout.removeAllViews();
            TextView intro = new TextView(getApplicationContext());
            intro.setId(ID_INTROTEXT);
            intro.setText(R.string.introtext);
            intro.setTextSize(20);
            intro.setTextColor(Color.BLACK);
            intro.setBackgroundColor(Color.LTGRAY);
            intro.setPadding(15, 15, 15, 15);
            layout.addView(intro);

            Button button = new Button(getApplicationContext());
            button.setText(R.string.menu_settings);
            button.setOnClickListener(this);
            layout.addView(button);

            setContentView(layout);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /** Called when the user clicks the Save button */
    public void save(View view) {
        populateMediaFileFromView(view);
        Log.e(TAG, "saving " + mediaFile);
        Toast.makeText(getApplicationContext(), "saving " + mediaFile, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, FileModifyAndSaveActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_MEDIAFILE, mediaFile);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /** Called when the user clicks the Send button */
    public void send(View view) {
        populateMediaFileFromView(view);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data

            Log.e(TAG, "sending " + mediaFile);
            Intent intent = new Intent(this, FileSendActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(EXTRA_MEDIAFILE, mediaFile);
            intent.putExtras(bundle);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), R.string.msg_network_down, Toast.LENGTH_LONG).show();
        }
    }

    private void populateMediaFileFromView(View view) {
        EditText editText = (EditText) findViewById(R.id.artist);
        mediaFile.setArtist(editText.getText().toString());
        editText = (EditText) findViewById(R.id.album);
        mediaFile.setAlbum(editText.getText().toString());
        editText = (EditText) findViewById(R.id.title);
        mediaFile.setTitle(editText.getText().toString());
        TextView textView = (TextView) findViewById(R.id.bitrate);
        mediaFile.setBitrate(textView.getText().toString());
        textView = (TextView) findViewById(R.id.duration);
        mediaFile.setDuration(textView.getText().toString());
        editText = (EditText) findViewById(R.id.filename);
        mediaFile.setFilename(editText.getText().toString());
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Audio.Media.DATA };
        Cursor cursor = null;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
            cursor = cursorLoader.loadInBackground();
        } else {
            cursor = managedQuery(contentUri, proj, null, null, null);
        }
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String fullpath = cursor.getString(column_index);
        return fullpath;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.itemPrefs: {
            startActivity(new Intent(this, PrefsActivity.class));
        }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "entering onClick()");
        startActivity(new Intent(getApplicationContext(), PrefsActivity.class));
    }

}
