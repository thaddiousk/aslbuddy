package example.org.aslbuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;
import com.google.rpc.context.AttributeContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;

import javax.annotation.Resource;

public class TranslateActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private HashMap<String, ASLObject> alphabet = new HashMap<>();
    private HashMap<String, ASLObject> words = new HashMap<>();
    private SharedPreferences shared;
    private EditText editText;
    private HorizontalScrollView translationScroll;
    private ConstraintLayout showTranslation;
    private Button closeTranslationBtn;
    private Button translateBtn;
    private ImageView micTranslate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);
        translationScroll = findViewById(R.id.translationScroll);
        showTranslation = findViewById(R.id.showTranslation);
        closeTranslationBtn = findViewById(R.id.closeTranslationBtn);
        editText = findViewById(R.id.textTranslate);
        micTranslate = findViewById(R.id.micTranslate);
        translateBtn = findViewById(R.id.translateRequest);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        shared = this.getSharedPreferences("com.example.aslbuddy", Context.MODE_PRIVATE);
        try {
            alphabet = (HashMap<String, ASLObject>) ObjectSerializer.deserialize(shared.getString("alphabet", ObjectSerializer.serialize(new HashMap<>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void translate(View view) throws InterruptedException {

        micTranslate.animate().alpha(0.2f).start();

        permissionToRecordAccepted = ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED;

        if (!permissionToRecordAccepted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        }

        // Setup media recorder
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);

        String fileName = getExternalCacheDir().getAbsolutePath() + "/aud.wav";
        System.out.println("FILENAME: " + fileName);
        recorder.setOutputFile(fileName);

        try {
            recorder.prepare();
            System.out.println("Prepared successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Handler mHandler = new Handler();

        if (permissionToRecordAccepted) {
            System.out.println("STARTING RECORDING");
            recorder.start();   // Recording is now started
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    recorder.stop();
                    System.out.println("STOPPING RECORDING");
                    recorder.reset();   // You can reuse the object by going back to setAudioSource() step
                    recorder.release(); // Now the object cannot be reused

                    // Convert speech to text
                    sampleRecognize(fileName);

                    // Play audio
//                    MediaPlayer mp = new MediaPlayer();
//
//                    try {
//                        mp.setDataSource(fileName);
//                        mp.prepare();
//                        mp.start();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            }, 3000);

        } else {
            System.out.println("Didn't record anything");
        }
    }

    // Speech to text
    // Code taken from: https://cloud.google.com/speech-to-text/docs/sync-recognize
    public void sampleRecognize(String fileName) {
        try {
            // Credentials creation
            InputStream ins = getResources().openRawResource(
                    getResources().getIdentifier("credentials",
                            "raw", getPackageName()));
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(ins));

            SpeechSettings settings = SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            SpeechClient speechClient = SpeechClient.create(settings);

            System.out.println("CREATED SPEECH CLIENT SUCCESSFULLY");

            // The language of the supplied audio
            String languageCode = "en-US";

            // Sample rate in Hertz of the audio data sent
            int sampleRateHertz = 16000;

            // Encoding of audio data sent. This sample sets this explicitly.
            // This field is optional for FLAC and WAV audio formats.
            RecognitionConfig.AudioEncoding encoding = RecognitionConfig.AudioEncoding.AMR_WB;
            RecognitionConfig config =
                    RecognitionConfig.newBuilder()
                            .setLanguageCode(languageCode)
                            .setSampleRateHertz(sampleRateHertz)
                            .setEncoding(encoding)
                            .build();

            System.out.println("CREATED RECOGNITION CONFIG SUCCESSFULLY");
            System.out.println("FILE NAME: " + fileName);

            Path path = Paths.get(fileName);
            byte[] data = Files.readAllBytes(path);
            ByteString content = ByteString.copyFrom(data);
            System.out.println("BYTESTRING: " + content.toString());
            RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(content).build();
            RecognizeRequest request =
                    RecognizeRequest.newBuilder().setConfig(config).setAudio(audio).build();
            RecognizeResponse response = speechClient.recognize(request);

            System.out.println("RECOGNIZED RESULT");
            System.out.println("RESPONSE SIZE: " + response.getResultsList().size());

            for (SpeechRecognitionResult result : response.getResultsList()) {
                // First alternative is the most probable result
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                System.out.printf("Transcript: %s\n", alternative.getTranscript());
                editText.setText(alternative.getTranscript());
            }

            System.out.println("END");
            micTranslate.animate().alpha(1f).start();

        } catch (Exception exception) {
            System.err.println("Failed to create the client due to: " + exception);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }

        if (!permissionToRecordAccepted ) finish();
    }

    public void translateRequest(View view) throws InterruptedException {
        if (editText.getText().length() == 0) {
            Toast.makeText(this, "No text entered", Toast.LENGTH_LONG).show();
            return;
        }
        translateBtn.setVisibility(View.GONE);
        translationScroll.setElevation(10f);
        closeTranslationBtn.setAlpha(1f);
        closeTranslationBtn.setClickable(true);
        closeTranslationBtn.setElevation(10f);
        String str = editText.getText().toString().toLowerCase();
        char[] charArray = str.toCharArray();
        parse(view, charArray);
    }

    public void parse(View view, char[] input) {
        StringBuffer inputBuffer = new StringBuffer();
        inputBuffer.append(input);
        translationScroll.setAlpha(1f);
        if (words.containsKey(inputBuffer.toString())) {
            loadWord(view, inputBuffer.toString());
        } else {
            loadLetters(view, inputBuffer.toString());
        }
    }

    public void loadWord(View view, String word) {
        //to-do
    }

    public void loadLetters(View view, String word) {
        char[] charArr = word.toCharArray();

        // First: Create Image.
        int lastImageId = 0;
        int lastTextId = 0;
        int width;
        int height;
        int letterMargin = 0;
        for (int i = 0; i < charArr.length; i++) {
            ImageView ASL = new ImageView(this);
            ASL.setId(View.generateViewId());
            width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 83, getResources().getDisplayMetrics());
            ASL.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
            // Set image.
            int imageId = getResources().getIdentifier("example.org.aslbuddy:drawable/" + String.valueOf(charArr[i]), null, null);
            ASL.setImageResource(imageId);
            showTranslation.addView(ASL);
            if (i == 0) {
                // Set Constraints.
                ConstraintLayout constraintLayout = findViewById(R.id.showTranslation);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(ASL.getId(), ConstraintSet.START, constraintLayout.getId(), ConstraintSet.START,0);
                constraintSet.connect(ASL.getId(),ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP,0);
                constraintSet.applyTo(constraintLayout);

            } else {
                // Set Constraints.
                ConstraintLayout constraintLayout = findViewById(R.id.showTranslation);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(ASL.getId(), ConstraintSet.START, lastImageId, ConstraintSet.END,8);
                constraintSet.connect(ASL.getId(),ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP,0);
                constraintSet.applyTo(constraintLayout);
            }
            lastImageId = ASL.getId();

            // Second: Create letter beneath the image.
            TextView letter = new TextView(this);
            letter.setId(View.generateViewId());
            letter.setTypeface(ResourcesCompat.getFont(view.getContext(), R.font.roboto_bold));
            letter.setTextSize(TypedValue.COMPLEX_UNIT_SP,24f);
            letter.setTextColor(Color.parseColor("#E5FFDE"));
            letter.setPadding(23, 0, 0, 0);

            if ((charArr[i] > 64 && charArr[i] < 91) || (charArr[i] > 96 && (charArr[i] < 123))) {
                letter.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_white_border));
            } else {
                letter.setBackground(null);
            }
            letter.setText(String.valueOf(charArr[i]));
            width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
            letter.setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
            showTranslation.addView(letter);
            if (i == 0) {
                // Set Constraints.
                ConstraintLayout constraintLayout = findViewById(R.id.showTranslation);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(letter.getId(), ConstraintSet.START, constraintLayout.getId(), ConstraintSet.START,0);
                constraintSet.connect(letter.getId(),ConstraintSet.TOP, ASL.getId(), ConstraintSet.BOTTOM,8);
                constraintSet.applyTo(constraintLayout);
            } else {
                // Set Constraints.
                ConstraintLayout constraintLayout = findViewById(R.id.showTranslation);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.connect(letter.getId(), ConstraintSet.START, lastTextId, ConstraintSet.END, 8);
                constraintSet.connect(letter.getId(),ConstraintSet.TOP, ASL.getId(), ConstraintSet.BOTTOM,8);
                constraintSet.applyTo(constraintLayout);
                letterMargin++;
            }
            lastTextId = letter.getId();
        }
    }

    public void hideTranslation(View view) {
        translationScroll.setAlpha(0f);
        translationScroll.setElevation(0);
        closeTranslationBtn.setClickable(false);
        closeTranslationBtn.setAlpha(0f);
        closeTranslationBtn.setElevation(0);
        showTranslation.removeAllViews();
        translateBtn.setVisibility(View.VISIBLE);
    }
}
