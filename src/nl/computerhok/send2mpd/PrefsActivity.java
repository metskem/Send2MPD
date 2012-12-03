package nl.computerhok.send2mpd;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {
    public static final String PREFS_HOSTNAME = "hostname";
    public static final String PREFS_PORT = "port";
    public static final String PREFS_USERNAME = "username";
    public static final String PREFS_PASSWORD = "password";
    public static final String PREFS_DESTDIR = "destdir";
    public static final String PREFS_MPD_PASSWORD = "mpdpassword";
    

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
