package smith.welder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        setButtonListeners();
    }

    public void setButtonListeners(){

        Button discover = (Button) findViewById(R.id.DiscoverButton);
        Button groups = (Button) findViewById(R.id.GroupButton);
        Button settings = (Button) findViewById(R.id.SettingsButton);

        groups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, GroupsAdd.class));
            }
        });

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent player = new Intent(MainMenu.this, PlayerScreen.class);
                //player.putExtra("uri","spotify:user:biocoven:playlist:3zpEy2JrhBVF6yscY41BuG");
                player.putExtra("uri","spotify:trackset:Playlist:6HbZgzrQaXNV6dzcxJqoCv,4DO5yP4K1CDr9oAJSqVs32");
                startActivity(player);
            }
        });

    }
}
