package game.chernousovaya.checkers.controller.activities;


import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import game.chernousovaya.checkers.R;
import game.chernousovaya.checkers.model.Board;
import game.chernousovaya.checkers.model.Cell;
import game.chernousovaya.checkers.model.Move;
import game.chernousovaya.checkers.model.PairCell;

public class GameActivity extends AppCompatActivity {

    private static final int ROWS = 8;
    private static final int COLUMNS = 8;
    private static final String LOG_TAG = GameActivity.class.getSimpleName();
    private static final int COLOR_ENEMY = 2;
    private static final int COLOR_PLAYER = 1;

    private int countOfPlayers = 0;
    private String level = "easy";
    private int currentPlayer = 1;

    private static int numberMove; //номер хода

    private Board mBoard;
    private boolean isChooseCheck = false;
    private Cell mChooseCell;

    private int mDeepScoreEnemy = 0;
    private int mDeepRecur = 0;
    private List<Move> moves = new ArrayList<>();
    private PairCell mDeepBestPairCell = new PairCell();
    private Move mDeepBestMove = new Move();

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(LOG_TAG, v.getId() + "");
        }
    };

    //Первый игрок играет за черные
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();

        countOfPlayers = intent.getIntExtra("count_players", 0);
        level = intent.getStringExtra("level");

        Log.i(LOG_TAG, "count = " + countOfPlayers + ", level = " + level);

        mBoard = new Board();
        mChooseCell = new Cell();
        renderBoard();

        numberMove = 0;
        Toast toast;
        if (countOfPlayers == 1) {
            toast = Toast.makeText(getApplicationContext(),
                    "Ваш ход",
                    Toast.LENGTH_SHORT);
        } else {
            toast = Toast.makeText(getApplicationContext(),
                    "Ход игрока №1",
                    Toast.LENGTH_SHORT);
        }
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
                    if (mBoard.getCell(i, j) == COLOR_ENEMY) {
                        imageView.setImageResource(R.drawable.white);
                    } else if (mBoard.getCell(i, j) == COLOR_PLAYER) {
                        imageView.setImageResource(R.drawable.black);
                    } else {
                        imageView.setImageResource(R.drawable.black_cell);
                    }
                    final int finalI = i;
                    final int finalJ = j;

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (countOfPlayers == 1) {
                                if (mBoard.getCell(finalI, finalJ) == COLOR_PLAYER)//если это шашка игрока №1
                                {
                                    if (isChooseCheck) { //если она уже была выбрана и игрок хочет изменить выбор своей шашки
                                        mChooseCell.setX(finalI);
                                        mChooseCell.setY(finalJ);
                                        renderBoard();
                                    }
                                    imageView.setBackgroundColor(Color.GREEN);
                                    isChooseCheck = true;
                                    mChooseCell.setX(finalI);
                                    mChooseCell.setY(finalJ);
                                } else {
                                    if (isChooseCheck) { //если игрок уже выбрал шашку и хочет сделать ход
                                        if (mBoard.moveChecker(mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ, COLOR_PLAYER, getApplicationContext())) {
                                            Log.i(LOG_TAG, "Move to: " + mChooseCell.getX() + ", " + mChooseCell.getY() + "-> " + Integer.toString(finalI) + "," + Integer.toString(finalJ));
                                            renderBoard();

                                            //Ход белых
                                            PairCell newCell = movementEnemy();

                                            mBoard.moveChecker(newCell.getmBegCell().getX(), newCell.getmBegCell().getY(), newCell.getmEndCell().getX(), newCell.getmEndCell().getY(), COLOR_ENEMY, getApplicationContext());
                                            numberMove++;
                                            Log.i(LOG_TAG, "Number move: " + numberMove);
                                            renderBoard();
                                            // mBoard.showBoard();

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
                                // Log.i(LOG_TAG, "Move to: " + mChooseCell.getX() + ", " + mChooseCell.getY()  + "-> " + Integer.toString(finalI) + "," + Integer.toString(finalJ));
                            }
                            //else if (currentPlayer == 1) {
                            else if (mBoard.getCell(finalI, finalJ) == currentPlayer)//если это шашка текущего игрока
                            {
                                if (isChooseCheck) { //если она уже была выбрана и игрок хочет изменить выбор своей шашки
                                    mChooseCell.setX(finalI);
                                    mChooseCell.setY(finalJ);
                                    renderBoard();
                                }
                                imageView.setBackgroundColor(Color.GREEN);
                                isChooseCheck = true;
                                mChooseCell.setX(finalI);
                                mChooseCell.setY(finalJ);
                            } else {
                                if (isChooseCheck) { //если игрок уже выбрал шашку и хочет сделать ход
                                    if (mBoard.moveChecker(mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ, currentPlayer, getApplicationContext())) {
                                        Log.i(LOG_TAG, "Player:" + currentPlayer + " Move to: " + mChooseCell.getX() + ", " + mChooseCell.getY() + "-> " + Integer.toString(finalI) + "," + Integer.toString(finalJ));
                                        renderBoard();
                                        if (currentPlayer == 1) {
                                            currentPlayer = 2;
                                        } else {
                                            currentPlayer = 1;
                                        }
                                        numberMove++;
                                        //Ход белых
                                        //   PairCell newCell = movementEnemy();

                                        //   mBoard.moveChecker(newCell.getmBegCell().getX(), newCell.getmBegCell().getY(), newCell.getmEndCell().getX(), newCell.getmEndCell().getY(), COLOR_ENEMY, getApplicationContext());
                                        //   numberMove++;
                                        //   Log.i(LOG_TAG, "Number move: " + numberMove);
                                        //   renderBoard();
                                        // mBoard.showBoard();
                                        Toast toast;
                                        if (currentPlayer == 2) {
                                            toast = Toast.makeText(getApplicationContext(),
                                                    "Ход игрока №2",
                                                    Toast.LENGTH_SHORT);
                                        } else {
                                            toast = Toast.makeText(getApplicationContext(),
                                                    "Ход игрока №1",
                                                    Toast.LENGTH_SHORT);
                                        }
                                        toast.show();
                                    }
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            "Сначала выберите шашку, которой хотите совершить ход",
                                            Toast.LENGTH_SHORT);
                                    toast.show();
                                }


                            }
                        }
                    });
                }
                if (i == mChooseCell.getX() && j == mChooseCell.getY()) {
                    imageView.setBackgroundColor(Color.GREEN);
                }
                tableRow.addView(imageView, j);
            }
            tableLayout.addView(tableRow, i);
        }
    }

    private void updateScore() {
        TextView scoreView = (TextView) findViewById(R.id.score);
        scoreView.setText(mBoard.getScore().getScoreBlack() + ":" + mBoard.getScore().getScoreWhite());
    }

    private boolean isBlackCell(int i, int j) {
        return ((i % 2 == 0 && j % 2 != 0) || (i % 2 != 0 && j % 2 == 0));
    }

    //Ход противника
    private PairCell movementEnemy() {

        Log.i(LOG_TAG, "movement white");

        return calculateBestMove();
    }

    private boolean winning(int colorPlayer) {
        //если цвет - белый
        if (colorPlayer == COLOR_ENEMY) {
            //если все шашки противника захвачены
            if (mBoard.getScore().getScoreWhite() == 12) {
                return true;
            }
        }
        //если цвет - черный
        else if (colorPlayer == COLOR_PLAYER) {
            if (mBoard.getScore().getScoreBlack() == 12) {
                return true;
            }
        }
        return false;
    }

    //Получаем доступные клетки, куда мы можем пойти
    private List<Cell> getAvailCellsInBoard(Board board, int begI, int begJ, int colorPlayer) {
        // Log.i(LOG_TAG, "Reboard in alg getAvailCells:");
        //  board.showBoard();
        List<Cell> availCells = new ArrayList<>();
        //   Log.i(LOG_TAG, "beg:" + begI + "," + begJ + "; colorPlayer: " + colorPlayer);
        if (board.getCell(begI, begJ) == colorPlayer) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    if (board.getCell(i, j) == 0) {
                        if (board.isValidMove(begI, begJ, i, j, colorPlayer)) {
                            availCells.add(new Cell(i, j));
                        }
                    }
                }
            }
        }
        // Log.i(LOG_TAG, "AvailCells: " + availCells.toString());
        return availCells;
    }

    //Алгоритм минимакс для вычисления следующего хода компьютера
    private Move algMiniMax(Board reboard, int begI, int begJ, int colorPlayer) {
        Log.i(LOG_TAG, "alg min max for " + begI + "," + begJ);
        mDeepRecur++;

        List<Cell> availCells = getAvailCellsInBoard(reboard, begI, begJ, colorPlayer);
        Log.i(LOG_TAG, "Deep Recur - " + mDeepRecur);

        //если выигрывает игрок
        if (winning(COLOR_PLAYER)) {
            mDeepScoreEnemy = -10;
            return new Move(new Cell(begI, begJ), null, mDeepScoreEnemy);
            //если выигрывает компьютер
        } else if (winning(COLOR_ENEMY)) {
            mDeepScoreEnemy = 10;
            return new Move(new Cell(begI, begJ), null, mDeepScoreEnemy);

            //если нет свободных ходов - ничья
        } else if (availCells.isEmpty()) {
            mDeepScoreEnemy = 0;
            return new Move(new Cell(begI, begJ), null, mDeepScoreEnemy);
        }


        for (int i = 0; i < availCells.size(); i++) {
            Move move = new Move();
            move.setmEndCell(availCells.get(i));
            //переставляем на "новой доске" шашку
            if (!reboard.moveChecker(begI, begJ, availCells.get(i).getX(), availCells.get(i).getY(), COLOR_ENEMY, this)) {
                availCells.remove(i);
            } else {
                reboard.showBoard();

                //Для просчитывания предполагаемого хода игрока нужно и для него проверять все доступные ходы и для всех строить аналогичную стратегию.
                //После получения хода игрока, переставляем его на воображаемой доске и ищем следующий свой ход

                if (colorPlayer == COLOR_ENEMY) {
                    //рекурсивно вызываем у противника, т.е. если сейчас ходил компьютер - теперь ходит игрок

                    //если это первый заход в рекурсию, то сразу сохраняем начальный ход
                    //if(mDeepRecur == 1){
                    //    mDeepBestPairCell = new PairCell(new Cell(begI, begJ), availCells.get(i));
                    //}
                    Move newMove = algMiniMax(reboard, availCells.get(i).getX(), availCells.get(i).getY(), COLOR_PLAYER);

                    Log.i(LOG_TAG, "Move enemy - " + newMove.toString());

                    move.setmScoreEnemy(newMove.getmScoreEnemy());
                    move.setmBegCell(new Cell(begI, begJ));
                    move.setmEndCell(new Cell(availCells.get(i).getX(), availCells.get(i).getY()));

                    if (mDeepRecur == 1) {
                        mDeepBestMove = move;
                    }
                    //иначе, если до этого ходил игрок - сейчас ходит компьютер
                } else {
                    Move newMove = algMiniMax(reboard, begI, begJ, COLOR_ENEMY);

                    Log.i(LOG_TAG, "Move player - " + newMove.toString());

                    move.setmScoreEnemy(newMove.getmScoreEnemy());
                    move.setmBegCell(new Cell(begI, begJ));
                    move.setmEndCell(new Cell(availCells.get(i).getX(), availCells.get(i).getY()));
                }
                moves.add(move);
            }
        }

        Move bestMove = new Move();
        if (colorPlayer == COLOR_ENEMY) {
            int bestScore = -10000;
            for (int i = 0; i < moves.size(); i++) {
                if (moves.get(i).getmScoreEnemy() > bestScore) {
                    bestScore = moves.get(i).getmScoreEnemy();
                    bestMove = moves.get(i);
                }
            }
        } else {
            int bestScore = 10000;
            for (int i = 0; i < moves.size(); i++) {
                if (moves.get(i).getmScoreEnemy() < bestScore) {
                    bestScore = moves.get(i).getmScoreEnemy();
                    bestMove = moves.get(i);
                }
            }
        }
        Log.i(LOG_TAG, "Best move enemy - " + bestMove);

        return bestMove;
    }

    private PairCell calculateBestMove() {
        if (level.equals("easy") && countOfPlayers == 1) {
            List<PairCell> pairCells = new ArrayList<>();
            //Уровень изи играется "на рандоме"
            //Сначала мы выбираем случайным образом одну шашку, которая сейчас может сделать ход
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    //если это шашка противника
                    if (mBoard.getCell(i, j) == COLOR_ENEMY) {
                        //смотрим доступные ходы для этой шашки
                        List<Cell> cells = getAvailCellsInBoard(mBoard, i, j, COLOR_ENEMY);
                        if (cells.size() > 0) {
                            //Добавляем все доступные ходы в массив ходов
                            for (int k = 0; k < cells.size(); k++) {
                                pairCells.add(new PairCell(new Cell(i, j), cells.get(k)));
                            }
                        }
                    }
                }
            }
            Random random = new Random();
            Log.i(LOG_TAG, pairCells.toString());
            PairCell pairCell = pairCells.get(random.nextInt(pairCells.size() - 1));
            Log.i(LOG_TAG, pairCell.toString());
            return pairCell;
        } else {
            Move bestMove = null;
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    //выбераем все шашки врага и проверяем для каждой шашки best move
                    if (mBoard.getCell(i, j) == COLOR_ENEMY) {
                        Log.i(LOG_TAG, "For check " + i + ", " + j + ":");
                        mDeepRecur = 0;
                        Move move = algMiniMax(mBoard, i, j, COLOR_ENEMY);
                        if (bestMove == null) {
                            bestMove = move;
                            // begCellBestMove = new Cell(i,j);
                        } else if (bestMove.getmScoreEnemy() > bestMove.getmScoreEnemy()) {
                            bestMove = move;
                            // begCellBestMove = new Cell(i,j);
                        }
                    }
                }
            }
            Log.i(LOG_TAG, mDeepBestMove.toString());
            return new PairCell(mDeepBestMove.getmBegCell(), mDeepBestMove.getmEndCell());
        }
    }
}
