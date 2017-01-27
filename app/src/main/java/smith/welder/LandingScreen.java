package smith.welder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.concurrent.Executor;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;

public class LandingScreen extends AppCompatActivity {

    private static String authToken;
    private static SpotifyApi webAPI;

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

                    String userID = "";

                    webAPI = new SpotifyApi();

                    webAPI.setAccessToken(toke);

                    new UserLookup(userID,this).execute(webAPI);



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

}
