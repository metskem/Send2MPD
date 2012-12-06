package nl.computerhok.send2mpd;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import android.content.SharedPreferences;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class FileSender {

    private static final String TAG = FileSender.class.getSimpleName();
    JSch jsch = null;
    Session session = null;
    Channel channel = null;
    ChannelSftp c = null;
    SharedPreferences sharedPrefs;
    
    public FileSender(SharedPreferences sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
    }

    public void sendFile(final MediaFile mediaFile) throws Exception {
        String hostname = sharedPrefs.getString(PrefsActivity.PREFS_HOSTNAME, "defaultHost");
        String username = sharedPrefs.getString(PrefsActivity.PREFS_USERNAME, "defaultUser");
        String password = sharedPrefs.getString(PrefsActivity.PREFS_PASSWORD, "defaultPassword");
        String destdir = sharedPrefs.getString(PrefsActivity.PREFS_DESTDIR, "/tmp");
        jsch = new JSch();
        session = jsch.getSession(username, hostname, 22);
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(password);
        session.connect();

        channel = session.openChannel("sftp");
        channel.connect();
        c = (ChannelSftp) channel;
        c.put(mediaFile.getFullpath(), destdir + "/" + mediaFile.getTargetfilename());

        c.disconnect();
        

        // do a chmod for the file 
        String targetFullPath = destdir + "/" + mediaFile.getTargetfilename();
        String command = "ls -rtl '" + targetFullPath + "'; chmod 664 '" + targetFullPath + "'";

        channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);
        channel.setInputStream(null);
        ((ChannelExec) channel).setErrStream(System.err);
        InputStream in = channel.getInputStream();
        channel.connect();
        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0)
                    break;
                Log.e(TAG, (new String(tmp, 0, i)));
            }
            if (channel.isClosed()) {
                Log.e(TAG, "exit-status: " + channel.getExitStatus());
                break;
            }
        }
        channel.disconnect();

        session.disconnect();

    }

    public void updateMPDDatabase() throws UnknownHostException, IOException {
        Log.e(TAG, "updating MPD database");
        String hostname = sharedPrefs.getString(PrefsActivity.PREFS_HOSTNAME, "defaultHost");
        Socket socket = new Socket(hostname, 6600);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(),true);
        String[] commands = new String[] { "password " + sharedPrefs.getString(PrefsActivity.PREFS_MPD_PASSWORD, "defaultMPDpassword"), "update", "close" };
        for (String cmd : commands) {
            writer.println(cmd);
        }
        writer.close();
        socket.close();
    }
}
