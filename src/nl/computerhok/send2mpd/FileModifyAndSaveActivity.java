package nl.computerhok.send2mpd;

import java.io.File;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class FileModifyAndSaveActivity extends Activity {
    public static final String TAG = FileModifyAndSaveActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "entering onCreate() ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Get the message from the intent
            Intent intent = getIntent();

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                MediaFile mediaFile = (MediaFile) bundle.getSerializable(MainActivity.EXTRA_MEDIAFILE);
                //                Log.e(TAG, "found in intent: " + mediaFile);

                // Create the text view
                TextView textView = new TextView(this);
                textView.setTextSize(20);

                AudioFile audioFile;
                try {
                    audioFile = AudioFileIO.read(new File(mediaFile.getFullpath()));
                    Tag tag = audioFile.getTag();

                    // set all the ID3 tags:
                    tag.setField(FieldKey.ALBUM, mediaFile.getAlbum());
                    tag.setField(FieldKey.ARTIST, mediaFile.getArtist());
                    tag.setField(FieldKey.TITLE, mediaFile.getTitle());
                    // the other tags from the file are considered read-only , and are not changed in this app

                    Log.e(TAG, "committing changes to " + audioFile.getFile().getCanonicalFile());
                    audioFile.commit();

                    intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(new File(mediaFile.getFullpath())));
                    sendBroadcast(intent);

                    AlertDialog.Builder builder = new AlertDialog.Builder(FileModifyAndSaveActivity.this);
                    builder.setMessage(R.string.msg_file_save_success).setTitle(R.string.title_file_save_success);
                    builder.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;

                } catch (Exception e) {
                    String errorMsg = "exception while read/writing mp3 file: \n" + e;
                    Log.e(TAG, errorMsg);
                    e.printStackTrace();
                    textView.setText(errorMsg);
                    textView.setTextColor(Color.RED);
                    setContentView(textView);
                    return;
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "entering onSaveInstanceState() ");
        super.onSaveInstanceState(outState);
    }

}
