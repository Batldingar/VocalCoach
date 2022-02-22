package com.baldware.gesangstraining.Activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baldware.gesangstraining.AudioRecording.AudioRecorder;
import com.baldware.gesangstraining.AudioRecording.MemoryHandler;
import com.baldware.gesangstraining.R;
import com.baldware.gesangstraining.Utils.FileHandler;
import com.baldware.gesangstraining.Views.FrequencyTextView;
import com.baldware.gesangstraining.Views.FrequencyView;
import com.baldware.gesangstraining.Views.SpectrogramView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class VisualisingActivity extends AppCompatActivity {

    // Variables
    private AudioRecorder mAudioRecorder;
    private SpectrogramView mSpectrogramView;
    private FrequencyTextView mFrequencyTextView;
    private FrequencyView mFrequencyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_visualising);

        // ----- Memory Handler -----
        MemoryHandler memoryHandler = new MemoryHandler();

        // ----- Audio Recorder -----
        mAudioRecorder = new AudioRecorder(VisualisingActivity.this, memoryHandler);

        if (!mAudioRecorder.startRecording()) {
            Toast.makeText(this, "Currently unable to start recording - please try again!", Toast.LENGTH_SHORT).show();
        }

        // ----- Spectrogram View -----
        mSpectrogramView = findViewById(R.id.errorDetection_spectrogram_view);
        mSpectrogramView.setMemoryHandler(memoryHandler);

        // ----- Frequency Text View -----
        mFrequencyTextView = findViewById(R.id.errorDetection_frequency_text_view);
        mFrequencyTextView.setMemoryHandler(memoryHandler);

        // ----- Frequency View -----
        mFrequencyView = findViewById(R.id.errorDetection_frequency_view);
        mFrequencyView.setMemoryHandler(memoryHandler);

        // Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.errorDetection_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pause_resume_menu_item:
                        if (mAudioRecorder.isRecording()) {
                            mAudioRecorder.stopRecording();
                            mAudioRecorder = new AudioRecorder(VisualisingActivity.this, memoryHandler);
                            item.setIcon(R.drawable.ic_baseline_pause_24);
                        } else {
                            mAudioRecorder.startRecording();
                            item.setIcon(R.drawable.ic_baseline_play_arrow_24);
                        }
                        break;
                    case R.id.save_menu_item:
                        mAudioRecorder.stopRecording();
                        FileHandler.save(VisualisingActivity.this, memoryHandler);
                        mAudioRecorder = new AudioRecorder(VisualisingActivity.this, memoryHandler);
                        mAudioRecorder.startRecording();
                        Toast.makeText(VisualisingActivity.this, "Spectrogram has been saved!", Toast.LENGTH_SHORT).show();
                        break;
                }

                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSpectrogramView.getFetchAndRedrawTimer().cancel();
        mFrequencyTextView.getFetchAndRedrawTimer().cancel();
        mFrequencyView.getFetchAndRedrawTimer().cancel();
        mAudioRecorder.stopRecording();
    }
}
