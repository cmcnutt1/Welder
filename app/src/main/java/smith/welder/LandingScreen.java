package smith.welder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class LandingScreen extends AppCompatActivity {

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
                startActivity(new Intent(LandingScreen.this, PlayerScreen.class));
            }
        });
    }
}
