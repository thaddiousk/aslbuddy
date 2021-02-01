package example.org.aslbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LevelActivity extends AppCompatActivity {

    private static final String TAG = "LevelActivity";

    private HashMap<String, ASLObject> alphabet = new HashMap<>();

    private SharedPreferences shared;

    private int currentLevel;

    private TextView currentLetterTextView;
    private TextView questionTextView;

    private ImageButton topLeftImageButton;
    private ImageButton topRightImageButton;
    private ImageButton bottomLeftImageButton;
    private ImageButton bottomRightImageButton;

    private Map<Integer, ImageButton> buttonPositions = new HashMap<>();
    private Map<Integer, String> buttonValues = new HashMap<>();

    private String currentLetter;
    private int correctPosition;

    private String word;
    private Spannable wordSpan;
    private int wordPosition;

    private int numberOfQuestions;
    private int correctQuestions = 0;
    private int incorrectQuestions = 0;

    private final int NEXT_QUESTION_DELAY = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.level_main);

        currentLevel = getIntent().getIntExtra("LEVEL", 1);
        currentLetterTextView = findViewById(R.id.textViewResult);
        questionTextView = findViewById(R.id.textView2);
        topLeftImageButton = findViewById(R.id.imageButton1);
        topRightImageButton = findViewById(R.id.imageButton2);
        bottomLeftImageButton = findViewById(R.id.imageButton3);
        bottomRightImageButton = findViewById(R.id.imageButton4);

        buttonPositions.put(0, topLeftImageButton);
        buttonPositions.put(1, topRightImageButton);
        buttonPositions.put(2, bottomLeftImageButton);
        buttonPositions.put(3, bottomRightImageButton);

        System.out.println("LEVEL: " + currentLevel);

        fillAlphabet();

        if (currentLevel == 1) {
            levelOne();
            numberOfQuestions = 10;
            questionTextView.setText("What's this letter in ASL?");
        }
        else if (currentLevel == 2) {

            questionTextView.setText("What's this word in ASL?");
            word = getRandomWord();
            wordPosition = 0;
            numberOfQuestions = word.length();
            wordSpan = new SpannableString(word);
            wordSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            currentLetterTextView.setText(wordSpan, TextView.BufferType.SPANNABLE);
            levelTwo();
        }

        topLeftImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonValues.get(0).equals(currentLetter)) {
                    choseCorrectAnswer(topLeftImageButton);
                } else {
                    choseWrongAnswer(topLeftImageButton, 0);
                }
            }
        });

        topRightImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonValues.get(1).equals(currentLetter)) {
                    choseCorrectAnswer(topRightImageButton);
                } else {
                    choseWrongAnswer(topRightImageButton, 1);
                }
            }
        });

        bottomLeftImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonValues.get(2).equals(currentLetter)) {
                    choseCorrectAnswer(bottomLeftImageButton);
                } else {
                    choseWrongAnswer(bottomLeftImageButton, 2);
                }
            }
        });

        bottomRightImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonValues.get(3).equals(currentLetter)) {
                    choseCorrectAnswer(bottomRightImageButton);
                } else {
                    choseWrongAnswer(bottomRightImageButton, 3);
                }
            }
        });
    }

    public void back(View view) {
        Intent intent = new Intent(this, GameMainActivity.class);
        startActivity(intent);
    }

    public void home(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void choseCorrectAnswer(ImageButton imageBtn) {

        toggleButtons(false);
        ImageButton correctImageBtn = buttonPositions.get(correctPosition);
        correctImageBtn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.correctAnswerCol));
        buttonValues.clear();
        numberOfQuestions--;
        correctQuestions++;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(currentLevel == 1) {

                    if (checkIfGameOver()) {
                        return;
                    }

                    levelOne();

                }
                else if(currentLevel == 2) {
                    wordSpan.setSpan(new ForegroundColorSpan(Color.GREEN), wordPosition, wordPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    currentLetterTextView.setText(wordSpan, TextView.BufferType.SPANNABLE);
                    wordPosition++;
                    Log.d(TAG, "choseWrongAnswer: " + numberOfQuestions);
                    if(checkIfGameOver()) {
                        return;
                    }
                    levelTwo();
                }
            }
        }, NEXT_QUESTION_DELAY);

    }

    public void choseWrongAnswer(ImageButton imageBtn, int input) {

        toggleButtons(false);
        ImageButton correctImageBtn = buttonPositions.get(correctPosition);
        correctImageBtn.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.correctAnswerCol));

        ImageButton wrongImageButton = buttonPositions.get(input);
        wrongImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.wrongAnswerCol));


        numberOfQuestions--;
        incorrectQuestions++;


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(currentLevel == 1) {
                    checkIfGameOver();
                    levelOne();

                }
                else if(currentLevel == 2) {
                    wordSpan.setSpan(new ForegroundColorSpan(Color.RED), wordPosition, wordPosition + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    currentLetterTextView.setText(wordSpan, TextView.BufferType.SPANNABLE);
                    wordPosition++;
                    Log.d(TAG, "choseWrongAnswer: " + numberOfQuestions);
                    if(checkIfGameOver()) {
                        return;
                    }
                    levelTwo();
                }
            }
        }, NEXT_QUESTION_DELAY);
    }

    private boolean checkIfGameOver() {
        if (numberOfQuestions == 0) {
            Intent intent = new Intent(this, GameResult.class);
            intent.putExtra("CORRECT", correctQuestions);
            intent.putExtra("INCORRECT", incorrectQuestions);
            startActivity(intent);
            finishAffinity();
            return true;
        }
        return false;
    }

    public void levelOne() {

        toggleButtons(true);
        topLeftImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.selectAnswerCol));
        topRightImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.selectAnswerCol));
        bottomLeftImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.selectAnswerCol));
        bottomRightImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.selectAnswerCol));
        Set<String> seenLetters = new HashSet<>();
        Set<Integer> positions = new HashSet<>(buttonPositions.keySet());
        currentLetter = getRandomLetter(seenLetters);
        currentLetterTextView.setText(currentLetter);

        int randPosition = getRandomNumber(0, 3);
        positions.remove(randPosition);
        correctPosition = randPosition;
        buttonPositions.get(randPosition).setImageDrawable(getResources().getDrawable(alphabet.get(currentLetter).getDrawableNum(), null));
        buttonValues.put(randPosition, currentLetter);

        System.out.println("Position: " + randPosition + " should be " + alphabet.get(currentLetter).getName());

        while (positions.size() > 0) {
            randPosition = getRandomNumber(0, 3);

            if (positions.contains(randPosition)) {
                String newLetter = getRandomLetter(seenLetters);
                System.out.println("Position: " + randPosition + " should be " + alphabet.get(newLetter).getName());
                buttonPositions.get(randPosition).setImageDrawable(getResources().getDrawable(alphabet.get(newLetter).getDrawableNum(), null));
                buttonValues.put(randPosition, newLetter);
                positions.remove(randPosition);
            }
        }
    }



    public void levelTwo() {

        toggleButtons(true);
        topLeftImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.selectAnswerCol));
        topRightImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.selectAnswerCol));
        bottomLeftImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.selectAnswerCol));
        bottomRightImageButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.selectAnswerCol));
        Set<String> seenLetters = new HashSet<>();
        Set<Integer> positions = new HashSet<>(buttonPositions.keySet());
        int randPosition = getRandomNumber(0, 3);
        positions.remove(randPosition);
        currentLetter = Character.toString(this.word.charAt(wordPosition));
        Log.d(TAG, "levelTwo: " + currentLetter);
        seenLetters.add(currentLetter);
        correctPosition = randPosition;
        buttonPositions.get(randPosition).setImageDrawable(getResources().getDrawable(alphabet.get(currentLetter).getDrawableNum(), null));

        buttonValues.put(randPosition, currentLetter);

        while (positions.size() > 0) {
            randPosition = getRandomNumber(0, 3);
            if (positions.contains(randPosition)) {
                String newLetter = getRandomLetter(seenLetters);
                System.out.println("Position: " + randPosition + " should be " + alphabet.get(newLetter).getName());
                buttonPositions.get(randPosition).setImageDrawable(getResources().getDrawable(alphabet.get(newLetter).getDrawableNum(), null));
                buttonValues.put(randPosition, newLetter);
                positions.remove(randPosition);
            }
        }
    }

    private String getRandomWord(){
        // TODO: generate random words
        String[] words = new String[]{"influence", "float", "idea", "banish", "literature", "robot"};
        int randPos = getRandomNumber(0, words.length-1);
        return words[randPos];
    }

    private String getRandomLetter(Set<String> seenLetters) {

        int asciiChar = getRandomNumber(97, 122);
        String character = Character.toString((char) asciiChar);

        while (seenLetters.contains(character)) {
            asciiChar = getRandomNumber(97, 122);
            character = Character.toString((char) asciiChar);
        }

        seenLetters.add(character);
        return character;
    }

    public int getRandomNumber(int min, int max) {
        return (int) (Math.random() * ((max - min) + 1)) + min;
    }

    public void toggleButtons(boolean enabled) {
        topLeftImageButton.setClickable(enabled);
        topRightImageButton.setClickable(enabled);
        bottomLeftImageButton.setClickable(enabled);
        bottomRightImageButton.setClickable(enabled);
    }

    private void fillAlphabet() {
        alphabet.put("a", new ASLObject("a", R.drawable.a, "pic", false));
        alphabet.put("b", new ASLObject("b", R.drawable.b, "pic", false));
        alphabet.put("c", new ASLObject("c", R.drawable.c, "pic", false));
        alphabet.put("d", new ASLObject("d", R.drawable.d, "pic", false));
        alphabet.put("e", new ASLObject("e", R.drawable.e, "pic", false));
        alphabet.put("f", new ASLObject("f", R.drawable.f, "pic", false));
        alphabet.put("g", new ASLObject("g", R.drawable.g, "pic", false));
        alphabet.put("h", new ASLObject("h", R.drawable.h, "pic", false));
        alphabet.put("i", new ASLObject("i", R.drawable.i, "pic", false));
        alphabet.put("j", new ASLObject("j", R.drawable.j, "pic", false));
        alphabet.put("k", new ASLObject("k", R.drawable.k, "pic", false));
        alphabet.put("l", new ASLObject("l", R.drawable.l, "pic", false));
        alphabet.put("m", new ASLObject("m", R.drawable.m, "pic", false));
        alphabet.put("n", new ASLObject("n", R.drawable.n, "pic", false));
        alphabet.put("o", new ASLObject("o", R.drawable.o, "pic", false));
        alphabet.put("p", new ASLObject("p", R.drawable.p, "pic", false));
        alphabet.put("q", new ASLObject("q", R.drawable.q, "pic", false));
        alphabet.put("r", new ASLObject("r", R.drawable.r, "pic", false));
        alphabet.put("s", new ASLObject("s", R.drawable.s, "pic", false));
        alphabet.put("t", new ASLObject("t", R.drawable.t, "pic", false));
        alphabet.put("u", new ASLObject("u", R.drawable.u, "pic", false));
        alphabet.put("v", new ASLObject("v", R.drawable.v, "pic", false));
        alphabet.put("w", new ASLObject("w", R.drawable.w, "pic", false));
        alphabet.put("x", new ASLObject("x", R.drawable.x, "pic", false));
        alphabet.put("y", new ASLObject("y", R.drawable.y, "pic", false));
        alphabet.put("z", new ASLObject("z", R.drawable.z, "pic", false));
    }
}
