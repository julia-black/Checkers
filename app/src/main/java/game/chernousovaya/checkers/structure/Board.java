package game.chernousovaya.checkers.structure;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Board {


    private static final int N = 8;
    private static final String LOG_TAG = "Board";
    int[][] arr = new int[N][N];

    public Board() {
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

    public int getCell(int i, int j) {
        return arr[i][j];
    }

    public void setCell(int i, int j, int checker) {
        arr[i][j] = checker;
    }

    public boolean moveChecker(int begI, int begJ, int i, int j, int checker, Context context) {
        if (getCell(begI, begJ) == checker) { //если в этой ячейке стоит такой цвет
            if (arr[i][j] == 0) { //если конечная клетка пуста
                setCell(i, j, checker);
                setCell(begI, begJ, 0);
                return true;
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
