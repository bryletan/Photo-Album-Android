package com.example.android03;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

public class Photo implements Serializable {

    private String name;
    private HashSet<String> locationTags;
    private HashSet<String> personTags;

    private String uri;

    public Photo() {

    }

    public Photo(String name, String uri) {
        this.name = name;
        this.uri = uri;
        this.locationTags = new HashSet<>();
        this.personTags = new HashSet<>();
    }

    /*
    Getters
     */
    public String getName() { return this.name; }

    public String getURI() { return this.uri; }

    public String toString() { return this.name; }

    public String getTags() {
        StringBuilder builder = new StringBuilder();

        // add all the location tags
        for(String tag: this.locationTags) {
            builder.append("Location: " + tag + "\n");
        }

        // add all the person tags
        for(String tag: this.personTags) {
            builder.append("Person: " + tag + "\n");
        }

        return builder.toString();
    }

    public HashSet<String> getLocationTags() {
        return this.locationTags;
    }

    public HashSet<String> getPersonTags() {
        return this.personTags;
    }

    public boolean hasTags() {
        return this.locationTags.size() != 0 || this.personTags.size() != 0;
    }

    public boolean equals(Photo p) {
        return this.name.equals(p.getName());
    }

    /*
    Setters
     */
    public void setURI(String uri) {
        this.uri = uri;
    }

    public boolean hasTag(String type, String value) {
        String val = value.toLowerCase();
        if(type.equals("Location")) {
            for(String tag : this.locationTags) {
                if(tag.toLowerCase().equals(val)) {
                    return true;
                }
            }

            return false;
        }

        for(String tag : this.personTags) {
            if(tag.toLowerCase().equals(val)) {
                return true;
            }
        }

        return false;
    }

    public void addTag(String type, String value) {
        if(type.equals("Location")) {
            this.locationTags.add(value);
        } else {
            this.personTags.add(value);
        }
    }

    public void removeTag(String type, String value) {
        if(type.equals("Location")) {
            this.locationTags.remove(value);
        } else {
            this.personTags.remove(value);
        }
    }
}
