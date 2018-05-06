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
import java.util.Timer;
import java.util.TimerTask;

import game.chernousovaya.checkers.R;
import game.chernousovaya.checkers.model.Board;
import game.chernousovaya.checkers.model.Cell;
import game.chernousovaya.checkers.model.Move;
import game.chernousovaya.checkers.model.PairCell;
import game.chernousovaya.checkers.model.Tree;

public class GameActivity extends AppCompatActivity {

    private static final String LOG_TAG = GameActivity.class.getSimpleName();
    private static final int ROWS = 8;
    private static final int COLUMNS = 8;
    private static final int COLOR_ENEMY = 2;
    private static final int COLOR_PLAYER = 1;
    private static final int COLOR_ENEMY_KING = 4;
    private static final int COLOR_PLAYER_KING = 5;
    public static boolean noMoves = false;
    private static final int maxDeep = 4; //максимальная глубина построения дерева

    private int countOfPlayers = 0;
    private String level = "easy";
    private int currentPlayer = 1;

    private static int numberMove; //номер хода

    private Board mBoard;
    private Board tempBoard;
    private boolean isChooseCheck = false;
    private Cell mChooseCell;
    private PairCell currentMove = new PairCell();

    private int mDeepScoreEnemy = 0;
    private int mDeepRecur = 0;
    private List<Move> moves = new ArrayList<>();
    private Move mDeepBestMove = new Move();
    private PairCell mDeepBestPairCell = new PairCell();

    private boolean isPlayersMoved = false;
    private boolean isTimerStoped = false;
    private TextView messageView;
    private boolean endGame = false;
    private boolean isResumeMove = false; //признак того, что это продолжение хода, а не новый хлд, т.е. в продолжении игрок должен бить дальше

    private List<PairCell> mandatoryMoves = new ArrayList<>(); //обязательные ходы для одной шашки
    // private ArrayList<Board> boardArrayList = new ArrayList<>(); //список для сохранения состояний доски

    private Tree tree = new Tree();

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
        TextView messageView = (TextView) findViewById(R.id.message);

        if (countOfPlayers == 1) {
            messageView.setText(" Ваш ход (Вы играете за черные)");
        } else {
            messageView.setText(" Первым ходит игрок №1 (Черные)");
        }
    }

    private void renderBoard() {

        if (!endGame) {
            updateScore();
            mBoard.showBoard();
            Log.i("EVAL " + LOG_TAG, mBoard.evaluationFunction() + "");
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
                        } else if (mBoard.getCell(i, j) == COLOR_PLAYER_KING) {
                            imageView.setImageResource(R.drawable.black_king);
                        } else if (mBoard.getCell(i, j) == COLOR_ENEMY_KING) {
                            imageView.setImageResource(R.drawable.white_king);
                        } else {
                            imageView.setImageResource(R.drawable.black_cell);
                        }
                        final int finalI = i;
                        final int finalJ = j;

                        if (!mBoard.isEndOfGame(getApplicationContext(), countOfPlayers) && !noMoves) {
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (countOfPlayers == 1) {
                                        if (mBoard.getCell(finalI, finalJ) == COLOR_PLAYER || mBoard.getCell(finalI, finalJ) == COLOR_PLAYER_KING)//если это шашка игрока №1
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
                                                mandatoryMoves = new ArrayList<>();

                                                //надо сделать проверку на то, что если другие шашки могут бить, то выбирать только из них
                                                List<PairCell> allMandatoryMoves = getAllMandatoryMoves(mBoard, mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()));

                                                //если данных ход содержится в обязательных ходах или обязательных ходов для битья нет, то всё хорошо
                                                if (allMandatoryMoves.contains(new PairCell(mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ)) || allMandatoryMoves.isEmpty()) {
                                                    getAvailCellsInBoard(mBoard, mChooseCell.getX(), mChooseCell.getY(), mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()));

                                                    int withCapture = mBoard.moveChecker(mandatoryMoves, mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ,
                                                            mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()), getApplicationContext());

                                                    currentMove = new PairCell(mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ);
                                                    //если мы ходим успешно
                                                    if (withCapture != 0) {
                                                        renderBoard();
                                                        //если мы сделали ход со взятием и можем продолжить ход
                                                        if (withCapture == 2 && thereAreAnyMoves(mBoard, finalI, finalJ, mBoard.getCell(finalI, finalJ))) {
                                                            isResumeMove = true;
                                                            mChooseCell.setX(finalI);
                                                            mChooseCell.setY(finalJ);
                                                            renderBoard();
                                                        } else {
                                                            isResumeMove = false;
                                                        }

                                                        if (!isResumeMove) {
                                                            isPlayersMoved = true;
                                                            renderBoard();
                                                            moveEnemy();
                                                        }
                                                    }
                                                } else {
                                                    Toast toast = Toast.makeText(getApplicationContext(),
                                                            "Есть шашки, которыми можно бить, выберите другую шашку",
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
                                    }
                                    //если это шашка текущего игрока
                                    else if ((currentPlayer == 1 && (mBoard.getCell(finalI, finalJ) == COLOR_PLAYER || mBoard.getCell(finalI, finalJ) == COLOR_PLAYER_KING))
                                            || (currentPlayer == 2 && (mBoard.getCell(finalI, finalJ) == COLOR_ENEMY || mBoard.getCell(finalI, finalJ) == COLOR_ENEMY_KING))) {
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

                                            mandatoryMoves.clear();
                                            //если это продолжение хода, то он должен бить все шашки
                                            List<PairCell> allMandatoryMoves = getAllMandatoryMoves(mBoard, mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()));
                                            if (allMandatoryMoves.contains(new PairCell(mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ)) || allMandatoryMoves.isEmpty()) {

                                                getAvailCellsInBoard(mBoard, mChooseCell.getX(), mChooseCell.getY(), mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()));

                                                int withCapture = mBoard.moveChecker(mandatoryMoves, mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ,
                                                        mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()), getApplicationContext());
                                                //если мы ходим успешно
                                                if (withCapture != 0) {
                                                    renderBoard();
                                                    Log.i(LOG_TAG, finalI + " " + finalJ);
                                                    //если мы сделали ход со взятием и можем продолжить ход
                                                    if (withCapture == 2 && thereAreAnyMoves(mBoard, finalI, finalJ, mBoard.getCell(finalI, finalJ))) {
                                                        isResumeMove = true;
                                                        mChooseCell.setX(finalI);
                                                        mChooseCell.setY(finalJ);
                                                        renderBoard();
                                                    } else {
                                                        isResumeMove = false;
                                                    }

                                                    if (!isResumeMove) {
                                                        if (currentPlayer == 1) {
                                                            currentPlayer = 2;
                                                            messageView = (TextView) findViewById(R.id.message);
                                                            if (endGame)
                                                                messageView.setText("");
                                                            else
                                                                messageView.setText("Ход игрока №2 (Белые)");
                                                        } else {
                                                            currentPlayer = 1;
                                                            messageView = (TextView) findViewById(R.id.message);
                                                            if (endGame)
                                                                messageView.setText("");
                                                            else
                                                                messageView.setText("Ход игрока №1 (Черные)");
                                                        }
                                                        numberMove++;
                                                    }
                                                }
                                            } else {
                                                Toast toast = Toast.makeText(getApplicationContext(),
                                                        "Есть шашки, которыми можно бить, выберите другую шашку",
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
                                }
                            });
                        } else {
                            endGame = true;
                            showResults();
                        }
                    }
                    if (i == mChooseCell.getX() && j == mChooseCell.getY()) {
                        imageView.setBackgroundColor(Color.GREEN);
                    }
                    tableRow.addView(imageView, j);
                }
                tableLayout.addView(tableRow, i);
            }
        }
    }

    //private boolean
    private void showResults() {
        Log.i(LOG_TAG, "show res");
        messageView = (TextView) findViewById(R.id.message);
        if (mBoard.getScore().getmScoreWhite() == 12) {
            if (countOfPlayers == 1)
                messageView.setText("К сожалению, Вы проиграли");
            else
                messageView.setText("Победа за игроком №2! Поздравляем!");
        } else if (mBoard.getScore().getmScoreBlack() == 12) {
            if (countOfPlayers == 1)
                messageView.setText("Поздравляем! Вы победили!");
            else
                messageView.setText("Победа за игроком №1! Поздравляем!");
        } else if (noMoves) {
            if (mBoard.getScore().getmScoreBlack() > mBoard.getScore().getmScoreWhite()) {
                if (countOfPlayers == 1)
                    messageView.setText("Поздравляем! Вы победили!");
                else
                    messageView.setText("Победа за игроком №1! Поздравляем!");
            } else {
                if (countOfPlayers == 1)
                    messageView.setText("К сожалению, Вы проиграли");
                else
                    messageView.setText("Победа за игроком №2! Поздравляем!");
            }
        }
    }

    private void moveEnemy() {
        messageView = (TextView) findViewById(R.id.message);
        messageView.setText("Ход противника");
        isTimerStoped = false;
        if (isPlayersMoved) {
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isPlayersMoved) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                isTimerStoped = true;
                                if (isTimerStoped) {
                                    PairCell newCell = calculateBestMove();
                                    getAvailCellsInBoard(mBoard, newCell.getmBegCell().getX(), newCell.getmBegCell().getY(), 2);
                                    mBoard.moveChecker(mandatoryMoves, newCell.getmBegCell().getX(), newCell.getmBegCell().getY(), newCell.getmEndCell().getX(), newCell.getmEndCell().getY(), mBoard.getCell(newCell.getmBegCell().getX(), newCell.getmBegCell().getY()), getApplicationContext());
                                    numberMove++;
                                    Log.i(LOG_TAG, "Number move: " + numberMove);
                                    messageView = (TextView) findViewById(R.id.message);
                                    if (endGame)
                                        messageView.setText("");
                                    else
                                        messageView.setText("Ваш ход");
                                    renderBoard();
                                    isPlayersMoved = false;
                                    isTimerStoped = false;
                                }
                            }
                        }
                    });
                }
            }, 0, 1000);
        }
    }

    private void updateScore() {
        Log.i(LOG_TAG, "update score");
        TextView scoreView = (TextView) findViewById(R.id.score);
        scoreView.setText(mBoard.getScore().getmScoreBlack() + ":" + mBoard.getScore().getmScoreWhite());
    }

    private boolean isBlackCell(int i, int j) {
        return ((i % 2 == 0 && j % 2 != 0) || (i % 2 != 0 && j % 2 == 0));
    }

    private boolean winning(int colorPlayer) {
        //если цвет - белый
        if (colorPlayer == COLOR_ENEMY) {
            //если все шашки противника захвачены
            if (mBoard.getScore().getmScoreWhite() == 12) {
                return true;
            }
        }
        //если цвет - черный
        else if (colorPlayer == COLOR_PLAYER) {
            if (mBoard.getScore().getmScoreBlack() == 12) {
                return true;
            }
        }
        return false;
    }

    //Получаить доступные клетки, куда мы можем пойти
    private List<Cell> getAvailCellsInBoard(Board board, int begI, int begJ, int colorPlayer) {

        // mandatoryMoves = new ArrayList<>();
        List<Cell> availCells = new ArrayList<>();
        if (board.getCell(begI, begJ) == colorPlayer) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    if (board.getCell(i, j) == 0) {
                        int flag = board.isValidMove(begI, begJ, i, j, colorPlayer);
                        if (flag == 1) {
                            availCells.add(new Cell(i, j));
                        } else if (flag == 2) {
                            Log.i(LOG_TAG, "!!! " + availCells.toString());
                            //если нашел первый ход со взятием, то добалвяем его в обязательные ходы
                            //availCells.clear();
                            availCells.add(new Cell(i, j));

                            mandatoryMoves.add(new PairCell(new Cell(begI, begJ), new Cell(i, j)));

                            // Log.i(LOG_TAG, "!!! " + availCells.toString());
                            return availCells;
                        }
                    }
                }
            }
        }
        return availCells;
    }

    //Получить все ходы, в которых мы бьем
    private List<PairCell> getAllMandatoryMoves(Board board, int colorPlayer) {
        List<PairCell> allMandatoryMoves = new ArrayList<>();
        //Идем по всем шашкам выбранного игрока
        for (int k = 0; k < ROWS; k++) {
            for (int u = 0; u < COLUMNS; u++) {
                if (board.getCell(k, u) == colorPlayer) {
                    //проверяем все свободные клетки на то, можно ли пойти и со взятием или нет
                    for (int i = 0; i < ROWS; i++) {
                        for (int j = 0; j < COLUMNS; j++) {
                            if (board.getCell(i, j) == 0) {
                                int flag = board.isValidMove(k, u, i, j, colorPlayer);
                                if (flag == 2) {
                                    //если нашел ход со взятием, то добалвяем его в обязательные ходы
                                    allMandatoryMoves.add(new PairCell(k, u, i, j));
                                }
                            }
                        }
                    }
                }
            }
        }
        Log.i(LOG_TAG, allMandatoryMoves.toString());
        return allMandatoryMoves;
    }

    //Проверка, есть ли еще ходы для битья у текущего игрока
    private boolean thereAreAnyMoves(Board board, int begI, int begJ, int colorPlayer) { //если да, то ход текущего игрока продолжается, нет - ход переходит
        ArrayList<PairCell> mandatoryMoves = new ArrayList<>();
        if (board.getCell(begI, begJ) == colorPlayer) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    if (board.getCell(i, j) == 0) {
                        int flag = board.isValidMove(begI, begJ, i, j, board.getCell(begI, begJ));
                        if (flag == 2) {
                            mandatoryMoves.add(new PairCell(new Cell(begI, begJ), new Cell(i, j)));

                        }
                    }
                }
            }
        }
        if (mandatoryMoves.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    //Алгоритм минимакс для вычисления следующего хода компьютера
    private Move algMiniMax(Board reboard, int begI, int begJ, int colorPlayer, int level) {
        Log.i(LOG_TAG, "alg min max for " + begI + "," + begJ);
        mDeepRecur++;

        if (level == maxDeep || winning(1) || winning(2)) {
            return new Move(new Cell(begI, begJ), null, reboard.evaluationFunction());
        }

        List<Cell> availCells = getAvailCellsInBoard(reboard, begI, begJ, colorPlayer);
        Log.i(LOG_TAG, "Deep Recur - " + mDeepRecur);
        Move bestMove = new Move();
        for (int i = 0; i < availCells.size(); i++) {
            Move move = new Move();
            getAvailCellsInBoard(reboard, begI, begJ, colorPlayer);
            //переставляем на "новой доске" шашку
            int withCapture = reboard.moveChecker(mandatoryMoves, begI, begJ, availCells.get(i).getX(), availCells.get(i).getY(), COLOR_ENEMY, this);
            if (withCapture == 0) {
                availCells.remove(i);
            } else {
                reboard.showBoard();
                int g;
                if (colorPlayer == 2) {
                    g = -100;
                    Move newMove = algMiniMax(reboard, availCells.get(i).getX(), availCells.get(i).getY(), colorPlayer, level + 1);

                    Log.i(LOG_TAG, "Move 2 - " + newMove.toString());

                    move.setEvalFunc(newMove.getEvalFunc());
                    move.setmBegCell(new Cell(begI, begJ));
                    move.setmEndCell(new Cell(availCells.get(i).getX(), availCells.get(i).getY()));

                    if (move.getEvalFunc() > g) {
                        bestMove = move;
                    }
                } else {
                    g = +100;
                    Move newMove = algMiniMax(reboard, availCells.get(i).getX(), availCells.get(i).getY(), colorPlayer, level + 1);

                    Log.i(LOG_TAG, "Move 1 - " + newMove.toString());

                    move.setEvalFunc(newMove.getEvalFunc());
                    move.setmBegCell(new Cell(begI, begJ));
                    move.setmEndCell(new Cell(availCells.get(i).getX(), availCells.get(i).getY()));
                    if (move.getEvalFunc() < g) {
                        bestMove = move;
                    }
                }
            }
        }
        Log.i(LOG_TAG, "Best move enemy - " + bestMove);

        return bestMove;
    }

    //Определить, принадлежит ли уровень максу
    private boolean isMaxLevel(int level) {
        //Все четные номера - принадлежат мину, все нечетные - максу
        if (level % 2 == 0) {
            return false;
        }
        return true;
    }

    //Move - ход и оценочная функция данного хода
    private int miniMax(Board board, int level) {
        if (level == maxDeep || winning(1) || winning(2)) {
            return board.evaluationFunction();
        }
        int colorPlayer;
        if (isMaxLevel(level)) {
            colorPlayer = 2;
        } else {
            colorPlayer = 1;
        }

        int g;
        //Получаем все доступные ходы
        ArrayList<PairCell> availMoves = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                List<Cell> arrayList = getAvailCellsInBoard(board, i, j, colorPlayer);
                for (int k = 0; k < arrayList.size(); k++) {
                    availMoves.add(new PairCell(new Cell(i, j), arrayList.get(k)));
                }
            }
        }
        int i = 0;
        if (colorPlayer == 2) {
            g = -100;
            board.moveChecker(mandatoryMoves, availMoves.get(i).getmBegCell().getX(), availMoves.get(i).getmBegCell().getY(),
                    availMoves.get(i).getmEndCell().getX(), availMoves.get(i).getmEndCell().getY(), colorPlayer, this);
            while (i < availMoves.size()) {
                g = Math.max(g, miniMax(board, level + 1));
                i++;
            }
        } else {
            g = +100;
            board.moveChecker(mandatoryMoves, availMoves.get(i).getmBegCell().getX(), availMoves.get(i).getmBegCell().getY(),
                    availMoves.get(i).getmEndCell().getX(), availMoves.get(i).getmEndCell().getY(), colorPlayer, this);
            while (i < availMoves.size()) {
                g = Math.max(g, miniMax(board, level + 1));
                i++;
            }
        }
        return g;
    }
    // level - уровень в дереве игры
    // pos – текущее состояние (board)
    //int miniMax(pos,int level)
    //{
    //    // если глубина равна листу,
    //    // или позиция соответсвует выигрышу
    //    // или позиция соответсвует проигрышу
    //    if (level = leaf || IsWin() || IsLoss())
    //          return eval(pos);

    //    // ищем существующую позицию в предыдущих состояниях
    //    // если не находим то сохраняем её в списке позиций
    //    if (FindPrevPos(pos)) return eval(pos);
    //    else Insert_Pos(pos);

    //    if (level == max) // n принадлежит уровню max
    //    {
    //        g := -∞;
    //        pos_cur = FirstChild(pos);
    //        while (с ≠ λ )
    //        {
    //            g = max(g, MiniMax(cur_pos, level + 1));
    //            с = NextBrother(c);
    //        }
    //    }
    //    else // n принадлежит уровню min
    //    {
    //        g := +∞;
    //        pos_cur = FirstChild(pos);
    //        while (с ≠ λ )
    //        {
    //            g = min(g, MiniMax(cur_pos, level + 1));
    //            с = NextBrother(c);
    //        }
    //    }
    //    // удаляем текущую позицию из списка позиций
    //    Delete_Pos(pos);
    //    return g;
    //}
    private boolean isBadMove(int i, int j) {
        if (i < 7) {
            if ((j < 7 && mBoard.getArr()[i + 1][j + 1] == COLOR_PLAYER) || (j > 0 && mBoard.getArr()[i + 1][j - 1] == COLOR_PLAYER))
                return true;
        }
        return false;
    }

    private PairCell calculateBestMove() {
        mandatoryMoves.clear();
        if (level.equals("easy") && countOfPlayers == 1) {
            List<PairCell> pairCells = new ArrayList<>();

            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    //если это шашка противника
                    if (mBoard.getCell(i, j) == COLOR_ENEMY || mBoard.getCell(i, j) == COLOR_ENEMY_KING) {
                        //смотрим доступные ходы для этой шашки
                        List<Cell> cells = getAvailCellsInBoard(mBoard, i, j, mBoard.getCell(i, j));
                        if (cells.size() > 0) {
                            //Добавляем все доступные ходы в массив ходов
                            for (int k = 0; k < cells.size(); k++) {
                                pairCells.add(new PairCell(new Cell(i, j), cells.get(k)));
                            }
                        }
                    }
                }
            }
            Log.i(LOG_TAG, "mandatory moves " + mandatoryMoves.toString());
            Log.i(LOG_TAG, "pairCells " + pairCells.toString());
            if (mandatoryMoves.size() > 0) {
                pairCells.clear();
                pairCells.addAll(mandatoryMoves);
            }
            Random random = new Random();
            Log.i(LOG_TAG, pairCells.toString());
            PairCell pairCell;

            int countOfBadMoves = 0;

            //если не осталось ходов
            if (pairCells.isEmpty()) {
                noMoves = true;
                showResults();
            } else {
                if (pairCells.size() > 1) {
                    pairCell = pairCells.get(random.nextInt(pairCells.size() - 1));
                    for (int i = 0; i < pairCells.size(); i++) {
                        if (isBadMove(pairCells.get(i).getmEndCell().getX(), pairCells.get(i).getmEndCell().getY())) {
                            countOfBadMoves++;
                        }
                    }
                    int i = 0;
                    if (countOfBadMoves < pairCells.size()) {
                        while (isBadMove(pairCell.getmEndCell().getX(), pairCell.getmEndCell().getY())) {
                            Log.i(LOG_TAG, pairCell.getmEndCell().getX() + "," + pairCell.getmEndCell().getY() + " is bad move");
                            // pairCell = pairCells.get(random.nextInt(pairCells.size() - 1));
                            pairCell = pairCells.get(i);
                            i++;
                        }
                    }

                } else {
                    pairCell = pairCells.get(0);
                }
                mandatoryMoves.clear();
                Log.i(LOG_TAG, pairCell.toString());
                return pairCell;
            }
        }
        if (level.equals("hard")) {
            int temp = 0;
            tree = new Tree();
            PairCell bestMove = new PairCell();
            Board board = new Board(mBoard);

            //Устанавливаем корнем дерева первый ход игрока с idxParent -1
            tree.addChildren(currentMove, board, -1);

            int idxParent = 0;
            int colorCurrentPlayer;
            //Цикл на максимальную глубину
            for (int l = 1; l < 4; l++) {

                if (isMaxLevel(l)) {
                    colorCurrentPlayer = COLOR_ENEMY;
                    temp = -100;
                } else {
                    colorCurrentPlayer = COLOR_PLAYER;
                    temp = 100;
                }

                ArrayList<PairCell> availMoves = new ArrayList<>();
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLUMNS; j++) {
                        //Если это уровень MAX, то сейчас ходят белые шашки

                        List<Cell> arrayList = getAvailCellsInBoard(board, i, j, colorCurrentPlayer);
                        for (int k = 0; k < arrayList.size(); k++) {
                            availMoves.add(new PairCell(new Cell(i, j), arrayList.get(k)));
                        }
                    }
                }
                //если есть доступные ходы
                if (!availMoves.isEmpty()) {
                    //Добавляем все следующие возможные ходы в дерево решений с родителем первым ходом
                    for (int j = 0; j < availMoves.size(); j++) {
                        //На воображаемой доске переставляем шашку
                        mandatoryMoves = new ArrayList<>();

                        List<PairCell> allMandatoryMoves = getAllMandatoryMoves(board, colorCurrentPlayer);

                        if (allMandatoryMoves.isEmpty() || allMandatoryMoves.contains(new PairCell(availMoves.get(j).getmBegCell(), availMoves.get(j).getmEndCell()))) {

                            Board newBoard = new Board(board);
                            getAvailCellsInBoard(board, availMoves.get(j).getmBegCell().getX(), availMoves.get(j).getmBegCell().getY(), colorCurrentPlayer);
                            newBoard.moveCheckerWithoutToast(mandatoryMoves, availMoves.get(j).getmBegCell().getX(), availMoves.get(j).getmBegCell().getY(),
                                    availMoves.get(j).getmEndCell().getX(), availMoves.get(j).getmEndCell().getY(), colorCurrentPlayer, this);
                            tree.addChildren(availMoves.get(j), newBoard, idxParent);
                        }

                        if (isMaxLevel(l)) {
                            for (Tree.Node node : tree.getChildrens(idxParent)) {
                                if (node.getBoard().evaluationFunction() > temp) {
                                    bestMove = node.getMove();
                                    temp = node.getBoard().evaluationFunction();
                                }
                            }
                        } else {
                            for (Tree.Node node : tree.getChildrens(idxParent)) {
                                if (node.getBoard().evaluationFunction() < temp) {
                                    bestMove = node.getMove();
                                    temp = node.getBoard().evaluationFunction();
                                }
                            }
                        }
                    }
                }

                for (Tree.Node node : tree.getChildrens(idxParent)) {


                }
            }
            idxParent++;
            return bestMove;
        }
        return new PairCell(0, 0, 0, 0);
    }

}
