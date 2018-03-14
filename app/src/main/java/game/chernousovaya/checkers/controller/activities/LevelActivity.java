package game.chernousovaya.checkers.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import game.chernousovaya.checkers.R;


public class LevelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);


        Button twoPlayersButton = (Button) findViewById(R.id.button_start_two);
        Button onePlayerEasyButton = (Button) findViewById(R.id.button_start_one_easy);
        Button onePlayerHardButton = (Button) findViewById(R.id.button_start_one_hard);

        onePlayerEasyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LevelActivity.this, GameActivity.class);
                intent.putExtra("count_players", 1);
                intent.putExtra("level", "easy");
                startActivity(intent);
            }
        });

        onePlayerHardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LevelActivity.this, GameActivity.class);
                intent.putExtra("count_players", 1);
                intent.putExtra("level", "hard");
                startActivity(intent);
            }
        });

        twoPlayersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LevelActivity.this, GameActivity.class);
                intent.putExtra("count_players", 2);
                intent.putExtra("level", "easy");
                startActivity(intent);
            }
        });
    }
}