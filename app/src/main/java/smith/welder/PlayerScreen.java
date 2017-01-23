package smith.welder;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;



public class PlayerScreen extends AppCompatActivity{

    private static final String CLIENT_ID = "fe50dae077c74216a5dd25fad3ddc0e3";
    private static final String REDIRECT_URI = "http://localhost:5505/callback";

    private static Player mPlayer;
    private static Metadata trackData;

    private ImageButton nextButton;
    private ImageButton backButton;
    private TextView songText;
    private TextView artistText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_screen);

        //Testing... Toast message popup of the Authentication Token string
        //Toast.makeText(this, LandingScreen.getAuthToken(), Toast.LENGTH_LONG).show();

        //Spotify Player object config
        final Config playerConfig = new Config(PlayerScreen.this, LandingScreen.getAuthToken(), AuthorizeUser.CLIENT_ID);

        //SpotifyPlayer (mPlayer) initialized and callback functions
        Spotify.getPlayer(playerConfig, PlayerScreen.this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    Log.d("Player","Initialized");

                    //Assign mPlayer to SpotifyPlayer result
                    mPlayer = spotifyPlayer;

                    //Add Connection State because if beginPlayback(mPlayer.playURI function specifically) called too soon
                    //after player initialization, error will be thrown. Now it will begin playback when onLoggedIn is returned.
                    mPlayer.addConnectionStateCallback(new ConnectionStateCallback() {
                        @Override
                        public void onLoggedIn() {
                            setupPlayerUI();
                            beginPlayback();
                        }

                        @Override
                        public void onLoggedOut() {
                            //Do something
                        }

                        @Override
                        public void onLoginFailed(Error error) {
                            //Do something
                        }

                        @Override
                        public void onTemporaryError() {
                            //Do something
                        }

                        @Override
                        public void onConnectionMessage(String s) {
                            //Log any connection messages to debug
                            Log.d("Player Connection: ",s);
                        }
                    });

                    mPlayer.addNotificationCallback(new Player.NotificationCallback() {
                        @Override
                        public void onPlaybackEvent(PlayerEvent playerEvent) {
                            Log.v("PLAYEREVENT: " ,playerEvent.name());

                            if(playerEvent.name().equals("kSpPlaybackEventAudioFlush") || playerEvent.name().equals("kSpPlaybackNotifyTrackChanged")){
                                changeSongDisplay();
                            }
                        }

                        @Override
                        public void onPlaybackError(Error error) {
                            Log.d("Player","Error: " + error.toString());
                        }
                    });



                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("PlayerScreen", "Playback Error: " + throwable.getMessage());
                }
            });
    }


    //Destroy player on back or exit. Good memory management.
    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
        Log.d("Player","DESTROYED");
    }


    //If user pauses
    public void onPause(Player nPlayer) {
        if (nPlayer != null) {
            nPlayer.pause(new Player.OperationCallback() {
                @Override
                public void onSuccess() {
                    Log.d("Player", "Song Paused");
                    togglePlay(true);
                }

                @Override
                public void onError(Error error) {
                    Log.d("Player", "FAILED ON PAUSE: " + error.toString());
                }
            });
        }
    }


    //If user presses play
    public void onPlay(){
        mPlayer.resume(new Player.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.d("Player","Resuming song");
                togglePlay(false);
            }

            @Override
            public void onError(Error error) {
                Log.d("Player","FAILED ON PLAY: " + error.toString());
            }
        });
    }



    //Get and display album artwork, song name, artist name
    //
    // *NOTE*: For artwork, DownloadImageTask class is used.
    // Look for DownloadImageTask.java in current directory
    // to see more about operation.
    public void changeSongDisplay(){
        trackData = mPlayer.getMetadata();
        new DownloadImageTask((ImageView) findViewById(R.id.albumArt))
                .execute(trackData.currentTrack.albumCoverWebUrl);
        songText = (TextView) findViewById(R.id.SongName);
        assert songText != null;
        songText.setText(trackData.currentTrack.name);
        artistText = (TextView) findViewById(R.id.ArtistName);
        artistText.setText(trackData.currentTrack.artistName);
    }


    //Switch pause and play buttons. Make one invisible, the other one visible.
    //If pausing, use togglePlay(true)
    public void togglePlay(boolean playing){
        ImageButton pause = (ImageButton)findViewById(R.id.PauseButton);
        ImageButton play = (ImageButton)findViewById(R.id.PlayButton);


        if(playing) {
            pause.setVisibility(View.INVISIBLE);
            play.setVisibility(View.VISIBLE);
        }
        else{
            play.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.VISIBLE);
        }
    }


    //Set up button listeners
    public void setupPlayerUI(){

        //Skip Forward Button
        nextButton = (ImageButton) findViewById(R.id.PlayerNextButton);
        assert nextButton != null;
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.skipToNext(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("PlayerScreen","Going to next track");
                    }

                    @Override
                    public void onError(Error error) {

                    }
                });
                togglePlay(false);
            }
        });

        //Skip Back Button
        backButton = (ImageButton) findViewById(R.id.PlayerBackButton);
        assert backButton != null;
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.skipToPrevious(new Player.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("PlayerScreen","Going to previous track");
                    }

                    @Override
                    public void onError(Error error) {

                    }
                });
                togglePlay(false);
            }
        });

        //Pause Button
        ImageButton pauseButton = (ImageButton) findViewById(R.id.PauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause(mPlayer);
            }
        });

        //Play Button
        ImageButton playButton = (ImageButton) findViewById(R.id.PlayButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay();
            }
        });
    }

    //Start playing
    public void beginPlayback(){
        mPlayer.playUri(null,"spotify:user:biocoven:playlist:6gN0lPTuKx2pBaHNaLrYI3",0,0);
    }
}
