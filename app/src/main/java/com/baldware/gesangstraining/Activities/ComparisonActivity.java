package com.baldware.gesangstraining.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baldware.gesangstraining.AudioRecording.MemoryHandler;
import com.baldware.gesangstraining.R;
import com.baldware.gesangstraining.Utils.FileHandler;
import com.baldware.gesangstraining.Views.SpectrogramView;

public class ComparisonActivity extends AppCompatActivity {

    private SpectrogramView mSpectrogramView1;
    private SpectrogramView mSpectrogramView2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_comparison);

        Intent intent = getIntent();
        String fileName1 = intent.getStringExtra("file1");
        String fileName2 = intent.getStringExtra("file2");

        MemoryHandler memoryHandler1 = FileHandler.load(this, fileName1);
        MemoryHandler memoryHandler2 = FileHandler.load(this, fileName2);

        mSpectrogramView1 = findViewById(R.id.comparison_spectrogram_view_1);
        mSpectrogramView1.setMemoryHandler(memoryHandler1);
        mSpectrogramView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpectrogramView1.setNoteView(!mSpectrogramView1.getNoteView());
            }
        });

        mSpectrogramView2 = findViewById(R.id.comparison_spectrogram_view_2);
        mSpectrogramView2.setMemoryHandler(memoryHandler2);
        mSpectrogramView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpectrogramView2.setNoteView(!mSpectrogramView2.getNoteView());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpectrogramView1.getFetchAndRedrawTimer().cancel();
        mSpectrogramView2.getFetchAndRedrawTimer().cancel();
    }
}
