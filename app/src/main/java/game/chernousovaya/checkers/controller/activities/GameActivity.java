package game.chernousovaya.checkers.controller.activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
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
import game.chernousovaya.checkers.model.Score;
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
    private boolean isShowRes = false;
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
                                                            if (!mBoard.isEndOfGame(getApplicationContext(), countOfPlayers)) {
                                                                moveEnemy();
                                                            }
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
                                                    //  Log.i(LOG_TAG, finalI + " " + finalJ);
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
                            //if (!isShowRes)
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mBoard.setScore(new Score());
    }

    //private boolean
    private void showResults() {

        Log.i(LOG_TAG, "show res");
        messageView = (TextView) findViewById(R.id.message);
        if (!isShowRes) {
            if (mBoard.getScore().getmScoreWhite() == 12) {
                if (countOfPlayers == 1) {
                    Toast toast = Toast.makeText(this,
                            "К сожалению, Вы проиграли.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    messageView.setText("К сожалению, Вы проиграли");
                } else {
                    Toast toast = Toast.makeText(this,
                            "К сожалению, Вы проиграли.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    messageView.setText("Победа за игроком №2! Поздравляем!");
                }
            } else if (mBoard.getScore().getmScoreBlack() == 12) {
                if (countOfPlayers == 1) {
                    (Toast.makeText(this,
                            "Поздравляем! Вы победили!",
                            Toast.LENGTH_SHORT)).show();
                    messageView.setText("Поздравляем! Вы победили!");
                } else {
                    (Toast.makeText(this,
                            "Победа за игроком №1! Поздравляем!",
                            Toast.LENGTH_SHORT)).show();
                    messageView.setText("Победа за игроком №1! Поздравляем!");
                }
            } else if (noMoves) {
                if (mBoard.getScore().getmScoreBlack() > mBoard.getScore().getmScoreWhite()) {
                    if (countOfPlayers == 1) {
                        Toast toast = Toast.makeText(this,
                                "Поздравляем! Вы победили!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        messageView.setText("Поздравляем! Вы победили!");
                    } else {
                        Toast toast = Toast.makeText(this,
                                "Победа за игроком №1! Поздравляем!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        messageView.setText("Победа за игроком №2! Поздравляем!");
                    }
                } else {
                    if (countOfPlayers == 1) {
                        Toast toast = Toast.makeText(this,
                                "К сожалению, Вы проиграли.",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        messageView.setText("К сожалению, Вы проиграли");
                    } else {
                        Toast toast = Toast.makeText(this,
                                "К сожалению, Вы проиграли.",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        messageView.setText("Победа за игроком №2! Поздравляем!");
                    }
                }
            }
            isShowRes = true;
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
                                    if (newCell.getmBegCell() == null) {
                                        noMoves = true;
                                        endGame = true;
                                        showResults();
                                        return;
                                    }
                                    getAvailCellsInBoard(mBoard, newCell.getmBegCell().getX(), newCell.getmBegCell().getY(), 2);
                                    mBoard.moveChecker(mandatoryMoves, newCell.getmBegCell().getX(), newCell.getmBegCell().getY(), newCell.getmEndCell().getX(), newCell.getmEndCell().getY(), mBoard.getCell(newCell.getmBegCell().getX(), newCell.getmBegCell().getY()), getApplicationContext());
                                    numberMove++;
                                    // Log.i(LOG_TAG, "Number move: " + numberMove);
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
        // }
    }

    private void updateScore() {
        // Log.i(LOG_TAG, "update score");
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

        List<Cell> availCells = new ArrayList<>();

        if ((colorPlayer == 2 && (board.getCell(begI, begJ) == COLOR_ENEMY || board.getCell(begI, begJ) == COLOR_ENEMY_KING))
                || (colorPlayer == 1 && (board.getCell(begI, begJ) == COLOR_PLAYER || board.getCell(begI, begJ) == COLOR_PLAYER_KING))) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    if (board.getCell(i, j) == 0) {
                        int flag = board.isValidMove(begI, begJ, i, j, colorPlayer);
                        if (flag == 1) {
                            availCells.add(new Cell(i, j));
                        } else if (flag == 2) {
                            availCells.add(new Cell(i, j));
                            mandatoryMoves.add(new PairCell(new Cell(begI, begJ), new Cell(i, j)));
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

    //Определить, принадлежит ли уровень максу
    private boolean isMaxLevel(int level) {
        //Все четные номера - принадлежат мину, все нечетные - максу
        if (level % 2 == 0) {
            return false;
        }
        return true;
    }

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
            // Log.i(LOG_TAG, "mandatory moves " + mandatoryMoves.toString());
            // Log.i(LOG_TAG, "pairCells " + pairCells.toString());
            if (mandatoryMoves.size() > 0) {
                pairCells.clear();
                pairCells.addAll(mandatoryMoves);
            }
            Random random = new Random();
            PairCell pairCell;

            int countOfBadMoves = 0;

            //если не осталось ходов
            if (pairCells.isEmpty()) {
                noMoves = true;
                endGame = true;
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
                            // pairCell = pairCells.get(random.nextInt(pairCells.size() - 1));
                            pairCell = pairCells.get(i);
                            i++;
                        }
                    }

                } else {
                    pairCell = pairCells.get(0);
                }
                mandatoryMoves.clear();
                // Log.i(LOG_TAG, pairCell.toString());
                return pairCell;
            }
        }
        if (level.equals("hard")) {
            tree = new Tree();
            PairCell bestMove = new PairCell();
            Board board = new Board(mBoard);

            //Устанавливаем корнем дерева первый ход игрока с idxParent -1
            tree.addChildren(currentMove, board, -1, 0, board.evaluationFunction());

            int idxParent = 0;
            int colorCurrentPlayer = COLOR_ENEMY;

            int levelMand = -1;
            //Цикл на максимальную глубину
            for (int l = 1; l < 5000; l++) {
                if (levelMand != tree.getLevelNode(idxParent)) {
                    if (isMaxLevel(tree.getLevelNode(idxParent))) {
                        colorCurrentPlayer = COLOR_ENEMY;
                    } else {
                        colorCurrentPlayer = COLOR_PLAYER;
                    }
                    ArrayList<PairCell> availMoves = new ArrayList<>();

                    for (int i = 0; i < ROWS; i++) {
                        for (int j = 0; j < COLUMNS; j++) {

                            List<Cell> arrayList = new ArrayList<>();
                            if (idxParent == -1) {
                                arrayList = getAvailCellsInBoard(board, i, j, colorCurrentPlayer);
                            } else {
                                if (idxParent < tree.getNodes().size())
                                    arrayList = getAvailCellsInBoard(tree.getNodes().get(idxParent).getBoard(), i, j, colorCurrentPlayer);
                            }

                            for (int k = 0; k < arrayList.size(); k++) {
                                availMoves.add(new PairCell(new Cell(i, j), arrayList.get(k)));
                            }
                        }
                    }

                    //если есть доступные ходы
                    //Добавляем все следующие возможные ходы в дерево решений с родителем первым ходом
                    for (int j = 0; j < availMoves.size(); j++) {
                        //На воображаемой доске переставляем шашку
                        mandatoryMoves = new ArrayList<>();
                        if (idxParent < tree.getNodes().size()) {
                            Board newBoard;
                            if (idxParent == -1) {
                                newBoard = new Board(board);
                            } else {
                                newBoard = new Board(tree.getNodes().get(idxParent).getBoard());
                            }

                            List<PairCell> allMandatoryMoves = getAllMandatoryMoves(newBoard, colorCurrentPlayer);
                            if (allMandatoryMoves.isEmpty() || allMandatoryMoves.contains(new PairCell(availMoves.get(j).getmBegCell(), availMoves.get(j).getmEndCell()))) {

                                getAvailCellsInBoard(newBoard, availMoves.get(j).getmBegCell().getX(), availMoves.get(j).getmBegCell().getY(), colorCurrentPlayer);

                                int res = newBoard.moveCheckerWithoutToast(mandatoryMoves, availMoves.get(j).getmBegCell().getX(), availMoves.get(j).getmBegCell().getY(),
                                        availMoves.get(j).getmEndCell().getX(), availMoves.get(j).getmEndCell().getY(), colorCurrentPlayer, this);
                                if (res != 0) {
                                    if (res == 2) {
                                        // Log.i(LOG_TAG, "! " + availMoves.get(j));
                                        tree.addChildren(availMoves.get(j), newBoard, idxParent, tree.getLevelNode(idxParent), newBoard.evaluationFunction());
                                        break;
                                    }
                                    tree.addChildren(availMoves.get(j), newBoard, idxParent, tree.getLevelNode(idxParent), newBoard.evaluationFunction());
                                }
                            }
                        }
                    }
                    idxParent++;
                }
            }
            mandatoryMoves = new ArrayList<>();
            //Если это уровень MAX, то сейчас ходят белые шашки

            //Сначала нужно расставить все evalMinMax на дерево
            int level = tree.getLevelNode(tree.getNodes().get(tree.getNodes().size() - 1).getIdxParent());
            //Если мы закончили на уровне противника
            if (level % 2 == 0) {
                level--;
            }
            for (int i = level; i > 0; i--) {
                if (i == 1) {
                    Log.i("aa", "!");
                }
                //Находим всех на этом уровне
                ArrayList<Tree.Node> listAll = tree.getNodesByLevel(i);

                //Группируем их по родителям
                while (!listAll.isEmpty()) {
                    ArrayList<Tree.Node> list = tree.getChildrens(listAll.get(0).getIdxParent());
                    //Находим макс или мин элемент из выбранных
                    //first - индекс
                    //second - значение макса или мина
                    Pair<Integer, Integer> pair = getMaxMinElem(list, i, level);
                    int idx = pair.first;
                    int eval = pair.second;
                    //Устанавливаем предку значение макс или мин, в зависимости от текущего уровня
                    tree.getNodes().get(tree.getNodes().get(idx).getIdxParent()).setEvalMinMax(eval);
                    listAll.removeAll(list);
                }
            }


            if (tree.getMaxInLevel(1) == null) {
                noMoves = true;
                endGame = true;
                showResults();
            } else {
                bestMove = tree.getMaxInLevel(1).getMove();
            }

            //После этого idx должен быть элемент 1го уровня, который выгоднее всего при подсчете стольких уровней
            Log.i("best " + LOG_TAG, bestMove.toString());
            return bestMove;
        }
        return new PairCell(0, 0, 0, 0);
    }

    private Pair<Integer, Integer> getMaxMinElem(ArrayList<Tree.Node> list, int level, int lastLevel) {
        int maxEval = -100;
        int minEval = 100;
        //  ArrayList<Tree.Node> elements = new ArrayList<>();
        int idx = -1;
        for (int j = 0; j < list.size(); j++) {
            if (isMaxLevel(level)) {
                if (level == lastLevel) {
                    if (list.get(j).getBoard().evaluationFunction() > maxEval) {
                        maxEval = list.get(j).getBoard().evaluationFunction();
                        idx = tree.getNodes().indexOf(list.get(j));
                    }
                } else {
                    if (list.get(j).getEvalMinMax() > maxEval) {
                        maxEval = list.get(j).getEvalMinMax();
                        idx = tree.getNodes().indexOf(list.get(j));
                    }
                }
            } else {
                if (level == lastLevel) {
                    if (list.get(j).getBoard().evaluationFunction() < minEval) {
                        minEval = list.get(j).getBoard().evaluationFunction();
                        idx = tree.getNodes().indexOf(list.get(j));
                    }
                } else {
                    if (list.get(j).getEvalMinMax() < minEval) {
                        minEval = list.get(j).getEvalMinMax();
                        idx = tree.getNodes().indexOf(list.get(j));
                    }
                }
            }
        }
        int tmp;
        if (isMaxLevel(level)) {
            tmp = maxEval;
        } else {
            tmp = minEval;
        }
        return new Pair<>(idx, tmp);
    }

}
