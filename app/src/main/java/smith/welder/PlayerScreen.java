package smith.welder;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.io.InputStream;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class PlayerScreen extends AppCompatActivity implements
    SpotifyPlayer.NotificationCallback, ConnectionStateCallback{

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

        //Remove title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_player_screen);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        //Check result coming from desired activity
        if (requestCode == REQUEST_CODE){
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN){
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                    @Override
                    public void onInitialized(SpotifyPlayer spotifyPlayer) {
                        mPlayer = spotifyPlayer;
                        mPlayer.addConnectionStateCallback(PlayerScreen.this);
                        mPlayer.addNotificationCallback(PlayerScreen.this);


                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("PlayerScreen", "Playback Error: " + throwable.getMessage());
                    }

                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void onLoggedIn() {
        Log.d("PlayerScreen", "User logged in");

        Log.d("PlayerScreen", "Playing Track");

        try {
            mPlayer.playUri(null, "spotify:user:spotify:playlist:37i9dQZEVXcPEf1HdkmU5s", 0, 0);

        }catch(Throwable e){
            Log.d("Player", "NOT PLAYING");}

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
            }
        });

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
            }
        });




        //String albumArtURL = trackData.currentTrack.albumCoverWebUrl.toString();

        //System.out.println(albumArtURL);

        // show The Image in a ImageView
        //new DownloadImageTask((ImageView) findViewById(R.id.albumArt))
        //        .execute(trackData.currentTrack.albumCoverWebUrl);




    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Error error) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {

        Log.v("PLAYEREVENT: " ,playerEvent.name());

        if(playerEvent.name().equals("kSpPlaybackEventAudioFlush") || playerEvent.name().equals("kSpPlaybackNotifyTrackChanged")){
            changeSongDisplay();
        }


    }

    @Override
    public void onPlaybackError(Error error) {

    }



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
}
