package example.org.aslbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.common.util.concurrent.Runnables;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private HashMap<String, ASLObject> alphabet = new HashMap<>();

    private SharedPreferences shared;

    MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView titleWords = findViewById(R.id.textView);
        Button translateBtn = findViewById(R.id.translateButton);
        Button gameBtn = findViewById(R.id.gameButton);
        ImageView image2 = findViewById(R.id.imageView2);
        ImageView image3 = findViewById(R.id.imageView3);
        ImageView image4 = findViewById(R.id.imageView4);
        titleWords.setY(-500f);
        translateBtn.setY(1000f);
        gameBtn.setY(1000f);

        //Enable persistent storage.
        shared = this.getSharedPreferences("com.example.aslbuddy", Context.MODE_PRIVATE);
        try {
            alphabet = (HashMap<String, ASLObject>) ObjectSerializer.deserialize(shared.getString("alphabet", ObjectSerializer.serialize(new HashMap<>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (alphabet.isEmpty()) {
            fillAlphabet();
        }
        titleWords.animate().alphaBy(1f).yBy(700f).setDuration(2000L).start();
        translateBtn.animate().yBy(-250f).setDuration(2000L).start();
        gameBtn.animate().yBy(-50f).setDuration(2000L).start();
        image2.animate().alphaBy(1f).setStartDelay(1000L).setDuration(400L).start();
        image3.animate().alphaBy(1f).setStartDelay(1000L).setDuration(800L).start();
        image4.animate().alphaBy(2f).setStartDelay(1000L).setDuration(1200L).start();
        Thread t = new Thread(){
            public void run(){
                mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.opening);
                mPlayer.setLooping(false);
                mPlayer.setVolume(0.15f, 0.15f);
                mPlayer.start();
            }
        };
        t.start();
    }

    public void onDestroy() {
        mPlayer.stop();
        super.onDestroy();
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

        try {
            shared.edit().putString("alphabet", ObjectSerializer.serialize(alphabet)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ContextCompat.getDrawable(this, R.drawable.a)
    }

    public void loadGame(View view) {
        Intent intent = new Intent(this, GameMainActivity.class);
        startActivity(intent);
    }

    public void loadTranslate(View view) {
        Intent intent = new Intent(this, TranslateActivity.class);
        startActivity(intent);
    }
}
