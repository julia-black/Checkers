package game.chernousovaya.checkers.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import game.chernousovaya.checkers.R;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startButton.setBackgroundResource(R.drawable.start_but_push);
                Intent intent = new Intent(MainMenu.this, GameActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        final Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setBackgroundResource(R.drawable.start_but_down);

    }


}
