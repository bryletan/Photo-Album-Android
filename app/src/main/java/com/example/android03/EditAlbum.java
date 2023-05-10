package com.example.android03;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EditAlbum extends AppCompatActivity {

    public static final String ALBUM_NAME = "albumName";
    public static final String CURRENT_ALBUM = "currentAlbum";
    public static final String CURRENT_POS = "currentPos";

    private int pos;
    private EditText albumName;
    private Album currentAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_album);

        // create toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Edit Album");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // getting Album instance
        Bundle bundle = getIntent().getExtras();

        currentAlbum = (Album) bundle.getSerializable(CURRENT_ALBUM);
        pos = bundle.getInt(CURRENT_POS);
        albumName = findViewById(R.id.first_tag);
        albumName.setText(currentAlbum.getName());
    }

    public void edit(View view) {
        String name = albumName.getText().toString();

        if(name.equals(null) || name.trim().equals("")) {
            Toast toast = Toast.makeText(this, "Please enter a valid name.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        // make Bundle
        Bundle bundle = new Bundle();
        bundle.putString(ALBUM_NAME, name);
        bundle.putInt(CURRENT_POS, pos);
        bundle.putSerializable(CURRENT_ALBUM, currentAlbum);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }
}