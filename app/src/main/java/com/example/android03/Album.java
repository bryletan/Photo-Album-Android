package com.example.android03;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.*;

public class Album implements Serializable {

    private String name;
    private int size;
    private ArrayList<Photo> albumPhotos;

    public Album(String name) {
        this.name = name;
        this.size = 0;
        albumPhotos = new ArrayList<Photo>();
    }

    public void addPhoto(Photo p) {
        albumPhotos.add(p);
        size++;
    }

    public void removePhoto(Photo p) {
        this.albumPhotos.remove(p);
        //p.removeAlbum(this);
        this.size--;
    }

    public void updatePhoto(Photo p) {
        for(int i = 0; i < this.albumPhotos.size(); i++) {
            if(this.albumPhotos.get(i).equals(p)) {
                this.albumPhotos.set(i, p);
                return;
            }
        }
    }

    public boolean equals(Album a) {
        return this.name.equals(a.getName());
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) { this.name = name; }

    public void setPhotos(ArrayList<Photo> photos) {
        this.albumPhotos = photos;
    }

    public int getSize() {
        return this.size;
    }

    public ArrayList<Photo> getPhotos() {
        return this.albumPhotos;
    }

    public Photo getPhoto(int pos) {
        return this.albumPhotos.get(pos);
    }
}
