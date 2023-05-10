package com.example.android03;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

public class SearchPhoto extends AppCompatActivity {

    public static final String SEARCHED_ALBUM = "searchedAlbum";

    private AutoCompleteTextView firstTagValue, secondTagValue;

    private Spinner firstTagType;
    private Spinner secondTagType;
    private Spinner conjunctionBox;

    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_photo);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // setup AutoCompleteTextView
        ArrayList<String> temp1 = new ArrayList<>();
        ArrayList<String> temp2 = new ArrayList<>();

        for(Album a : Photos.albums) {
            for(Photo p: a.getPhotos()) {
                HashSet<String> locations = p.getLocationTags();
                HashSet<String> persons = p.getPersonTags();
                temp1.addAll(locations);
                temp2.addAll(persons);
            }
        }
        // remove the duplicates
        ArrayList<String> autoSearchLocation = new ArrayList<>(new HashSet<>(temp1));
        ArrayList<String> autoSearchPerson = new ArrayList<>(new HashSet<>(temp2));

        ArrayAdapter<String> locationArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autoSearchLocation);
        ArrayAdapter<String> personArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autoSearchPerson);

        firstTagValue = findViewById(R.id.first_tag);
        secondTagValue = findViewById(R.id.second_tag);

        // setting up on item click listeners
        firstTagType = findViewById(R.id.first_tag_type);
        firstTagType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String choice = adapterView.getItemAtPosition(i).toString();
                Log.i("Selected Item!", "Selected: " + choice);
                if(choice.equals("Location")) {
                    firstTagValue.setAdapter(locationArrayAdapter);
                } else {
                    firstTagValue.setAdapter(personArrayAdapter);
                }
                firstTagValue.setThreshold(1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        secondTagType = findViewById(R.id.second_tag_type);
        secondTagType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String choice = adapterView.getItemAtPosition(i).toString();
                Log.i("Selected Item!", "Selected: " + choice);
                if(choice.equals("Location")) {
                    secondTagValue.setAdapter(locationArrayAdapter);
                } else {
                    secondTagValue.setAdapter(personArrayAdapter);
                }
                secondTagValue.setThreshold(1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        ArrayList<String> tagTypeChoices = new ArrayList<>();
        tagTypeChoices.add("Location");
        tagTypeChoices.add("Person");
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, R.layout.tag_type, tagTypeChoices);

        firstTagType.setAdapter(ad);
        secondTagType.setAdapter(ad);

        conjunctionBox = findViewById(R.id.conjunction_box);
        ArrayList<String> choices = new ArrayList<>();
        choices.add("None");
        choices.add("AND");
        choices.add("OR");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.tag_type, choices);
        conjunctionBox.setAdapter(adapter);

        searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });
    }

    public void search() {
        String firstType = firstTagType.getSelectedItem().toString();
        String firstValue = firstTagValue.getText().toString();

        String secondType = secondTagType.getSelectedItem().toString();
        String secondValue = secondTagValue.getText().toString();

        String con = conjunctionBox.getSelectedItem().toString();

        // check if there is no first tag
        if(firstValue.equals("")) {
            Toast toast = Toast.makeText(this, "Missing first tag.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // check if there is conjunction and no second tag
        if(!con.equals("None") && secondValue.equals("")) {
            Toast toast = Toast.makeText(this, "Missing second tag.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // check if theres two tags and no conjunction
        if(con.equals("None") && !secondValue.equals("")) {
            Toast toast = Toast.makeText(this, "Missing conjunction.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        String name = firstType + ": " + firstValue;
        if(!con.equals("None")) {
            name += " " + con + " " + secondType + ": " + secondValue;
        }

        Album result = new Album(name);
        // if searching for one tag
        if(con.equals("None")) {
            for(Album a: Photos.albums) {
                for(Photo p: a.getPhotos()) {
                    if(p.hasTag(firstType, firstValue)) {
                        result.addPhoto(p);
                    }
                }
            }
        } else {
            if(con.equals("AND")) {
                for(Album a: Photos.albums) {
                    for(Photo p: a.getPhotos()) {
                        if(p.hasTag(firstType, firstValue) && p.hasTag(secondType, secondValue)) {
                            result.addPhoto(p);
                        }
                    }
                }
            } else {
                for(Album a: Photos.albums) {
                    for(Photo p: a.getPhotos()) {
                        if(p.hasTag(firstType, firstValue) || p.hasTag(secondType, secondValue)) {
                            result.addPhoto(p);
                        }
                    }
                }
            }
        }

        goBack(result);
    }

    public void goBack(Album a) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();

        bundle.putSerializable(SEARCHED_ALBUM, a);
        intent.putExtras(bundle);

        setResult(RESULT_OK, intent);
        finish();
    }
}