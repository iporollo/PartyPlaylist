package ip.partyplaylist.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;

@IgnoreExtraProperties
public class Party implements Serializable {

    public String name;
    public ArrayList<Song> trackList;
    public String playlistId;
    public String hostId;
    public String partyId;
    public String spotifyAccessToken;

    public Party() {

    }

    public Party(String name, ArrayList<Song> trackList) {
        this.name = name;
        this.trackList = trackList;
    }

    public void addTrack(Song trackToAdd) {
        trackList.add(trackToAdd);
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public void setPartyAccessToken(String accessToken) {this.spotifyAccessToken = accessToken;}
}
