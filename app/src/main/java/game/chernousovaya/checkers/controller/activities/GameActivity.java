package game.chernousovaya.checkers.controller.activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import game.chernousovaya.checkers.R;
import game.chernousovaya.checkers.model.Board;
import game.chernousovaya.checkers.model.Cell;
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
    public static boolean noMoves = false; //Признак того, что больше нет доступных ходов
    private static final int maxDeep = 6; //Глубина построения дерева

    private int countOfPlayers = 0;
    private String level = "easy";
    private int currentPlayer = 1;

    private Board mBoard;
    private boolean isChooseCheck = false; //Выбрана ли шашка
    private Cell mChooseCell; //Выбранная клетка
    private PairCell currentMove = new PairCell(); //Текущий ход

    private boolean isPlayersMoved = false; //Прошел ли ход игрока
    private boolean isTimerStopped = false; //Вспомогательная переменная для задержки
    private TextView messageView;
    private boolean endGame = false; //Закончилась ли игра
    private boolean isShowRes = false; //Показан ли результат
    private boolean isResumeMove = false; //Признак того, что это продолжение хода, а не новый ход, т.е. в продолжении игрок должен бить дальше

    private List<PairCell> mandatoryMoves = new ArrayList<>(); //Обязательные ходы

    private Tree tree = new Tree(); //Дерево игры

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        endGame = false;
        noMoves = false;
        Intent intent = getIntent();
        //Получаем переданные из предыдущей активити данные
        countOfPlayers = intent.getIntExtra("count_players", 0);
        level = intent.getStringExtra("level");
        Log.i(LOG_TAG, "count = " + countOfPlayers + ", level = " + level);

        mBoard = new Board();
        mChooseCell = new Cell();
        renderBoard();

        TextView messageView = (TextView) findViewById(R.id.message);

        if (countOfPlayers == 1) {
            messageView.setText(" Ваш ход (Вы играете за черные)");
        } else {
            messageView.setText(" Первым ходит игрок №1 (Черные)");
        }
    }

    //Отрисовка доски
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
                            imageView.setOnClickListener(view -> {
                                if (countOfPlayers == 1) {
                                    if (mBoard.getCell(finalI, finalJ) == COLOR_PLAYER || mBoard.getCell(finalI, finalJ) == COLOR_PLAYER_KING) //если это шашка игрока
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

                                            //получаем все обязательные ходы
                                            List<PairCell> allMandatoryMoves = getAllMandatoryMoves(mBoard, mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()));

                                            //если данный ход содержится в обязательных ходах или обязательных ходов для битья нет, то ход возможен
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
                                                    //если это не продолжение хода, то ход переходит к противнику
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
                                        List<PairCell> allMandatoryMoves = getAllMandatoryMoves(mBoard, mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()));
                                        if (allMandatoryMoves.contains(new PairCell(mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ)) || allMandatoryMoves.isEmpty()) {

                                            getAvailCellsInBoard(mBoard, mChooseCell.getX(), mChooseCell.getY(), mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()));

                                            int withCapture = mBoard.moveChecker(mandatoryMoves, mChooseCell.getX(), mChooseCell.getY(), finalI, finalJ,
                                                    mBoard.getCell(mChooseCell.getX(), mChooseCell.getY()), getApplicationContext());
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
                            });
                        } else {
                            endGame = true;
                            if (!isShowRes)
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
      finish();
    }

    //Показать результат игры
    private void showResults() {
        Log.i(LOG_TAG, "Show res");
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

    //Ход противника
    private void moveEnemy() {
        if (mBoard.isEndOfGame(this, countOfPlayers)) {
            endGame = true;
            showResults();
        } else {
            messageView = (TextView) findViewById(R.id.message);
            messageView.setText("Ход противника");
            isTimerStopped = false;
            if (isPlayersMoved) {
                final Timer timer = new Timer();
                //Таймер нужен, чтобы игрок увидел сначала передвижение своей шашки, а только потом перемещение шашки противника
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(() -> {
                            if (isPlayersMoved) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                isTimerStopped = true;
                                if (isTimerStopped) {
                                    PairCell newCell = calculateBestMove();
                                    if (newCell.getmBegCell() == null) {
                                        noMoves = true;
                                        endGame = true;
                                        showResults();
                                        return;
                                    }
                                    getAvailCellsInBoard(mBoard, newCell.getmBegCell().getX(), newCell.getmBegCell().getY(), 2);
                                    mBoard.moveChecker(mandatoryMoves, newCell.getmBegCell().getX(), newCell.getmBegCell().getY(), newCell.getmEndCell().getX(), newCell.getmEndCell().getY(), mBoard.getCell(newCell.getmBegCell().getX(), newCell.getmBegCell().getY()), getApplicationContext());
                                    messageView = (TextView) findViewById(R.id.message);
                                    if (endGame)
                                        messageView.setText("");
                                    else
                                        messageView.setText("Ваш ход");
                                    renderBoard();
                                    isPlayersMoved = false;
                                    isTimerStopped = false;
                                }
                            }
                        });
                    }
                }, 0, 1000);
            }
        }
    }

    //Обновление счета на экране
    private void updateScore() {
        TextView scoreView = (TextView) findViewById(R.id.score);
        scoreView.setText(mBoard.getScore().getmScoreBlack() + ":" + mBoard.getScore().getmScoreWhite());
    }

    private boolean isBlackCell(int i, int j) {
        return ((i % 2 == 0 && j % 2 != 0) || (i % 2 != 0 && j % 2 == 0));
    }

    //Получить доступные клетки, куда мы можем пойти
    private List<Cell> getAvailCellsInBoard(Board board, int begI, int begJ, int colorPlayer) {

        List<Cell> availCells = new ArrayList<>();

        if (((colorPlayer == COLOR_ENEMY || colorPlayer == COLOR_ENEMY_KING) && (board.getCell(begI, begJ) == COLOR_ENEMY || board.getCell(begI, begJ) == COLOR_ENEMY_KING))
                || ((colorPlayer == COLOR_PLAYER || colorPlayer == COLOR_PLAYER_KING) && (board.getCell(begI, begJ) == COLOR_PLAYER || board.getCell(begI, begJ) == COLOR_PLAYER_KING))) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLUMNS; j++) {
                    if (board.getCell(i, j) == 0) {
                        int flag = board.isValidMove(begI, begJ, i, j, board.getCell(begI, begJ));
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

    //Получить все обязательные ходы, в которых мы бьем
    private List<PairCell> getAllMandatoryMoves(Board board, int colorPlayer) {
        List<PairCell> allMandatoryMoves = new ArrayList<>();
        //идем по всем шашкам выбранного игрока
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
    private boolean thereAreAnyMoves(Board board, int begI, int begJ, int colorPlayer) {
        //если да, то ход текущего игрока продолжается, нет - ход переходит
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

    //Определить, является ли это "плохим ходом"
    private boolean isBadMoveWhite(int i, int j) {
        if (i < 7) {
            if ((j < 7 && mBoard.getArr()[i + 1][j + 1] == COLOR_PLAYER) || (j > 0 && mBoard.getArr()[i + 1][j - 1] == COLOR_PLAYER))
                return true;
        }
        if (i < 7) {
            if ((j < 7 && mBoard.getArr()[i + 1][j + 1] == COLOR_PLAYER_KING) || (j > 0 && mBoard.getArr()[i + 1][j - 1] == COLOR_PLAYER_KING))
                return true;
        }
        return false;
    }

    //Получить часть дерева для выбранного idxParent
    private boolean createTree(int idxParent) {
        //определяем цвет текущего игрока
        int colorCurrentPlayer = isMaxLevel(tree.getLevelNode(idxParent)) ? COLOR_ENEMY : COLOR_PLAYER;
        ArrayList<PairCell> availMoves = new ArrayList<>();
        //Находим все возможные ходы на данный момент
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                List<Cell> arrayList = new ArrayList<>();
                if (idxParent == -1) { //если родитель = -1, то это нулевой уровень
                    arrayList = getAvailCellsInBoard(mBoard, i, j, colorCurrentPlayer);
                } else {
                    if (idxParent < tree.getNodes().size())
                        //присваиваем списку все доступные ходы для данной клетки
                        arrayList = getAvailCellsInBoard(tree.getNodes().get(idxParent).getBoard(), i, j, colorCurrentPlayer);
                }
                for (int k = 0; k < arrayList.size(); k++) {
                    availMoves.add(new PairCell(new Cell(i, j), arrayList.get(k)));
                }
            }
        }

        if (availMoves.isEmpty()) {
            return false;
        }
        //Добавляем все возможные ходы в дерево решений с родителем первым ходом
        for (int j = 0; j < availMoves.size(); j++) {
            mandatoryMoves = new ArrayList<>();
            if (idxParent < tree.getNodes().size()) {
                Board newBoard;
                if (idxParent == -1) {
                    newBoard = new Board(mBoard);
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
                            //если перемещение вернуло результат = 2, это значит, что мы ходили со взятием
                            tree.addChildren(availMoves.get(j), newBoard, idxParent, tree.getLevelNode(idxParent),
                                    newBoard.evaluationFunction(availMoves.get(j).getmEndCell().getX(), availMoves.get(j).getmEndCell().getY(), colorCurrentPlayer));
                            break;
                        }
                        tree.addChildren(availMoves.get(j), newBoard, idxParent, tree.getLevelNode(idxParent),
                                newBoard.evaluationFunction(availMoves.get(j).getmEndCell().getX(), availMoves.get(j).getmEndCell().getY(), colorCurrentPlayer));
                    }
                }
            }
        }
        return true;
    }

    //Расставляем мин и макс на уровне level (Последовательно)
    private void linearMiniMax(int level, int lastLevel) {
        ArrayList<Tree.Node> listAll = tree.getNodesByLevel(level);
        //Группируем их по родителям
        while (!listAll.isEmpty()) {
            if (level > 1) {
                if (isMaxLevel(level)) {
                    // можно не проводить поиска на поддереве, растущем из всякой MAX
                    // вершины, у которой значение альфа не меньше значения бета всех ее родительских MIN вершин.
                    int idx = tree.getNodes().get(listAll.get(0).getIdxParent()).getIdxParent();
                    if ((tree.getNodes().get(listAll.get(0).getIdxParent()).getAlpha())
                            > tree.getNodes().get(idx).getBeta()) {
                        break;
                    }
                } else {
                    //можно не проводить поиска на поддереве, растущем из всякой MIN вершины, у которой значение альфа
                    //не превышает значения бета всех ее родительских MAX вершин;
                    int idx = tree.getNodes().get(listAll.get(0).getIdxParent()).getIdxParent();
                    if ((tree.getNodes().get(listAll.get(0).getIdxParent()).getBeta())
                            < tree.getNodes().get(idx).getAlpha()) {
                        break;
                    }
                }
            }
            ArrayList<Tree.Node> list = tree.getChildrens(listAll.get(0).getIdxParent());
            //Находим макс или мин элемент из выбранных
            Pair<Integer, Integer> pair = getMaxMinElem(list, level, lastLevel);
            int idx = pair.first; //индекс
            int eval = pair.second; //значение макса или мина на данной вершине
            //Устанавливаем предку значение макс или мин, в зависимости от текущего уровня
            tree.getNodes().get(tree.getNodes().get(idx).getIdxParent()).setEvalMinMax(eval);
            listAll.removeAll(list);
        }
    }

    //Вычислить лучший ход
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
                        if (isBadMoveWhite(pairCells.get(i).getmEndCell().getX(), pairCells.get(i).getmEndCell().getY())) {
                            countOfBadMoves++;
                        }
                    }
                    int i = 0;
                    if (countOfBadMoves < pairCells.size()) {
                        while (isBadMoveWhite(pairCell.getmEndCell().getX(), pairCell.getmEndCell().getY())) {
                            pairCell = pairCells.get(random.nextInt(pairCells.size() - 1));
                            i++;
                            if (i > pairCells.size()) {
                                pairCell = pairCells.get(0);
                                break;
                            }
                        }
                    } else {
                        pairCell = pairCells.get(random.nextInt(pairCells.size() - 1));
                    }

                } else {
                    pairCell = pairCells.get(0);
                }
                mandatoryMoves.clear();
                return pairCell;
            }
        }
        if (level.equals("hard")) {
            tree = new Tree();
            PairCell bestMove = new PairCell();
            Board board = new Board(mBoard);

            //Устанавливаем корнем дерева первый ход игрока
            tree.addChildren(currentMove, board, -1, 0, board.evaluationFunction(currentMove.getmEndCell().getX(), currentMove.getmEndCell().getY(), COLOR_PLAYER));

            int idxParent = 0;
            long start = System.currentTimeMillis();

            boolean isCreate = true;
            while (isCreate && tree.getLevelNode(tree.getNodes().get(tree.getNodes().size() - 1).getIdxParent()) <= maxDeep && !board.isEndOfGame(this, countOfPlayers)) {
                isCreate = createTree(idxParent);
                idxParent++;
            }
            long end = System.currentTimeMillis();
            long traceTime = end - start;
            Log.i("Results", "________");
            Log.i("Results", "Time create tree " + traceTime + " ms");
            mandatoryMoves = new ArrayList<>();
            //Если это уровень MAX, то сейчас ходят белые шашки
            int level = tree.getLevelNode(tree.getNodes().get(tree.getNodes().size() - 1).getIdxParent());
            Log.i("Level", level + "");
            Log.i("Results", "Count all elements in tree " + tree.getNodes().size());

            for (int i = level; i > 0; i--) {
                // miniMax(i, level);
                parallelMinMax(i, level); //Применяем алгоритм минимакс на каждом уровне
            }

            if (tree.getMaxInLevel(1) == null) {
                noMoves = true;
                endGame = true;
                showResults();
            } else {
                bestMove = tree.getMaxInLevel(1).getMove(); //лучший ход - это элемент 1го уровня дерева с максимальной оценкой
            }
            long end1 = System.currentTimeMillis();
            long traceTime1 = end1 - end;
            long fullTime = end1 - start;
            Log.i("Results", "Time minimax " + traceTime1 + " ms");
            Log.i("Results", "Full time " + fullTime + " ms");
            Log.i(LOG_TAG, "Best move " + bestMove.toString());
            return bestMove;
        }
        return new PairCell(0, 0, 0, 0);
    }

    //Расставляем мин и макс на уровне = level (Параллельно)
    private void parallelMinMax(int level, int lastLevel) {
        ArrayList<Tree.Node> listAll = tree.getNodesByLevel(level);
        //Кол-во потоков равное число доступных процессоров
        int countThreads = Runtime.getRuntime().availableProcessors();
        //Создаем пул потоков
        ExecutorService pool = Executors.newFixedThreadPool(countThreads);
        List<Callable<Object>> tasks = new ArrayList<>();
        try {
            for (int i = 0; i < listAll.size(); i++) {
                tasks.add(() -> {
                    if (level > 1) {
                        if (isMaxLevel(level)) {
                            // можно не проводить поиска на поддереве, растущем из всякой MAX
                            // вершины, у которой значение альфа не меньше значения бета всех ее родительских MIN вершин.
                            int idx = tree.getNodes().get(listAll.get(0).getIdxParent()).getIdxParent();
                            if ((tree.getNodes().get(listAll.get(0).getIdxParent()).getAlpha())
                                    > tree.getNodes().get(idx).getBeta()) {
                                return null;
                            }
                        } else {
                            //можно не проводить поиска на поддереве, растущем из всякой MIN вершины, у которой значение альфа
                            //не превышает значения бета всех ее родительских MAX вершин;
                            int idx = tree.getNodes().get(listAll.get(0).getIdxParent()).getIdxParent();
                            if ((tree.getNodes().get(listAll.get(0).getIdxParent()).getBeta())
                                    < tree.getNodes().get(idx).getAlpha()) {
                                return null;
                            }
                        }
                    }
                    ArrayList<Tree.Node> list = tree.getChildrens(listAll.get(0).getIdxParent());
                    //Находим макс или мин элемент из выбранных
                    Pair<Integer, Integer> pair = getMaxMinElem(list, level, lastLevel);
                    int idx = pair.first;
                    int eval = pair.second;
                    //Устанавливаем предку значение макс или мин, в зависимости от текущего уровня
                    tree.getNodes().get(tree.getNodes().get(idx).getIdxParent()).setEvalMinMax(eval);
                    listAll.removeAll(list);
                    return null;
                });
            }
            List<Future<Object>> invokeAll = pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    //Получить макс или мин элемент в поддереве
    private Pair<Integer, Integer> getMaxMinElem(ArrayList<Tree.Node> list, int level, int lastLevel) {
        int maxEval = -100;
        int minEval = 100;
        int idx = -1;
        for (int j = 0; j < list.size(); j++) {
            if (isMaxLevel(level)) {
                if (level == lastLevel) {
                    if (list.get(j).getBoard().evaluationFunction(
                            list.get(j).getMove().getmEndCell().getX(), list.get(j).getMove().getmEndCell().getY(), COLOR_ENEMY) > maxEval) {
                        maxEval = list.get(j).getBoard().evaluationFunction(
                                list.get(j).getMove().getmEndCell().getX(), list.get(j).getMove().getmEndCell().getY(), COLOR_ENEMY);
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
                    if (list.get(j).getBoard().evaluationFunction(list.get(j).getMove().getmEndCell().getX(), list.get(j).getMove().getmEndCell().getY(),
                            COLOR_PLAYER) < minEval) {
                        minEval = list.get(j).getBoard().evaluationFunction(list.get(j).getMove().getmEndCell().getX(), list.get(j).getMove().getmEndCell().getY(), COLOR_PLAYER);
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
        int tmp = isMaxLevel(level) ? maxEval : minEval;
        return new Pair<>(idx, tmp);
    }

}
