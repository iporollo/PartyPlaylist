package ip.partyplaylist.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import ip.partyplaylist.R;
import ip.partyplaylist.controllers.HostPlayerController;
import ip.partyplaylist.model.Party;
import ip.partyplaylist.model.Song;
import ip.partyplaylist.screen_actions.HostPlayerScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;
import kaaes.spotify.webapi.android.models.Playlist;


public class HostPlayerActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, HostPlayerScreenActions{//, CreatePartyScreenActions {

    private static final String CLIENT_ID = "ea09225ef2974242a1549f3812a15496";

    private Party mCurrentParty;
    private ArrayList<Song> mTrackList;
    private ArrayList<String> mSongListOfStrings;
    private Player mPlayer;
    private HostPlayerController mHostPlayerController;
    private SharedPreferenceHelper mSharedPreferenceHelper;

    private Button mAddButton;
    private TextView songTitle;
    private TextView songArtist;
    private ImageView albumCover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_player);
        mSharedPreferenceHelper = new SharedPreferenceHelper(this);
        mHostPlayerController = new HostPlayerController(this);

        mAddButton = (Button) findViewById(R.id.btnAddSong);
        songTitle = (TextView) findViewById(R.id.txtSongTitle);
        songArtist = (TextView) findViewById(R.id.txtSongArtist);
        albumCover = (ImageView) findViewById(R.id.imgSongCover);

        songTitle.setVisibility(View.GONE);
        songArtist.setVisibility(View.GONE);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHostPlayerController.onAddTrackButtonPressed();
            }
        });


        //creates spotify music player
        Config playerConfig = new Config(HostPlayerActivity.this, mSharedPreferenceHelper.getCurrentSpotifyToken(), CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver(){
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.addNotificationCallback(HostPlayerActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("HostPlayerActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });


        mTrackList = mHostPlayerController.createSongModelArrayList();
        mSongListOfStrings = mHostPlayerController.createSongStringArrayList();

        mCurrentParty = mHostPlayerController.getInitialParty(mTrackList);

        // Get the reference of ListViewAnimals
        final ListView lstviewTracksGUI=(ListView)findViewById(R.id.lstviewTracks);

        // Create The Adapter with passing ArrayList as 3rd parameter
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSongListOfStrings);
        // Set The Adapter
        lstviewTracksGUI.setAdapter(arrayAdapter);
        lstviewTracksGUI.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                songTitle.setVisibility(View.VISIBLE);
                songArtist.setVisibility(View.VISIBLE);

                songArtist.setText(mTrackList.get(position).songArtistName);
                songTitle.setText(mTrackList.get(position).songName);

                try {
                    String img_url = mTrackList.get(position).imageURL;
                    URL url = new URL(img_url);
                    Bitmap bmp;
                    bmp= BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    albumCover.setImageBitmap(bmp);
                } catch (IOException e) {
                    System.err.print("Error getting album image");
                }

                mPlayer.playUri(null, mTrackList.get(position).songID, 0, 0);

            }
        });

    }


    @Override
    public void showSearchTrackScreen() {
        Intent startSearchSongActivity = new Intent(this, SearchTrackActivity.class);
        startActivityForResult(startSearchSongActivity, CreatePartyActivity.SEARCH_SONG_REQUEST_CODE); // activates the method below
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CreatePartyActivity.SEARCH_SONG_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Song trackToAdd = (Song) data.getExtras().get(SearchTrackActivity.TRACK);

                //todo need to check to see if song is already in tracklist, dont add it if it is

                mTrackList.add(trackToAdd);
                //mCurrentParty.addTrack(trackToAdd);
                mSongListOfStrings.add(trackToAdd.songArtistName + " - " + trackToAdd.songName);

                mHostPlayerController.updateCurrentSpotifyPlaylist(mCurrentParty);
            }
        }
    }


    @Override
    public void onPlaybackError(Error error) {
        Log.d("HostPlayerActivity", "Playback failed");
    }

    @Override
    public void onPlaybackEvent(PlayerEvent event) {
        Log.d("HostPlayerActivity", "playback event logging right here");
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }



}