package smith.welder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collection;
import java.util.Map;

public class GroupsAdd extends AppCompatActivity {

    final String userID = LandingScreen.getUserID();
    boolean inGroup;

    //Screen Resources
    TextView addError;
    TextView createMessage;
    Button confirm;
    Button cancel;
    EditText textEntry;

    //Firebase Groups/ database reference
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    String DBLocation = "/Welder/Groups/";
    final DatabaseReference dbRef = db.getReference(DBLocation);

    DatabaseReference groupRef;

    //create
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_add);

        setListeners();
        setupButtons();

    }

    public void setListeners() {
        Button submitButton = (Button) findViewById(R.id.GroupAddSubmit);
        textEntry = (EditText) findViewById(R.id.GroupAddInput);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addError.setVisibility(View.INVISIBLE);
                if (textEntry.getText().toString().length() < 5) {
                    addError.setText(R.string.errorMessageTooShort);
                    addError.setVisibility(View.VISIBLE);
                } else {
                    checkGroupExistence(textEntry.getText().toString());
                }
            }
        });
    }

    public void checkGroupExistence(String groupName) {
        final String groupID = groupName;



        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                boolean groupExists = false;


                //Search for user in database
                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    Log.d("GroupInfo:", child.toString());
                    Map<String, Object> model = (Map<String, Object>) child.getValue();

                    if (model.get("groupName").equals(groupID)) {
                        groupExists = true;
                        groupRef = child.getRef();
                        break;
                    }
                }

                if (groupExists) {
                    //Do nothing.
                    //User is in database
                    Log.d("Firebase", "Group " + groupID + " already in database");
                    Log.d("Firebase stuff", dataSnapshot.toString());
                    isUserInGroup(groupID);

                }
                else {
                    showNewGroupMessage(groupID);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void writeNewGroupToDatabase(String groupName){
        final String groupID = groupName;

        //Push new user object to database
        dbRef.push().child("groupName").setValue(groupID, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d ("Firebase",("Group " + groupID + " has been created in database"));
                Log.d("Firebase DBREF",databaseReference.toString());
                writeUserToNewGroup(databaseReference.getParent(), groupID);

            }
        });
    }

    public void writeUserToNewGroup(DatabaseReference dbRef, String groupName){

        dbRef.child("subscribers").push().setValue(userID, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d ("Firebase",("User " + userID + " has been added to new group"));
                Log.d("Firebase DBREF",databaseReference.toString());

            }
        });

    }

    public void writeUserToExistingGroup(DatabaseReference dbRef){
        Log.d("Group Add","DBREF: " + dbRef.toString());

        dbRef.push().setValue(userID, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.d ("Firebase",("User " + userID + " has been added to existing group"));
                textEntry.setText("");
            }
        });

        //dbRef.child("subscribers").
    }

    public void isUserInGroup(final String groupName){

        groupRef.child("subscribers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Search for user in database
                Log.d("SubscriberInfo:", dataSnapshot.toString() + dataSnapshot.getChildrenCount());


                for (DataSnapshot value : dataSnapshot.getChildren()) {
                    Log.d("User Info", value.getValue().toString());

                    if (value.getValue().toString().equals(userID)) {
                        inGroup = true;
                        Log.d("Found userID in group", value.getValue().toString());
                        addError.setText(R.string.errorMessageUserAlreadySubbed);
                        addError.setVisibility(View.VISIBLE);
                        break;
                    }
                }

                if(!inGroup){
                    writeUserToExistingGroup(dataSnapshot.getRef());
                    toastResults(groupName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showNewGroupMessage(String groupname){

        final String groupID = groupname;

        createMessage.setVisibility(View.VISIBLE);
        confirm.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeNewGroupToDatabase(groupID);
                hideConfirmation();
                toastResults(groupID);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideConfirmation();
            }
        });
    }

    public void hideConfirmation(){
        createMessage.setVisibility(View.INVISIBLE);
        confirm.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        textEntry.setText("");

    }

    public void setupButtons(){
        addError = (TextView) findViewById(R.id.GroupAddErrorMessage);
        createMessage = (TextView) findViewById(R.id.AddNewGroupMessage);
        confirm = (Button) findViewById(R.id.ConfirmGroupAdd);
        cancel = (Button) findViewById(R.id.CancelGroupAdd);
    }

    public void toastResults(String groupName){
        Toast.makeText(getApplicationContext(), "Success! You've been added to group " + groupName, Toast.LENGTH_LONG ).show();

    }
}
