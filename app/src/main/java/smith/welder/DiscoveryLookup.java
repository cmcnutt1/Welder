package smith.welder;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by chris on 1/26/17.
 */

public class DiscoveryLookup extends AsyncTask<SpotifyApi,Void, String> {

    DatabaseReference DBRef;
    Activity calling;

    String discoverId;

    public DiscoveryLookup(DatabaseReference dbr, Activity callingScreen){
        this.DBRef = dbr;
        this.calling = callingScreen;
    }

    @Override
    protected String doInBackground(SpotifyApi... api) {

        //String discoverUri;

        SpotifyApi webAPI = api[0];

        final SpotifyService web = webAPI.getService();


        web.getMyPlaylists(new Callback<Pager<PlaylistSimple>>() {
            @Override
            public void success(Pager<PlaylistSimple> playlistSimplePager, Response response) {
                String discUri = "";
                for(int i = 0; i < playlistSimplePager.items.size(); i++){
                    if(playlistSimplePager.items.get(i).name.equals("Discover Weekly")){
                        discUri = playlistSimplePager.items.get(i).uri;
                        break;
                    }
                }
                discoverId = discUri.substring(30);
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        DBRef.getParent().child("discoverId").setValue(discoverId, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d("Firebase","Hopefully this works. discover id: " + discoverId);
            }
        });

        return discoverId;

    }

    @Override
    protected void onPostExecute(String nothing){
        Log.d("Landing","Opening Main Menu");
        calling.startActivity(new Intent(calling, MainMenu.class));
    }
}
