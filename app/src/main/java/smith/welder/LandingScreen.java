package smith.welder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.Map;
import java.util.concurrent.Executor;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class LandingScreen extends AppCompatActivity {

    private static String authToken;
    private static SpotifyApi webAPI;

    private String discoverId;
    private boolean newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_screen);

        Button connect = (Button) findViewById(R.id.SpotConnectButton);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizeUser o = new AuthorizeUser();
                o.auth(LandingScreen.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {

                case TOKEN:
                    String toke = response.getAccessToken();
                    //Toast.makeText(this, "Logged in!", Toast.LENGTH_SHORT).show();
                    setAuthToken(response.getAccessToken());
                    //Toast.makeText(this, getAuthToken(), Toast.LENGTH_LONG).show();
                    Log.d("Authentication","got access token");


                    //Begin setup for Android Web API wrapper
                    webAPI = new SpotifyApi();

                    webAPI.setAccessToken(toke);

                    final SpotifyService web = webAPI.getService();


                    //Everything nested in 'success' callback below. Otherwise,
                    //HTTP GET curl request (Retrofit) will throw timing of
                    //execution off, resulting in null username.

                    web.getMe(new Callback<UserPrivate>() {
                        @Override
                        public void success(UserPrivate userPrivate, Response response) {
                            String userID = userPrivate.id;
                            Log.d("UserID",userID);

                            boolean isNewUser = isUserRegistered(userID);

                            if(isNewUser) {
                                Log.d("Firebase", "User " + userID + " is a returning user. Proceeding..");
                            }


                            startActivity(new Intent(LandingScreen.this, MainMenu.class));

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.d("Error Getting User ID", error.toString());
                        }
                    });


                    //Log.d("Username", "Is: " + id);

                case ERROR:
                    Log.d("Authentication", "Could not authenticate");
                    break;
            }
        }
    }

    public static String getAuthToken(){
        return authToken;
    }

    public void setAuthToken(String token){
        authToken = token;
    }

    public boolean isUserRegistered(String username){

        final String userID = username;

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        String DBLocation = "/Welder/Users/";

        final DatabaseReference dbRef = db.getReference(DBLocation);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean userExists = false;

                //Search for user in database
                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    Log.d("UserInfo:",child.toString());
                    Map<String, Object> model = (Map<String, Object>) child.getValue();

                    if(model.get("username").equals(userID)) {
                        userExists = true;
                        break;
                    }
                }

                if(userExists){
                    //Do nothing.
                    //User is in database
                    Log.d("Firebase","User " + userID + " already in database");
                    Log.d("Firebase stuff",dataSnapshot.toString());
                }
                else{
                    writeNewUserToDatabase(dbRef,userID);
                }

                newUser = !userExists;


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Firebase","Data write cancelled: " + databaseError.toString());
            }

        });

        return newUser;
    }

    public void writeNewUserToDatabase(DatabaseReference dbRef, String username){

        final String userID = username;
        //Push new user object to database
        dbRef.push().child("username").setValue(userID, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d ("Firebase",("User " + userID + " has been created in database"));
                Log.d("Firebase DBREF",databaseReference.toString());
                writeUserDiscoverID(databaseReference);

            }
        });
    }

    public void writeUserDiscoverID(DatabaseReference dbr){

        SpotifyService web = webAPI.getService();

        final DatabaseReference dbRef = dbr;

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

                dbRef.getParent().child("discoverId").setValue(discoverId, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        Log.d("Firebase","Hopefully this works. discover id: " + discoverId);
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

    }
}