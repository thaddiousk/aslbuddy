package example.org.aslbuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameMainActivity extends AppCompatActivity {

    private HashMap<String, ASLObject> alphabet = new HashMap<>();
    private Profile profile;

    private SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main);

        shared = this.getSharedPreferences("com.example.aslbuddy", Context.MODE_PRIVATE);
        try {
            alphabet = (HashMap<String, ASLObject>) ObjectSerializer.deserialize(shared.getString("alphabet", ObjectSerializer.serialize(new HashMap<>())));
            profile = (Profile) ObjectSerializer.deserialize(shared.getString("profile", ObjectSerializer.serialize(new Profile(1,0))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setLevelImage(profile.getLevel(), profile.getPart());
    }

    public void back(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void levelOne(View view) {
        level(1);
    }

    public void levelTwo(View view) {
        if(profile.getLevel() < 2) {
            return;
        }
        level(2);
    }

    public void levelThree(View view) {
        if(profile.getLevel() < 3) {
            return;
        }
        level(3);
    }

    public void level(int level) {
        Intent intent = new Intent(this, LevelActivity.class);
        intent.putExtra("LEVEL", level);
        startActivity(intent);
    }

    private void setLevelImage(int level, int part) {
        TextView level1 = findViewById(R.id.level1);
        TextView level2 = findViewById(R.id.level2);
        TextView level3 = findViewById(R.id.level3);
        Map<String, ImageView> levelsToMedal = new HashMap<>();
        levelsToMedal.put("level1_part1", findViewById(R.id.level1_medal1));
        levelsToMedal.put("level1_part2", findViewById(R.id.level1_medal2));
        levelsToMedal.put("level1_part3", findViewById(R.id.level1_medal3));
        levelsToMedal.put("level2_part1", findViewById(R.id.level2_medal1));
        levelsToMedal.put("level2_part2", findViewById(R.id.level2_medal2));
        levelsToMedal.put("level2_part3", findViewById(R.id.level2_medal3));
        levelsToMedal.put("level3_part1", findViewById(R.id.level3_medal1));
        levelsToMedal.put("level3_part2", findViewById(R.id.level3_medal2));
        levelsToMedal.put("level3_part3", findViewById(R.id.level3_medal3));

        for(int i = 1; i <= level; i++) {
            if(i == 2) {
                level2.setAlpha(1.0f);
            }
            else if(i == 3) {
                level3.setAlpha(1.0f);
            }
            int medals = i == level ? part : 3;
            for( int j = 1;  j <= medals; j++) {
                String levelMedal = String.format("level%d_part%d", i, j);
                ImageView medal = levelsToMedal.get(levelMedal);
                medal.setAlpha(1.0f);
            }
        }

    }
}
