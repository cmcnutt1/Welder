package smith.welder;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;



public class UserLookup extends AsyncTask <SpotifyApi, Void, String> {

    //Globals

    String userID;
    Activity calling;
    boolean newUser;

    String discoverId;
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference discParam;

    SpotifyApi webAPI;

    //Constructor

    public UserLookup(String user, Activity callingScreen){
        this.userID = user;
        this.calling = callingScreen;
    }

    @Override
    protected String doInBackground(SpotifyApi... api) {

        webAPI = api[0];

        final SpotifyService web = webAPI.getService();

        web.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                userID = userPrivate.id;
                Log.d("UserID",userID);

            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Error Getting User ID", error.toString());
            }
        });

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
                    //Push new user object to database
                    dbRef.push().child("username").setValue(userID, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            Log.d ("Firebase",("User " + userID + " has been created in database"));
                            Log.d("Firebase DBREF",databaseReference.toString());
                            discParam = databaseReference.getParent();

                        }
                    });
                }

                newUser = (!userExists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        return userID;
    }

    @Override
    protected void onPostExecute(String nothing){

        if(!newUser) {
            Log.d("Firebase","Not a new user. Going to main menu");
            calling.startActivity(new Intent(calling, MainMenu.class));
        }
        else{
            Log.d("Firebase","New user. Getting discover weekly id");
            new DiscoveryLookup(discParam,calling).execute(webAPI);
        }

    }
}
