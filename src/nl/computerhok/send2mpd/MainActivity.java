package nl.computerhok.send2mpd;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, TextWatcher {
    private static final String TAG = MainActivity.class.getSimpleName();
    public final static String EXTRA_MEDIAFILE = MainActivity.class.getPackage() + ".MEDIAFILE";
    public final static int ID_INTROTEXT = 4711;
    public final static int ID_VERSIONINFO = 4712;
    private MediaFile mediaFile;
    private SharedPreferences sharedPrefs = null;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        Intent intent = getIntent();

        if (intent != null && intent.getExtras() != null) {
            setContentView(R.layout.activity_main);

            Bundle bundle = intent.getExtras();
            mediaFile = new MediaFile();
            Uri audioUri = bundle.getParcelable(Intent.EXTRA_STREAM);

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
            mediaFile.setTargetfilename(filename);
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

            editedField = (EditText) findViewById(R.id.artist);
            editedField.setText(mediaFile.getArtist(), TextView.BufferType.NORMAL);
            editedField.addTextChangedListener(this);

            editedField = (EditText) findViewById(R.id.title);
            editedField.setText(mediaFile.getTitle(), TextView.BufferType.NORMAL);
            editedField.addTextChangedListener(this);

            editedField = (EditText) findViewById(R.id.album);
            editedField.setText(mediaFile.getAlbum(), TextView.BufferType.NORMAL);

            TextView viewField = (TextView) findViewById(R.id.duration);
            viewField.setText(mediaFile.getDuration(), TextView.BufferType.NORMAL);

            viewField = (TextView) findViewById(R.id.bitrate);
            viewField.setText(mediaFile.getBitrate(), TextView.BufferType.SPANNABLE);
            
            // make the target file name initially well propagated
            afterTextChanged(null);
        } else {
            //            Log.e(TAG, "intent was null, we were probably started from launcher");
            
            // remove the usual views and add an intro text and a button to the prefs
            LayoutInflater inflater = getLayoutInflater();
            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.activity_main, null);
            layout.removeAllViews();
            BorderedTextView intro = new BorderedTextView(getApplicationContext());
            intro.setId(ID_INTROTEXT);
            intro.setText(R.string.introtext);
            intro.setTextSize(20);
            intro.setTextColor(Color.BLACK);
            intro.setBackgroundColor(Color.LTGRAY);
            intro.setPadding(15, 15, 15, 15);
            layout.addView(intro);

            // add the version info to the welcome screen
            BorderedTextView versioninfo = new BorderedTextView(getApplicationContext());
            versioninfo.setId(ID_VERSIONINFO);
            versioninfo.setText("Version info: " + Send2MPDConstants.VERSION);
            versioninfo.setTextSize(15);
            versioninfo.setTextColor(Color.BLACK);
            versioninfo.setBackgroundColor(Color.LTGRAY);
            versioninfo.setGravity(Gravity.CENTER_HORIZONTAL);
            versioninfo.setPadding(15, 15, 15, 15);
            layout.addView(versioninfo);

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

    /** Called when the user clicks the Send button */
    public void send(View view) {
        populateMediaFileFromView(view);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (sharedPrefs.getString(PrefsActivity.PREFS_HOSTNAME, null) != null && sharedPrefs.getString(PrefsActivity.PREFS_HOSTNAME, null).length() > 0) {
                if (sharedPrefs.getString(PrefsActivity.PREFS_PORT, null) != null && sharedPrefs.getString(PrefsActivity.PREFS_PORT, null).length() > 0) {
                    if (sharedPrefs.getString(PrefsActivity.PREFS_USERNAME, null) != null && sharedPrefs.getString(PrefsActivity.PREFS_USERNAME, null).length() > 0) {
                        if (sharedPrefs.getString(PrefsActivity.PREFS_PASSWORD, null) != null && sharedPrefs.getString(PrefsActivity.PREFS_PASSWORD, null).length() > 0) {
                            if (sharedPrefs.getString(PrefsActivity.PREFS_DESTDIR, null) != null && sharedPrefs.getString(PrefsActivity.PREFS_DESTDIR, null).length() > 0) {
                                if (mediaFile.getTargetfilename() != null && mediaFile.getTargetfilename().length() > 0) {
                                    Log.e(TAG, "sending " + mediaFile);
                                    Intent intent = new Intent(this, FileSendActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable(EXTRA_MEDIAFILE, mediaFile);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.msg_null_targetfile, Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.msg_null_destdir, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.msg_null_password, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.msg_null_username, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.msg_null_port, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.msg_null_hostname, Toast.LENGTH_LONG).show();
            }
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
        startActivity(new Intent(getApplicationContext(), PrefsActivity.class));
    }

    @Override
    public void afterTextChanged(Editable s) {
        String fileName = null;
        EditText editText = (EditText) findViewById(R.id.artist);
        String artist = editText.getText().toString();
        editText = (EditText) findViewById(R.id.title);
        String title = editText.getText().toString();

        fileName = artist.trim() + " - " + title.trim() + ".mp3";
        editText = (EditText) findViewById(R.id.targetfilename);
        editText.setText(fileName);
        mediaFile.setTargetfilename(fileName);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // not implemented
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // not implemented
    }

}
