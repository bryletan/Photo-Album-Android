package com.example.android03;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MovePhoto extends AppCompatActivity {

    public static final String ALBUM_POS = "albumPos";
    public static final String PHOTO_POS = "photoPos";
    public static final String DESTINATION_ALBUM_POS = "destinationPos";

    private int albumPos;
    private int photoPos;

    private EditText destinationAlbum;
    private Button moveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_photo);

        // create toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Move Photo");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        moveButton = findViewById(R.id.move_button);
        moveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movePhoto();
            }
        });

        destinationAlbum = findViewById(R.id.destination_album_name);

        // getting Album instance
        Bundle bundle = getIntent().getExtras();

        albumPos = bundle.getInt(ALBUM_POS);
        photoPos = bundle.getInt(PHOTO_POS);
    }

    public void movePhoto() {
        String destinationName = destinationAlbum.getText().toString();

        if(destinationName.equals("")) {
            Toast toast = Toast.makeText(this, "Enter an album name.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        int destinationPos = -1;
        for(int i = 0; i < Photos.albums.size(); i++) {
            Album curAlbum = Photos.albums.get(i);
            if(curAlbum.getName().equals(destinationName)) {
                destinationPos = i;
                break;
            }
        }

        // if the destination does not exist
        if(destinationPos == -1) {
            Toast toast = Toast.makeText(this, "Entered Album does not exist.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // all checks passed
        Bundle bundle = new Bundle();
        bundle.putInt(DESTINATION_ALBUM_POS, destinationPos);

        goBack(bundle);
    }

    // goes back to showAlbum
    public void goBack(Bundle bundle) {
        Intent intent = new Intent();

        bundle.putInt(ShowAlbum.ALBUM_POS, albumPos);

        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);
        finish();
    }

    // should only execute when the user cancels a move action
    public void goBack() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
