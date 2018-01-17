package game.chernousovaya.checkers.structure;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Board {

    private Score score;

    private static final int N = 8;
    private static final String LOG_TAG = "Board";
    int[][] arr = new int[N][N];

    //0 - пусто
    //1 - белая шашка (ИИ)
    //2 - черная шашка (игрока)

    public Board() {
        score = new Score();
        //первоначальная расстановка шашек
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (((i == 0 || i == 2) && j % 2 != 0)
                        || (i == 1 && j % 2 == 0)) {
                    arr[i][j] = 1;
                } else if ((i == 5 || i == 7) && j % 2 == 0
                        || (i == 6 && j % 2 != 0)) {
                    arr[i][j] = 2;
                } else {
                    arr[i][j] = 0;
                }
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

    public boolean moveChecker(int begI, int begJ, int i, int j, int checker, Context context) { //checker - цвет выбранной шашки
        if (getCell(begI, begJ) == checker) { //если в этой ячейке стоит такой цвет

            if (arr[i][j] == 0) { //если конечная клетка пуста

                if (isValidMove(begI, begJ, i, j, checker)) { //если это валидный ход по правилам английский шашек

                    setCell(i, j, checker);
                    setCell(begI, begJ, 0);
                    return true;
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
            Log.i(LOG_TAG, "Ошибка");
            return false;
        }
    }

    private boolean isValidMove(int begI, int begJ, int i, int j, int checker) {

        if (checker == 1) { //если ходят белые
            return true;
        } else if (checker == 2) { //если ходят черные
            //если они идут без взятия
            if (i == begI - 1 && (j == begJ + 1 || j == begJ - 1)) {
                return true;
            }
            //если идут со взятием
            else if (i == begI - 2) {
                //если идет влево и там стоит вражеская белая шашка
                if (j == begJ - 2 && arr[i + 1][j + 1] == 1) {
                    captureEnemyChecker(i + 1, j + 1, 1);
                    return true;
                } else if (j == begJ + 2 && arr[i + 1][j - 1] == 1) {
                    captureEnemyChecker(i + 1, j - 1, 1);
                    return true;
                }
            }
        }
        return false;
    }

    //Захват вражеской клетки
    private void captureEnemyChecker(int i, int j, int colorEnemyChecker) {
        arr[i][j] = 0;
        if (colorEnemyChecker == 1) {
            score.incScoreBlack(1);
        } else if (colorEnemyChecker == 2) {
            score.incScoreWhite(1);
        }

    }

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
