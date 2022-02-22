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
import com.baldware.gesangstraining.Views.AdviceTextView;
import com.baldware.gesangstraining.Views.FrequencyTextView;
import com.baldware.gesangstraining.Views.FrequencyView;
import com.baldware.gesangstraining.Views.SpectrogramView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ErrorDetectionActivity extends AppCompatActivity {

    // Variables
    private AudioRecorder mAudioRecorder;
    private SpectrogramView mSpectrogramView;
    private FrequencyTextView mFrequencyTextView;
    private FrequencyView mFrequencyView;
    private AdviceTextView mAdviceTextView1;
    private AdviceTextView mAdviceTextView2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_error_detection);

        // ----- Memory Handler -----
        MemoryHandler memoryHandler = new MemoryHandler();

        // ----- Audio Recorder -----
        mAudioRecorder = new AudioRecorder(ErrorDetectionActivity.this, memoryHandler);

        if (!mAudioRecorder.startRecording()) {
            Toast.makeText(this, "Currently unable to start recording - please try again!", Toast.LENGTH_SHORT).show();
        }

        // ----- Spectrogram View -----
        mSpectrogramView = findViewById(R.id.errorDetection_spectrogram_view);
        mSpectrogramView.setMemoryHandler(memoryHandler);
        mSpectrogramView.setNoteView(true);

        // ----- Frequency Text View -----
        mFrequencyTextView = findViewById(R.id.errorDetection_frequency_text_view);
        mFrequencyTextView.setMemoryHandler(memoryHandler);
        mFrequencyTextView.setNoteView(true);

        // ----- Frequency View -----
        mFrequencyView = findViewById(R.id.errorDetection_frequency_view);
        mFrequencyView.setMemoryHandler(memoryHandler);
        mFrequencyView.setNoteView(true);

        // ----- Advice Text View -----
        mAdviceTextView1 = findViewById(R.id.errorDetection_singer_advice_text_view_1);
        mAdviceTextView1.setMemoryHandler(memoryHandler);
        mAdviceTextView1.setAdviceMode(AdviceTextView.Mode.NOISE);

        // ----- Advice Text View -----
        mAdviceTextView2 = findViewById(R.id.errorDetection_singer_advice_text_view_2);
        mAdviceTextView2.setMemoryHandler(memoryHandler);
        mAdviceTextView2.setAdviceMode(AdviceTextView.Mode.SINGER_FORMANT);

        // Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.errorDetection_bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.pause_resume_menu_item:
                        if (mAudioRecorder.isRecording()) {
                            mAudioRecorder.stopRecording();
                            mAudioRecorder = new AudioRecorder(ErrorDetectionActivity.this, memoryHandler);
                            item.setIcon(R.drawable.ic_baseline_pause_24);
                        } else {
                            mAudioRecorder.startRecording();
                            item.setIcon(R.drawable.ic_baseline_play_arrow_24);
                        }
                        break;
                    case R.id.save_menu_item:
                        mAudioRecorder.stopRecording();
                        FileHandler.save(ErrorDetectionActivity.this, memoryHandler);
                        mAudioRecorder = new AudioRecorder(ErrorDetectionActivity.this, memoryHandler);
                        mAudioRecorder.startRecording();
                        Toast.makeText(ErrorDetectionActivity.this, "Spectrogram has been saved!", Toast.LENGTH_SHORT).show();
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
        mAdviceTextView1.getFetchAndRedrawTimer().cancel();
        mAdviceTextView2.getFetchAndRedrawTimer().cancel();
        mAudioRecorder.stopRecording();
    }
}
