package game.chernousovaya.checkers.model;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Board {
    private static final String LOG_TAG = Board.class.getSimpleName();
    private static final int COLOR_ENEMY = 2;
    private static final int COLOR_PLAYER = 1;
    private static final int COLOR_ENEMY_KING = 4;
    private static final int COLOR_PLAYER_KING = 5;
    private static final int N = 8;
    private Score score;

    int[][] arr = new int[N][N];

    //0 - пусто
    //3 - белая клетка, на нее нельзя ходить
    //2 - белая шашка (компьютера)
    //1 - черная шашка (игрока)
    //4 - дамка белых
    // 5 - дамка черных

    public Board() {
        score = new Score();
        //первоначальная расстановка шашек
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (((i == 0 || i == 2) && j % 2 != 0)
                        || (i == 1 && j % 2 == 0)) {
                    arr[i][j] = 2;
                } else if ((i == 5 || i == 7) && j % 2 == 0
                        || (i == 6 && j % 2 != 0)) {
                    arr[i][j] = 1;
                } else if (i == 3 && j % 2 == 0 || i == 4 && j % 2 != 0) {
                    arr[i][j] = 0;
                } else
                    arr[i][j] = 3;
            }
        }
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public int getCell(int i, int j) {
        return arr[i][j];
    }

    public void setCell(int i, int j, int checker) {
        arr[i][j] = checker;
    }

    private void checkKing(int i, int j, int checker) {
        if ((checker == COLOR_PLAYER) && (i == 0)) {
            setCell(i, j, COLOR_PLAYER_KING);
        }
        if (checker == COLOR_ENEMY && i == N - 1) {
            setCell(i, j, COLOR_ENEMY_KING);
        }
    }

    //ход шашки
    public boolean moveChecker(int begI, int begJ, int i, int j, int checker, Context context) { //checker - цвет выбранной шашки
        if (getCell(begI, begJ) == checker) { //если в этой ячейке стоит такой цвет
            if (arr[i][j] == 0) { //если конечная клетка пуста
                if (isValidMove(begI, begJ, i, j, checker) != 0) {
                    if (isValidMove(begI, begJ, i, j, checker) == 1) {
                        Log.i(LOG_TAG, "Обнуляется - " + begI + "," + begJ);
                        setCell(i, j, checker);
                        setCell(begI, begJ, 0);
                        checkKing(i, j, checker);
                        return true;
                    }
                    //если со взятием
                    else {
                        Log.i(LOG_TAG, "Обнуляется - " + begI + "," + begJ);
                        capture(begI, begJ, i, j, checker);
                        setCell(i, j, checker);
                        setCell(begI, begJ, 0);
                        checkKing(i, j, checker);
                        return true;
                    }
                } else {
                    Toast toast = Toast.makeText(context,
                            "Вы не можете пойти в эту клетку",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    return false;
                }
            } else {
                Toast toast = Toast.makeText(context,
                        "Эта клетка не пуста!",
                        Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }
        } else {
            Log.i(LOG_TAG, "Error, wrong current check");
            return false;
        }
    }

    //проверка хода на валидность
    //0 - не валидный
    //1 - валидный, без взятия
    //2 - валидный и со взятием
    public int isValidMove(int begI, int begJ, int i, int j, int checker) {
        if (arr[begI][begJ] != 3 && arr[i][j] != 3) {
            if (checker == COLOR_ENEMY || checker == COLOR_ENEMY_KING) { //если ходят белые
                //если они идут без взятия
                if (begI < 8 && ((i == begI + 1 || checker == COLOR_ENEMY_KING && i == begI - 1) && (begJ < 8 && (j == begJ + 1) || (begJ > 0 && (j == begJ - 1))))) {
                    return 1;
                }
                //если идут со взятием
                else if ((i == begI + 2) || checker == COLOR_ENEMY_KING && i == begI - 2) {
                    //если идет влево и там стоит вражеская белая шашка
                    if (checker == COLOR_ENEMY) {
                        if (begI < 7 && begJ < 7 && j == begJ + 2 && (arr[begI + 1][begJ + 1] == COLOR_PLAYER || arr[begI + 1][begJ + 1] == COLOR_PLAYER_KING)) {
                            return 2;
                        } else if (begI < 7 && begJ > 1)
                            if (j == begJ - 2 && (arr[begI + 1][begJ - 1] == COLOR_PLAYER || arr[begI + 1][begJ - 1] == COLOR_PLAYER_KING)) {
                                return 2;
                            }
                    } else if (checker == COLOR_ENEMY_KING) {
                        if (j == begJ + 2 && (arr[begI - 1][begJ + 1] == COLOR_PLAYER || arr[begI - 1][begJ + 1] == COLOR_PLAYER_KING)) {
                            return 2;
                        } else if (begI < 7 && begJ > 1)
                            if (j == begJ - 2 && (arr[begI - 1][begJ - 1] == COLOR_PLAYER || arr[begI - 1][begJ - 1] == COLOR_PLAYER_KING)) {
                                return 2;
                            }
                    }
                }

            } else if (checker == COLOR_PLAYER || checker == COLOR_PLAYER_KING) { //если ходят черные
                //если они идут без взятия
                if ((i == begI - 1 || (checker == COLOR_PLAYER_KING && i == begI + 1)) && (j == begJ + 1 || j == begJ - 1)) {
                    return 1;
                }
                //если идут со взятием
                else if (i == begI - 2 || (checker == COLOR_PLAYER_KING && i == begI + 2)) {
                    //если идет влево и там стоит вражеская белая шашка
                    if (checker == COLOR_PLAYER) {
                        if (j == begJ - 2 && (arr[i + 1][j + 1] == COLOR_ENEMY || arr[i + 1][j + 1] == COLOR_ENEMY_KING)) {
                            captureEnemyChecker(i + 1, j + 1, arr[i + 1][j + 1]);
                            return 2;
                        } else if (j == begJ + 2 && (arr[i + 1][j - 1] == COLOR_ENEMY || arr[i + 1][j - 1] == COLOR_ENEMY_KING)) {
                            captureEnemyChecker(i + 1, j - 1, arr[i + 1][j - 1]);
                            return 2;
                        }
                    } else if (checker == COLOR_PLAYER_KING) {
                        if (j == begJ - 2 && (arr[i - 1][j + 1] == COLOR_ENEMY || arr[i - 1][j + 1] == COLOR_ENEMY_KING)) {
                            captureEnemyChecker(i - 1, j + 1, arr[i - 1][j + 1]);
                            return 2;
                        } else if (j == begJ + 2 && (arr[i - 1][j - 1] == COLOR_ENEMY || arr[i - 1][j - 1] == COLOR_ENEMY_KING)) {
                            captureEnemyChecker(i - 1, j - 1, arr[i - 1][j - 1]);
                            return 2;
                        }
                    }
                }
            }
        }
        return 0;
    }

    public void capture(int begI, int begJ, int i, int j, int colorChecker) {

        if (begI < 7 && begJ < 7 && j == begJ + 2 && (arr[begI + 1][begJ + 1] == COLOR_PLAYER || arr[begI + 1][begJ + 1] == COLOR_PLAYER_KING)) {
            captureEnemyChecker(begI + 1, begJ + 1, arr[begI + 1][begJ + 1]);
        } else if (begI < 7 && begJ > 1)
            if (j == begJ - 2 && (arr[begI + 1][begJ - 1] == COLOR_PLAYER || arr[begI + 1][begJ - 1] == COLOR_PLAYER_KING)) {
                captureEnemyChecker(begI + 1, begJ - 1, arr[begI + 1][begJ - 1]);
            }

        if (arr[begI][begJ] == COLOR_ENEMY_KING) {
             if(j == begJ - 2 &&( arr[begI - 1][begJ + 1] == COLOR_PLAYER || arr[begI - 1][begJ + 1] == COLOR_PLAYER_KING)){
                 captureEnemyChecker(begI - 1, begJ + 1, arr[begI - 1][begJ + 1]);
             }
             else if(j == begJ + 2 && (arr[begI - 1][begJ - 1] == COLOR_PLAYER || arr[begI - 1][begJ - 1] == COLOR_PLAYER_KING) ){
                 captureEnemyChecker(begI - 1, begJ - 1, arr[begI - 1][begJ - 1]);
             }
        }
    }

    //Захват вражеской клетки
    private void captureEnemyChecker(int i, int j, int colorEnemyChecker) {
        arr[i][j] = 0;
        if (colorEnemyChecker == COLOR_ENEMY || colorEnemyChecker == COLOR_ENEMY_KING) {
            score.incScoreBlack(1);
        } else if (colorEnemyChecker == COLOR_PLAYER || colorEnemyChecker == COLOR_PLAYER_KING) {
            score.incScoreWhite(1);
        }
    }

    public boolean isEndOfGame(Context context, int countOfPlayers) {
        Toast toast;
        if (score.getmScoreWhite() == 12) {
            if (countOfPlayers == 1) {
                toast = Toast.makeText(context,
                        "К сожалению, Вы проиграли.",
                        Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(context,
                        "Победа за игроком №2! Поздравляем!",
                        Toast.LENGTH_SHORT);
            }
            toast.show();
            return true;
        } else if (score.getmScoreBlack() == 12) {
            if (countOfPlayers == 1) {
                toast = Toast.makeText(context,
                        "Поздравляем! Вы победили!",
                        Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(context,
                        "Победа за игроком №1! Поздравляем!",
                        Toast.LENGTH_SHORT);
            }
            toast.show();
            return true;
        }
        return false;
    }

    //отобразить в лог расстановку
    public void showBoard() {
        for (int i = 0; i < N; i++) {
            String str = "";
            for (int j = 0; j < N; j++) {
                str += arr[i][j] + " ";
            }
            Log.i("Board", str);
        }
    }

    public static int getN() {
        return N;
    }

    public int[][] getArr() {
        return arr;
    }

    public void setArr(int[][] arr) {
        this.arr = arr;
    }
}
