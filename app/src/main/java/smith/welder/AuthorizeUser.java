package smith.welder;

import android.app.Activity;
import android.os.Bundle;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import static com.spotify.sdk.android.authentication.LoginActivity.REQUEST_CODE;


public class AuthorizeUser {
    
    
    
    public void auth(Activity callingScreen) {

        String CLIENT_ID = "fe50dae077c74216a5dd25fad3ddc0e3";
        String REDIRECT_URI = "http://localhost:5505/callback";

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(callingScreen, REQUEST_CODE, request);
    }        
}
