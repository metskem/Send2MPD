package nl.computerhok.send2mpd;

import java.io.Serializable;

/**
 * Simple JavaBean to hold all information (like file name and id3 tags from an mp3 file).
 * 
 * @author metskem
 * 
 */
public class MediaFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filename;
    private String fullpath;
    private String album;
    private String artist;
    private String title;
    private String duration;
    private String bitrate;

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
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
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

    @Override
    public String toString() {
        return "MediaFile [filename=" + filename + ", fullpath=" + fullpath + ", album=" + album + ", artist=" + artist + ", title=" + title + ", duration=" + duration
                + ", bitrate=" + bitrate + "]";
    }

    public String getFullpath() {
        return fullpath;
    }

    public void setFullpath(final String fullpath) {
        this.fullpath = fullpath;
    }
}
