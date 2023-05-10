package com.example.android03;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ShowPhoto extends AppCompatActivity {
    public static final String PHOTO_POS = "currentPos";

    private ImageView imageView;
    private int albumPos;
    private int photoPos;
    private Album currentAlbum;
    private boolean isSearched = false;

    // xml elements
    private TextView tagTextView;
    private Spinner tagTypeBox;
    private EditText tagValueField;

    private Button addTagButton;
    private Button removeTagButton;
    private ImageButton prevButton;
    private ImageButton nextButton;

    private ActivityResultLauncher<Intent> startForResultMovePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_detail);

        Intent intent = getIntent();
        Bundle bundle = getIntent().getExtras();

        isSearched = bundle.getBoolean(ShowAlbum.SEARCH_STATUS);
        if(!isSearched) {
            albumPos = bundle.getInt(ShowAlbum.ALBUM_POS);
            currentAlbum = Photos.albums.get(albumPos);
        } else {
            currentAlbum = (Album) bundle.getSerializable(ShowAlbum.SEARCHED_ALBUM);
        }

        photoPos = bundle.getInt(PHOTO_POS);

        // setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(currentAlbum.getPhoto(photoPos).getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        imageView = findViewById(R.id.image_display);
        tagTextView = findViewById(R.id.tag_view);

        tagTypeBox = findViewById(R.id.tag_type_cbox);
        ArrayList<String> choices = new ArrayList<>();
        choices.add("Location");
        choices.add("Person");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.tag_type, choices);
        tagTypeBox.setAdapter(adapter);

        tagValueField = findViewById(R.id.tag_value_field);

        addTagButton = findViewById(R.id.confirmation_button);
        addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTag();
            }
        });

        removeTagButton = findViewById(R.id.deletion_button);
        removeTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeTag();
            }
        });

        prevButton = findViewById(R.id.prev_button);
        nextButton = findViewById(R.id.next_button);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoPos--;
                if(photoPos < 0) {
                    photoPos = currentAlbum.getSize() - 1;
                }
                displayImage();
                getSupportActionBar().setTitle(currentAlbum.getPhoto(photoPos).getName());
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photoPos++;
                if(photoPos == currentAlbum.getSize()) {
                    photoPos = 0;
                }
                displayImage();
                getSupportActionBar().setTitle(currentAlbum.getPhoto(photoPos).getName());
            }
        });

        registerActivities();
        displayImage();
    }

    public void registerActivities() {
        startForResultMovePhoto =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                applyMove(data);
                            }
                        });
    }

    public void displayImage() {
        Photo currentPhoto = currentAlbum.getPhoto(photoPos);

        String photoURI = currentPhoto.getURI();
        Uri image = Uri.parse(photoURI);

        try {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), image);
            @SuppressLint("WrongThread")
            Bitmap map = ImageDecoder.decodeBitmap(source);
            map = Bitmap.createScaledBitmap(map, 360, 420, false);
            imageView.setImageBitmap(map);

        } catch (Exception e) {
            e.printStackTrace();
        }

        String tags;
        if(currentPhoto.hasTags()) {
            tags = "Tags: \n" + currentPhoto.getTags();
        } else {
            tags = "Tags: \n This photo has no tags.";
        }
        tagTextView.setText(tags);
    }

    public void addTag() {
        // get all information
        String tagType = tagTypeBox.getSelectedItem().toString();
        String tagValue = tagValueField.getText().toString();

        if(tagType == null || tagValue.equals("")) {
            Toast toast = Toast.makeText(this, "Missing information.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Photo currentPhoto = currentAlbum.getPhoto(photoPos);

        if(currentPhoto.hasTag(tagType, tagValue)) {
            Toast toast = Toast.makeText(this, "Photo already contains tag.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        currentPhoto.addTag(tagType, tagValue);

        tagValueField.setText("");
        displayImage();
    }

    public void removeTag() {
        String tagType = tagTypeBox.getSelectedItem().toString();
        String tagValue = tagValueField.getText().toString();

        if(tagType == null || tagValue.equals("")) {
            Toast toast = Toast.makeText(this, "Missing information.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Photo currentPhoto = currentAlbum.getPhoto(photoPos);

        if(!currentPhoto.hasTag(tagType, tagValue)) {
            Toast toast = Toast.makeText(this, "Photo does not contain tag.", Toast.LENGTH_LONG);
            toast.show();
        }

        currentPhoto.removeTag(tagType, tagValue);

        tagValueField.setText("");
        displayImage();
    }

    public void movePhoto() {
        Intent intent = new Intent(this, MovePhoto.class);
        Bundle bundle = new Bundle();

        bundle.putInt(MovePhoto.ALBUM_POS, albumPos);
        bundle.putInt(MovePhoto.PHOTO_POS, photoPos);
        intent.putExtras(bundle);

        startForResultMovePhoto.launch(intent);
    }

    public void applyMove(Intent data) {
        // get the index of the destination album
        Bundle bundle = data.getExtras();
        int destinationPos = bundle.getInt(MovePhoto.DESTINATION_ALBUM_POS);

        // move photo from current pos to the album at destination pos
        Album destination = Photos.albums.get(destinationPos);
        Photo currentPhoto = currentAlbum.getPhoto(photoPos);

        currentAlbum.removePhoto(currentPhoto);
        destination.addPhoto(currentPhoto);

        Toast toast = Toast.makeText(this, "Moved photo to " + destination.getName(), Toast.LENGTH_LONG);
        toast.show();
        goBack();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(!isSearched) {
            inflater.inflate(R.menu.move_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_move:
                movePhoto();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        try {
            FileOutputStream fos = this.openFileOutput("albums.dat", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(Photos.albums);
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    public void goBack() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);
        finish();
    }
}