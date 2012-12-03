package nl.computerhok.send2mpd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class FileSendActivity extends Activity implements OnSharedPreferenceChangeListener {
    private static final String TAG = FileSendActivity.class.getSimpleName();
    private SharedPreferences sharedPrefs = null;
    private AlertDialog dialog = null;
    private String hostname;
    private String destdir;
    private boolean sendRunning; 
    public static final String SEND_RUNNING = "sendRunning";
    private AlertDialog.Builder builder;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null){
            sendRunning = (Boolean) savedInstanceState.get(SEND_RUNNING);
        }
        Log.e(TAG, "entering onCreate() , instance " + this);
        if (!sendRunning) {
            sendRunning = true;
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPrefs.registerOnSharedPreferenceChangeListener(this);
            // Get the message from the intent
            Intent intent = getIntent();

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                MediaFile mediaFile = (MediaFile) bundle.getSerializable(MainActivity.EXTRA_MEDIAFILE);
                AsyncSendFileTask sendTask = new AsyncSendFileTask();
                sendTask.execute(mediaFile);
            }
        } else {
            builder = new AlertDialog.Builder(FileSendActivity.this);
            builder.setMessage(R.string.msg_file_send).setTitle(R.string.title_file_send);
            dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "entering onSaveInstanceState()");
        super.onSaveInstanceState(outState);
        outState.putBoolean(SEND_RUNNING, sendRunning);
        dialog.dismiss();
    }

    /**
     * Asynchronous sending the file to the server.
     * Takes a {@link MediaFile} as parameter, and returns {@link Exception} as result (null if all went oke).
     * 
     * @author metskem
     *
     *
     */
    public class AsyncSendFileTask extends AsyncTask<MediaFile, Integer, Exception> {
        private final String TAG = AsyncSendFileTask.class.getSimpleName();

        @Override
        protected Exception doInBackground(MediaFile... mediaFiles) {
            MediaFile mediaFile = mediaFiles[0];
            Log.e(TAG, "entering doInBackground() with " + mediaFile);
            sendRunning = true;

            FileSender sender = new FileSender(sharedPrefs);
            
            try {
                
                sender.sendFile(mediaFile);
                sender.updateMPDDatabase();
                
            } catch (Exception e) {
                Log.e(TAG, "exception occured in backgroundtask: " + e.getMessage());
                return e;
            } finally {
                sendRunning = false;
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e(TAG, "entering onCancelled()");
        }

        @Override
        protected void onPostExecute(Exception result) {
            super.onPostExecute(result);
            // dismiss the "progress dialog" first
            dialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(FileSendActivity.this);

            if (result != null) {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (notification == null) {
                    notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                }
                if (notification != null) {
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                }

                builder.setMessage(getResources().getString(R.string.msg_file_send_failed) + " : " + result.getMessage()).setTitle(R.string.title_file_send_failed);
                builder.setPositiveButton(R.string.button_filesend_go_prefs, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(getApplicationContext(), PrefsActivity.class));
                        finish();
                    }
                });

                builder.setNegativeButton(R.string.button_filesend_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });

                dialog = builder.create();
                dialog.show();
            } else {
                hostname = sharedPrefs.getString("hostname", "defaultHost");
              destdir = sharedPrefs.getString("destdir", "/tmp");
                builder.setMessage(getResources().getString(R.string.msg_file_send_success) + " " + hostname + ":" + destdir).setTitle(R.string.title_file_send_success);
                builder.setPositiveButton(R.string.button_OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        finish();
                    }
                });

                dialog = builder.create();
                dialog.show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e(TAG, "entering onPreExecute()");
            builder = new AlertDialog.Builder(FileSendActivity.this);
            builder.setMessage(R.string.msg_file_send).setTitle(R.string.title_file_send);
            dialog = builder.create();
            dialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.e(TAG, "entering onProgressUpdate() with values " + values);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.e(TAG, "onSharedPreferenceChanged()");
    }

}
