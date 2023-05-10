package com.example.android03;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AddAlbum extends AppCompatActivity {

    public static final String ALBUM_NAME = "albumName";
    public static final String ALBUM_SIZE = "albumSize";

    private EditText albumName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_album);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Add Album");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        albumName = findViewById(R.id.first_tag);
    }

    public void add(View view) {
        String name = albumName.getText().toString();

        if(name.equals(null) || name.trim().equals("")) {
            Toast toast = Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        for(Album a : Photos.albums) {
            if(a.getName().equals(name)) {
                Toast toast = Toast.makeText(this, "Album already exists.", Toast.LENGTH_LONG);
                toast.show();

                albumName.setText("");
                return;
            }
        }

        // make Bundle
        Bundle bundle = new Bundle();
        bundle.putString(ALBUM_NAME, name);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

}