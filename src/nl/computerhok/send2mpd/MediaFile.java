package nl.computerhok.send2mpd;

import java.io.File;
import java.io.Serializable;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import android.util.Log;

/**
 * Simple JavaBean to hold all information (like file name and id3 tags from an mp3 file).
 * 
 * @author metskem
 * 
 */
public class MediaFile implements Serializable {
    private static final String TAG = MediaFile.class.getSimpleName();
    private static final long serialVersionUID = 1L;

    private String filename;
    private String fullpath;
    private String album;
    private String artist;
    private String title;
    private String duration;
    private String bitrate;
    private String targetfilename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(final String album) {
        if (album != null) {
            this.album = album.trim();
        }
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(final String artist) {
        if (artist != null) {
            this.artist = artist.trim();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        if (title != null) {
            this.title = title;
        }
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(final String duration) {
        this.duration = duration;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(final String bitrate) {
        this.bitrate = bitrate;
    }

    public void save() throws Exception {

        AudioFile audioFile;
            audioFile = AudioFileIO.read(new File(getFullpath()));
            Tag tag = audioFile.getTag();

            // set all the ID3 tags:
            tag.setField(FieldKey.ALBUM, getAlbum());
            tag.setField(FieldKey.ARTIST, getArtist());
            tag.setField(FieldKey.TITLE, getTitle());
            // the other tags from the file are considered read-only , and are not changed in this app

            Log.e(TAG, "committing changes to " + audioFile.getFile().getCanonicalFile());
            audioFile.commit();
    }

    @Override
    public String toString() {
        return "MediaFile [filename=" + filename + ", fullpath=" + fullpath + ", album=" + album + ", artist=" + artist + ", title=" + title + ", duration=" + duration
                + ", bitrate=" + bitrate + ", targetfilename=" + targetfilename + "]";
    }

    public String getFullpath() {
        return fullpath;
    }

    public void setFullpath(final String fullpath) {
        this.fullpath = fullpath;
    }

    public String getTargetfilename() {
        return targetfilename;
    }

    public void setTargetfilename(final String targetfilename) {
        this.targetfilename = targetfilename;
    }
}
