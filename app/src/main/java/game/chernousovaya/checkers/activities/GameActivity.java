package game.chernousovaya.checkers.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import game.chernousovaya.checkers.R;

public class GameActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }
}
