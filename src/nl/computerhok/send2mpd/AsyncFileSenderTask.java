package nl.computerhok.send2mpd;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.File;

/**
 * Asynchronous sending the file to the server.
 * Takes a {@link nl.computerhok.send2mpd.MediaFile} as parameter, and returns {@link Exception} as result (null if all went oke).
 *
 * @author metskem
 */
public class AsyncFileSenderTask extends AsyncTask<MediaFile, Integer, String> {
    private final String TAG = AsyncFileSenderTask.class.getSimpleName();
    private MainActivity mParentActivity;
    private Context mContext;
    private SharedPreferences mSharedPrefs;
    private final int MY_NOTIFICATION_ID = 471112345;


    public AsyncFileSenderTask(MainActivity parentActivity) {
        mParentActivity = parentActivity;
        mContext = parentActivity.getApplicationContext();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(parentActivity);
    }

    @Override
    protected String doInBackground(MediaFile... mediaFiles) {
        MediaFile mediaFile = mediaFiles[0];
        if (saveFile(mediaFile) == null) {

            FileSender sender = new FileSender(mSharedPrefs);

            try {
                sender.sendFile(mediaFile);
                sender.updateMPDDatabase();
            } catch (Exception e) {
                Log.e(TAG, "exception occurred in backgroundtask: " + e.getMessage());
                return e.getMessage();
            }
        }
        return "File successfully transferred to " + mSharedPrefs.getString(PrefsActivity.PREFS_HOSTNAME, "defaultHost");
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // prepare the Intent for when the user slides down the drawer and clicks then notification
        final Intent restartMainActivityIntent = new Intent(mContext, MainActivity.class);
        restartMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        final PendingIntent pendingIntent = PendingIntent.getActivity(mContext, MY_NOTIFICATION_ID, restartMainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // prepare the notification
        Notification.Builder notificationBuilder = new Notification.Builder(mContext)
                .setTicker(mContext.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(mContext.getString(R.string.app_name))
                .setContentText(result);

        // Send the notification
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MY_NOTIFICATION_ID, notificationBuilder.build());

        Log.i(TAG, "Notification sent: " + result);

    }

    /**
     * Save the file to the device.
     *
     * @param mediaFile the {@link nl.computerhok.send2mpd.MediaFile} to save
     * @return the error message, or null if all succeeds
     */

    private String saveFile(final MediaFile mediaFile) {
        try {
            mediaFile.save();
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(new File(mediaFile.getFullpath())));
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            return "exception while read/writing mp3 file: \n" + e.getMessage();
        }
        // all went well, return null
        return null;
    }
}
