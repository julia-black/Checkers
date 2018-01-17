package game.chernousovaya.checkers.activities;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import game.chernousovaya.checkers.R;
import game.chernousovaya.checkers.structure.Board;
import game.chernousovaya.checkers.structure.Cell;

public class GameActivity extends AppCompatActivity {

    private static final int ROWS = 8;
    private static final int COLUMNS = 8;
    private static final String LOG_TAG = "GameActivity";
    private static int numberMove; //номер хода

    private Board mBoard;
    private boolean isChooseCheck = false;
    private Cell chooseCell;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(LOG_TAG, v.getId() + "");
        }
    };

    //Игрок играет за черные (они делают 1й ход)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mBoard = new Board();
        chooseCell = new Cell();
        renderBoard();

        numberMove = 1;
        Toast toast = Toast.makeText(getApplicationContext(),
                "Ваш ход",
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void renderBoard() {
        updateScore();
        mBoard.showBoard();
        final TableLayout tableLayout = (TableLayout) findViewById(R.id.board);
        tableLayout.removeAllViews();
        tableLayout.setBackgroundColor(Color.WHITE);

        for (int i = 0; i < ROWS; i++) {
            final TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));

            tableRow.setClickable(true);
            tableRow.setOnClickListener(onClickListener);
            for (int j = 0; j < COLUMNS; j++) {
                final ImageView imageView = new ImageView(this);

                if (isBlackCell(i, j)) {
                    imageView.setBackgroundColor(Color.BLACK);
                    if (mBoard.getCell(i, j) == 1) {
                        imageView.setImageResource(R.drawable.white);
                    } else if (mBoard.getCell(i, j) == 2) {
                        imageView.setImageResource(R.drawable.black);
                    } else {
                        imageView.setImageResource(R.drawable.black_cell);
                    }
                    final int finalI = i;
                    final int finalJ = j;
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mBoard.getCell(finalI, finalJ) == 2)//если это шашка игрока
                            {
                                if (isChooseCheck) { //если она уже была выбрана и игрок хочет изменить выбор своей шашки
                                    chooseCell.setX(finalI);
                                    chooseCell.setY(finalJ);
                                    renderBoard();
                                }
                                imageView.setBackgroundColor(Color.GREEN);
                                isChooseCheck = true;
                                chooseCell.setX(finalI);
                                chooseCell.setY(finalJ);
                            } else {
                                if (isChooseCheck) { //если игрок уже выбрал шашку и хочет сделать ход
                                    if (mBoard.moveChecker(chooseCell.getX(), chooseCell.getY(), finalI, finalJ, 2, getApplicationContext())) {
                                        renderBoard();
                                        Cell newCell = movementEnemy(2, 1);
                                        mBoard.moveChecker(2, 1, newCell.getX(), newCell.getY(), 1, getApplicationContext());
                                        renderBoard();

                                        Toast toast = Toast.makeText(getApplicationContext(),
                                                "Ваш ход",
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            "Сначала выберите шашку, которой хотите совершить ход",
                                            Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                            Log.i(LOG_TAG, Integer.toString(finalI) + " " + Integer.toString(finalJ));
                        }
                    });
                }
                if (i == chooseCell.getX() && j == chooseCell.getY()) {
                    imageView.setBackgroundColor(Color.GREEN);
                }
                tableRow.addView(imageView, j);
            }
            tableLayout.addView(tableRow, i);
        }
    }

    private void updateScore(){
        TextView scoreView = (TextView) findViewById(R.id.score);
        scoreView.setText(mBoard.getScore().getScoreBlack() + ":" + mBoard.getScore().getScoreWhite());

    }
    private boolean isBlackCell(int i, int j) {
        return ((i % 2 == 0 && j % 2 != 0) || (i % 2 != 0 && j % 2 == 0));
    }

    // //Проверка на то, можем ли мы ходить в выбранную клетку по правилам шашек
    // private boolean isValidMove(){
    //     if()
    // }

    //Ход противника
    private Cell movementEnemy(int i, int j) { //ход черных

        Log.i(LOG_TAG, "movement black");
        //тут когда-нибудь будет крутая логика

        final Cell[] newCell = {new Cell(3, 2)};
        return newCell[0];
    }


}
