package com.example.android03;

import android.content.Context;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ListViewAdapter extends ArrayAdapter<Photo> {

    ListViewAdapter(@NonNull Context context, ArrayList<Photo> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        // Get the {@link Word} object located at this position in the list
        Photo currentItem = getItem(position);

        ImageView thumbnail = listItem.findViewById(R.id.imageViewPhoto);
        thumbnail.setImageURI(Uri.parse(currentItem.getURI()));

        TextView title = listItem.findViewById(R.id.textViewName);
        title.setText(currentItem.getName());

        return listItem;
    }

}