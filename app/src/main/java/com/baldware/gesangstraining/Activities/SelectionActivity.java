package com.baldware.gesangstraining.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baldware.gesangstraining.R;
import com.baldware.gesangstraining.Utils.FileHandler;

import java.util.ArrayList;
import java.util.Arrays;

public class SelectionActivity extends AppCompatActivity {

    private String[] mFileNames;
    private ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_selection);

        ListView listView = findViewById(R.id.selection_list_view);

        String[] fileNames = FileHandler.getFileNames(this);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(fileNames)));

        listView.setAdapter(mArrayAdapter);

        mFileNames = new String[2];

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listView.getChildAt(position).setEnabled(false);

                if (mFileNames[0] == null) {
                    mFileNames[0] = fileNames[position];
                } else {
                    mFileNames[1] = fileNames[position];


                    //move from one activity to another
                    Intent intent = new Intent(SelectionActivity.this, ComparisonActivity.class);
                    intent.putExtra("file1", mFileNames[0]);
                    intent.putExtra("file2", mFileNames[1]);
                    SelectionActivity.this.startActivity(intent);

                    mFileNames[0] = null;
                    mFileNames[1] = null;
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        listView.getChildAt(i).setEnabled(true);
                    }
                }
            }
        });

        Button deleteButton = findViewById(R.id.selection_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileHandler.deleteSaveFiles(v.getContext());
                mArrayAdapter.clear();
                mArrayAdapter.notifyDataSetChanged();
            }
        });
    }
}
