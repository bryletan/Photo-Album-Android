package com.example.android03;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class ShowAlbum extends AppCompatActivity {

    public static final String ALBUM_POS = "currentAlbumPos";
    public static final String SEARCH_STATUS = "searchStatus";
    public static final String SEARCHED_ALBUM = "searchedAlbum";

    private int albumPos;
    private ListView listView;
    private ArrayList<Photo> photos;
    private Album currentAlbum;
    private ListViewAdapter adapter;

    private boolean isSearched = false;
    private boolean deleteState = false;

    private ActivityResultLauncher<Intent> startForResultAdd;
    private ActivityResultLauncher<Intent> startForResultShowPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_detail);

        // getting Album instance
        Bundle bundle = getIntent().getExtras();

        isSearched = bundle.getBoolean(SEARCH_STATUS);
        if(!isSearched) {
            albumPos = bundle.getInt(ALBUM_POS);
            currentAlbum = Photos.albums.get(albumPos);
        } else {
            currentAlbum = (Album) bundle.getSerializable(SEARCHED_ALBUM);
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(currentAlbum.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        photos = currentAlbum.getPhotos();

        listView = findViewById(R.id.photo_list);
        adapter = new ListViewAdapter(this, photos);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((list, view, pos, id) -> showPhoto(pos));

        registerActivities();
    }

    public void registerActivities() {
        startForResultAdd =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                addPhoto(data);
                            }
                        });
        startForResultShowPhoto =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                Intent data = result.getData();
                                updateAlbums(data);
                            }
                        });
    }

    public void updateAlbums(Intent data) {
        return;
    }

    public void addPhoto(Intent data) {
        if(data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // remove the .file extension
            String name = getFileName(imageUri).substring(0, getFileName(imageUri).lastIndexOf('.'));

            Photo photoToAdd = new Photo(name, imageUri.toString());
            for(Photo p : photos) {
                if(photoToAdd.getURI().equals(p.getURI())) {
                    Toast toast = Toast.makeText(this, "Photo already exists.", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
            }
            currentAlbum.addPhoto(photoToAdd);
            Log.i("Added Photo", "Current Album Length: " + currentAlbum.getSize());
        }
        else {
            // ERROR, no image selected
            Toast toast = Toast.makeText(this, "No photo selected.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // reset adapter
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    public void removePhoto() {
        if(photos.size() == 0) {
            Toast toast = Toast.makeText(this, "There are no photos. ", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        invalidateOptionsMenu();
        deleteState = true;

        Toast toast = Toast.makeText(this, "Select photo to delete.", Toast.LENGTH_SHORT);
        toast.show();

        listView.setOnItemClickListener((a, v, position, id) -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(ShowAlbum.this);
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete " + photos.get(position).getName());
            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    listView.setOnItemClickListener((list, view, pos, id) -> showPhoto(pos));
                    invalidateOptionsMenu();
                    deleteState = false;
                }});
            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    currentAlbum.removePhoto(photos.get(position));

                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener((list, view, pos, id) -> showPhoto(pos));
                    invalidateOptionsMenu();
                    deleteState = false;
                }});
            adb.show();
        });
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // selecting an image, only used by addPhoto()
    private void selectImage() {
        // there is Intent.ACTION_OPEN_DOCUMENT or Intent.ACTION_GET_CONTENT
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startForResultAdd.launch(intent);
    }

    public void showPhoto(int p) {
        Intent intent = new Intent(ShowAlbum.this, ShowPhoto.class);
        Bundle extras = new Bundle();

        extras.putInt(ALBUM_POS, albumPos);
        extras.putInt(ShowPhoto.PHOTO_POS, p);

        extras.putBoolean(SEARCH_STATUS, isSearched);
        extras.putSerializable(SEARCHED_ALBUM, currentAlbum);

        intent.putExtras(extras);
        startForResultShowPhoto.launch(intent);
    }

    public void goBack() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelAction() {
        Toast toast = Toast.makeText(this, "Cancelled Delete.", Toast.LENGTH_LONG);
        toast.show();

        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((list, view, pos, id) -> showPhoto(pos));

        invalidateOptionsMenu();
        deleteState = false;
    }

    @Override
    public void onStop() {
        currentAlbum.setPhotos(photos);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        if(!isSearched && !deleteState) {
            inflater.inflate(R.menu.add_menu, menu);
            inflater.inflate(R.menu.remove_menu, menu);
        } else if (!isSearched && deleteState) {
            inflater.inflate(R.menu.cancel_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                selectImage();
                return true;
            case R.id.action_remove:
                removePhoto();
                return true;
            case R.id.action_cancel:
                cancelAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}