package com.example.android03;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Photos extends AppCompatActivity {

    private ListView listView;

    public static ArrayList<Album> albums = new ArrayList<Album>();
    private boolean deleteState = false;
    private boolean editState = false;

    private ActivityResultLauncher<Intent> startForResultAdd;
    private ActivityResultLauncher<Intent> startForResultShowAlbum;
    private ActivityResultLauncher<Intent> startForResultEdit;
    private ActivityResultLauncher<Intent> startForResultSearchPhoto;
    private ArrayAdapter<Album> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_list);

        // create toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Photo Album");

        // read from file
        try {
            FileInputStream fis = this.openFileInput("albums.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);

            albums = (ArrayList<Album>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // set up the listview
        listView = findViewById(R.id.album_list);
        adapter = new ArrayAdapter<>(this, R.layout.album, albums);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((list, view, pos, id) -> showAlbum(pos));

        registerActivities();
    }

    public void registerActivities() {
        startForResultAdd =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                applyEdit(result, "addAlbum");
                            }
                        });
        startForResultShowAlbum =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                applyEdit(result, "showAlbum");
                            }
                        });
        startForResultEdit =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                applyEdit(result, "editAlbum");
                            }
                        });
        startForResultSearchPhoto =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                applyEdit(result, "searchPhoto");
                            }
                        });
    }

    private void showAlbum(int p) {
        // passing Album instance
        Intent intent = new Intent(this, ShowAlbum.class);
        Bundle bundle = new Bundle();
        bundle.putInt(ShowAlbum.ALBUM_POS, p);

        intent.putExtras(bundle);
        startForResultShowAlbum.launch(intent);
    }

    private void showAlbum(Album a) {
        Intent intent = new Intent(this, ShowAlbum.class);
        Bundle bundle = new Bundle();

        bundle.putBoolean(ShowAlbum.SEARCH_STATUS, true);
        bundle.putSerializable(ShowAlbum.SEARCHED_ALBUM, a);

        intent.putExtras(bundle);
        startForResultShowAlbum.launch(intent);
    }

    public void addAlbum() {
        Intent intent = new Intent(this, AddAlbum.class);
        startForResultAdd.launch(intent);
    }

    public void applyEdit(ActivityResult result, String action) {
        Intent intent = result.getData();
        Bundle bundle = intent.getExtras();

        if(action.equals("addAlbum")) {
            String name = bundle.getString(AddAlbum.ALBUM_NAME);
            Album album = new Album(name);
            albums.add(album);
        }
        else if(action.equals("editAlbum")) {
            int position = bundle.getInt(EditAlbum.CURRENT_POS);
            Album albumToEdit = albums.get(position);
            String name = bundle.getString(EditAlbum.ALBUM_NAME);

            for(Album a : albums) {
                if(a.getName().equals(name)) {
                    Toast toast = Toast.makeText(this, "Album name already exists.", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
            }

            albumToEdit.setName(name);
            // reset the listener
            listView.setOnItemClickListener((list, view, pos, id) -> showAlbum(pos));
        } else if(action.equals("searchPhoto")) {
            Album searched = (Album) bundle.getSerializable(SearchPhoto.SEARCHED_ALBUM);
            showAlbum(searched);
        }

        // reset the adapter
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
    }

    public void editAlbum() {
        if(albums.size() == 0) {
            Toast toast = Toast.makeText(this, "There are no albums to edit.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        invalidateOptionsMenu();
        editState = true;

        Toast toast = Toast.makeText(this, "Select album to edit.", Toast.LENGTH_SHORT);
        toast.show();
        listView.setOnItemClickListener((a, v, position, id) -> {
            Album album = albums.get(position);

            // passing Album instance
            Intent intent = new Intent(this, EditAlbum.class);
            Bundle bundle = new Bundle();
            bundle.putString(EditAlbum.ALBUM_NAME, album.getName());
            bundle.putSerializable(EditAlbum.CURRENT_ALBUM, album);
            bundle.putInt(EditAlbum.CURRENT_POS, position);
            intent.putExtras(bundle);

            invalidateOptionsMenu();
            editState = false;

            startForResultEdit.launch(intent);
        });
    }

    public void removeAlbum() {
        if(albums.size() == 0) {
            Toast toast = Toast.makeText(this, "There are no albums to delete.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        invalidateOptionsMenu();
        deleteState = true;

        Toast toast = Toast.makeText(this, "Select album to delete.", Toast.LENGTH_SHORT);
        toast.show();

        listView.setOnItemClickListener((a, v, position, id) -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(Photos.this);
            adb.setTitle("Delete?");
            adb.setMessage("Are you sure you want to delete " + albums.get(position).getName());
            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    listView.setOnItemClickListener((list, view, pos, id) -> showAlbum(pos));
                    invalidateOptionsMenu();
                    deleteState = false;
                }});
            adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    albums.remove(position);
                    adapter.notifyDataSetChanged();
                    listView.setAdapter(adapter);

                    listView.setOnItemClickListener((list, view, pos, id) -> showAlbum(pos));
                    invalidateOptionsMenu();
                    deleteState = false;
                }});
            adb.show();
        });
    }

    public void searchPhoto() {
        Intent intent = new Intent(this, SearchPhoto.class);
        startForResultSearchPhoto.launch(intent);
    }

    public void cancelAction() {
        String state = deleteState ? "Delete" : "Edit";

        Toast toast = Toast.makeText(this, "Cancelled " + state + ".", Toast.LENGTH_LONG);
        toast.show();

        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((list, view, pos, id) -> showAlbum(pos));

        invalidateOptionsMenu();
        deleteState = false;
        editState = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(!deleteState && !editState) {
            inflater.inflate(R.menu.add_menu, menu);
            inflater.inflate(R.menu.edit_menu, menu);
            inflater.inflate(R.menu.search_menu, menu);
            inflater.inflate(R.menu.remove_menu, menu);
        } else {
            inflater.inflate(R.menu.cancel_menu, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                addAlbum();
                return true;
            case R.id.action_remove:
                removeAlbum();
                return true;
            case R.id.action_edit:
                editAlbum();
                return true;
            case R.id.action_search:
                searchPhoto();
                return true;
            case R.id.action_cancel:
                cancelAction();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        // write to file
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
}