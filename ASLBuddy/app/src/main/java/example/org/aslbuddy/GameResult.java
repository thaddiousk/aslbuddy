package example.org.aslbuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class GameResult extends AppCompatActivity {

    private RatingBar ratingBar;
    private TextView score;
    private TextView feedbackTextView;

    private final double PASSING_THRESHOLD = 0.5;
    private SharedPreferences shared;
    MediaPlayer mPlayer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_result);

        shared = this.getSharedPreferences("com.example.aslbuddy", Context.MODE_PRIVATE);

        Intent intent = getIntent();
        int correctQuestions = intent.getIntExtra("CORRECT", 0);
        int incorrectQuestions = intent.getIntExtra("INCORRECT", 0);
        int totalQuestions = incorrectQuestions + correctQuestions;
        System.out.println("correct shurui" + correctQuestions);
        System.out.println("incorrect shurui" + incorrectQuestions);

        ratingBar = findViewById(R.id.ratingBar);

        int correctPercentage = Math.round( ((float)correctQuestions / totalQuestions) * 100);
        double percentage = (double) correctQuestions / (double) totalQuestions;
        System.out.println("PERCENTAGE: " + percentage);
        score = findViewById(R.id.textViewPercentage);
        feedbackTextView = findViewById(R.id.textView5);
        score.setText(correctPercentage + "%");

        if (percentage >= PASSING_THRESHOLD) {
            levelPassed();
            updateProfile();
        } else {
            levelFailed();
        }

        ratingBar.setRating((float)correctPercentage /20);
    }

    private void levelFailed() {
        Thread t = new Thread(){
            public void run(){
                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.failure);
                mPlayer.setLooping(false);
                mPlayer.setVolume(0.15f, 0.15f);
                mPlayer.start();
            }
        };
        t.start();
        feedbackTextView.setText("Try again!");
        feedbackTextView.setTextColor(Color.RED);
    }

    private void levelPassed() {
        Thread t = new Thread(){
            public void run(){
                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.success);
                mPlayer.setLooping(false);
                mPlayer.setVolume(0.15f, 0.15f);
                mPlayer.start();
            }
        };
        t.start();
        feedbackTextView.setText("You passed!");
        feedbackTextView.setTextColor(Color.GREEN);
    }

    public void back(View view) {
        Intent intent = new Intent(this, GameMainActivity.class);
        startActivity(intent);
    }

    public void home(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void updateProfile() {
        Profile profile;
        try {
            profile = (Profile) ObjectSerializer.deserialize(shared.getString("profile", ObjectSerializer.serialize(new Profile(1,0))));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if(profile.getPart() < 2) {
            profile.setPart(profile.getPart() + 1);
        }
        else if(profile.getLevel() < 3){
            profile.setPart(0);
            profile.setLevel(profile.getLevel() + 1);
        }
        try {
            shared.edit().putString("profile", ObjectSerializer.serialize(profile)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
