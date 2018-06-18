package game.chernousovaya.checkers.model;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import game.chernousovaya.checkers.controller.activities.GameActivity;

public class Board {
    private static final String LOG_TAG = Board.class.getSimpleName();
    private static final int COLOR_ENEMY = 2;
    private static final int COLOR_PLAYER = 1;
    private static final int COLOR_ENEMY_KING = 4;
    private static final int COLOR_PLAYER_KING = 5;
    private static final int N = 8;
    private Score score = new Score();

    int[][] arr = new int[N][N];
    int temp = 0;

    //0 - пусто
    //3 - белая клетка, на нее нельзя ходить
    //2 - белая шашка
    //1 - черная шашка
    //4 - дамка белых
    //5 - дамка черных

    public Board(Board board) {
        this.score.setmScoreBlack(board.score.getmScoreWhite());
        this.score.setmScoreWhite(board.score.getmScoreWhite());
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                arr[i][j] = board.arr[i][j];
            }
        }
    }

    public Board() {
        temp = 0;
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

       //for (int i = 0; i < N; i++) {
       //    for (int j = 0; j < N; j++) {
       //        if (((i == 0 || i == 2) && j % 2 != 0)
       //                || (i == 1 && j % 2 == 0)) {
       //            arr[i][j] = 0;
       //        } else if ((i == 5 || i == 7) && j % 2 == 0
       //                || (i == 6 && j % 2 != 0)) {
       //            arr[i][j] = 0;
       //        } else if (i == 3 && j % 2 == 0 || i == 4 && j % 2 != 0) {
       //            arr[i][j] = 0;
       //        } else
       //            arr[i][j] = 3;
       //    }
       //}
       //arr[4][3] = 2;
       //arr[7][0] = 1;
       //arr[7][2] = 1;
       //arr[7][4] = 1;
       //arr[6][1] = 1;
       //showBoard();

    }

    private boolean isBadMoveWhite(int i, int j, int color) {
        if (color == COLOR_ENEMY || color == COLOR_ENEMY_KING) {
            if (i < 7) {
                if ((j < 7 && arr[i + 1][j + 1] == COLOR_PLAYER) || (j > 0 && arr[i + 1][j - 1] == COLOR_PLAYER))
                    return true;
            }
        }
        return false;
    }

    //Оценочная функция
    public int evaluationFunction(int x, int y, int color) {
        int countBlack = 0;
        int countWhite = 0;
        int countKingBlack = 0;
        int countKingWhite = 0;
        int k = isBadMoveWhite(x, y, color) ? 2 : 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                switch (arr[i][j]) {
                    case COLOR_PLAYER:
                        countBlack++;
                        break;
                    case COLOR_ENEMY:
                        countWhite++;
                        break;
                    case COLOR_PLAYER_KING:
                        countKingBlack++;
                        break;
                    case COLOR_ENEMY_KING:
                        countKingWhite++;
                }
            }
        }
        return (countWhite - countBlack) + 3 * (countKingWhite - countKingBlack) - k;
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

    //Проверить, не стала ли шашка дамкой
    private void checkKing(int i, int j, int checker) {
        if ((checker == COLOR_PLAYER) && (i == 0)) {
            setCell(i, j, COLOR_PLAYER_KING);
        }
        if (checker == COLOR_ENEMY && i == N - 1) {
            setCell(i, j, COLOR_ENEMY_KING);
        }
    }

    //ход шашки
    //0 - ошибка
    //1 - идут без взятия
    //2 - со взятием
    public int moveChecker(List<PairCell> mandatoryMoves, int begI, int begJ, int i, int j, int checker, Context context) { //checker - цвет выбранной шашки
        if (!GameActivity.noMoves) {
            if (mandatoryMoves.isEmpty()) {
                if (checker == getCell(begI, begJ) || (checker == 2 && (getCell(begI, begJ) == COLOR_ENEMY || getCell(begI, begJ) == COLOR_ENEMY_KING))
                        || (checker == 1 && (getCell(begI, begJ) == COLOR_PLAYER || getCell(begI, begJ) == COLOR_PLAYER_KING))) {
                    if (arr[i][j] == 0) { //если конечная клетка пуста
                        if (isValidMove(begI, begJ, i, j, checker) != 0) {
                            if (isValidMove(begI, begJ, i, j, checker) == 1) {
                                setCell(i, j, checker);
                                setCell(begI, begJ, 0);
                                checkKing(i, j, checker);
                                return 1;
                            }
                            //если со взятием
                            else {
                                capture(begI, begJ, i, j, checker);
                                setCell(i, j, checker);
                                setCell(begI, begJ, 0);
                                checkKing(i, j, checker);
                                return 2;
                            }
                        } else {
                            Toast toast = Toast.makeText(context,
                                    "Вы не можете пойти в эту клетку",
                                    Toast.LENGTH_SHORT);
                            toast.show();
                            return 0;
                        }
                    } else {
                        Toast toast = Toast.makeText(context,
                                "Эта клетка не пуста!",
                                Toast.LENGTH_SHORT);
                        toast.show();
                        return 0;
                    }
                } else {
                    Log.i(LOG_TAG, "Error, wrong current check" + checker + " " + getCell(begI, begJ));
                    return 0;
                }
            } else {
                PairCell pairCell = new PairCell(new Cell(begI, begJ), new Cell(i, j));
                //если ход игрока является одним из обязательных ходов
                if (getCell(begI, begJ) == checker && arr[i][j] == 0
                        && isValidMove(begI, begJ, i, j, checker) == 2
                        && containsPairCell(mandatoryMoves, pairCell)) { //если в этой ячейке стоит такой цвет и конечная клетка пуста
                    capture(begI, begJ, i, j, checker);
                    setCell(i, j, checker);
                    setCell(begI, begJ, 0);
                    checkKing(i, j, checker);
                    return 2;
                } else {
                    Toast toast = Toast.makeText(context,
                            "Вы обязаны бить!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                    return 0;
                }
            }
        }
        return 0;
    }

    //Ход без уведомлений (для алгоритма минимакс)
    public int moveCheckerWithoutToast(List<PairCell> mandatoryMoves, int begI, int begJ, int i, int j, int checker, Context context) { //checker - цвет выбранной шашки
        if (!GameActivity.noMoves) {
            if (mandatoryMoves.isEmpty()) {
                if (checker == getCell(begI, begJ) || (checker == 2 && (getCell(begI, begJ) == COLOR_ENEMY || getCell(begI, begJ) == COLOR_ENEMY_KING))
                        || (checker == 1 && (getCell(begI, begJ) == COLOR_PLAYER || getCell(begI, begJ) == COLOR_PLAYER_KING))) {
                    if (arr[i][j] == 0) { //если конечная клетка пуста
                        checker = getCell(begI, begJ);
                        if (isValidMove(begI, begJ, i, j, checker) != 0) {
                            if (isValidMove(begI, begJ, i, j, checker) == 1) {
                                setCell(i, j, checker);
                                setCell(begI, begJ, 0);
                                checkKing(i, j, checker);
                                return 1;
                            }
                            //если со взятием
                            else {
                                capture(begI, begJ, i, j, checker);
                                setCell(i, j, checker);
                                setCell(begI, begJ, 0);
                                checkKing(i, j, checker);
                                return 2;
                            }
                        } else {
                            Log.i(LOG_TAG, "Не валидный ход" + checker + " " + getCell(begI, begJ) + " [" + begI + "," + begJ + "] " + i + "," + j + " " + arr[i][j]);
                            return 0;
                        }
                    } else {
                        Log.i(LOG_TAG, "Эта клетка не пуста" + checker + " " + getCell(begI, begJ) + " [" + begI + "," + begJ + "] " + i + "," + j + " " + arr[i][j]);
                        return 0;
                    }
                } else {
                    Log.i(LOG_TAG, "Error, wrong current check" + checker + " " + getCell(begI, begJ) + " [" + begI + "," + begJ + "] " + i + "," + j);
                    return 0;
                }
            } else {
                checker = getCell(begI, begJ);
                PairCell pairCell = new PairCell(new Cell(begI, begJ), new Cell(i, j));
                //если ход игрока является одним из обязательных ходов
                if (getCell(begI, begJ) == checker && arr[i][j] == 0
                        && isValidMove(begI, begJ, i, j, checker) == 2
                        && containsPairCell(mandatoryMoves, pairCell)) { //если в этой ячейке стоит такой цвет и конечная клетка пуста
                    capture(begI, begJ, i, j, checker);
                    setCell(i, j, checker);
                    setCell(begI, begJ, 0);
                    checkKing(i, j, checker);
                    return 2;
                } else {
                    Log.i(LOG_TAG, "mandatory moves not empty, but error" + checker + " " + getCell(begI, begJ) + " " + begI + "," + begJ + " " + i + "," + j);
                    return 0;
                }
            }
        }
        return 0;
    }

    //Содержится ли этот ход в списке
    private boolean containsPairCell(List<PairCell> list, PairCell pairCell) {
        for (PairCell p : list) {
            if (p.equals(pairCell)) {
                return true;
            }
        }
        return false;
    }

    //Проверка хода на валидность
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
                else if ((i == begI + 2) || (checker == COLOR_ENEMY_KING && i == begI - 2)) {
                    //если идет влево и там стоит вражеская белая шашка
                    if (checker == COLOR_ENEMY || checker == COLOR_ENEMY_KING) {
                        if (begI < 7 && begJ < 7 && j == begJ + 2 && (arr[begI + 1][begJ + 1] == COLOR_PLAYER || arr[begI + 1][begJ + 1] == COLOR_PLAYER_KING)) {
                            return 2;
                        } else if (begI < 7 && begJ > 1)
                            if (j == begJ - 2 && (arr[begI + 1][begJ - 1] == COLOR_PLAYER || arr[begI + 1][begJ - 1] == COLOR_PLAYER_KING)) {
                                return 2;
                            }
                    }
                    if (checker == COLOR_ENEMY_KING) {
                        if (j == begJ + 2 && (arr[begI - 1][begJ + 1] == COLOR_PLAYER || arr[begI - 1][begJ + 1] == COLOR_PLAYER_KING)) {
                            return 2;
                        } else if (begJ > 1)
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
                    if (checker == COLOR_PLAYER || (checker == COLOR_PLAYER_KING && begI > i)) {
                        if ((i < 7 && j < 7) && (j == begJ - 2 && (arr[i + 1][j + 1] == COLOR_ENEMY || arr[i + 1][j + 1] == COLOR_ENEMY_KING))) {
                            return 2;
                        } else if ((i < 7) && (j == begJ + 2 && (arr[i + 1][j - 1] == COLOR_ENEMY || arr[i + 1][j - 1] == COLOR_ENEMY_KING))) {
                            return 2;
                        }
                    }
                    if (checker == COLOR_PLAYER_KING) {
                        if ((i > 0) && (j == begJ - 2 && (arr[i - 1][begJ - 1] == COLOR_ENEMY || arr[i - 1][begJ - 1] == COLOR_ENEMY_KING))) {
                            return 2;
                        } else if ((i > 0) && (j == begJ + 2 && (arr[i - 1][j - 1] == COLOR_ENEMY || arr[i - 1][j - 1] == COLOR_ENEMY_KING))) {
                            return 2;
                        }

                    }
                }
            }
        }
        return 0;
    }

    //Захват шашки
    public void capture(int begI, int begJ, int i, int j, int checker) {
        //если идет влево и там стоит вражеская белая шашка
        if (checker == COLOR_ENEMY || checker == COLOR_ENEMY_KING) {
            if (begI < 7 && begJ < 7 && j == begJ + 2 && (arr[begI + 1][begJ + 1] == COLOR_PLAYER || arr[begI + 1][begJ + 1] == COLOR_PLAYER_KING)) {
                captureEnemyChecker(begI + 1, begJ + 1, arr[begI + 1][begJ + 1]);
            } else if (begI < 7 && begJ > 1)
                if (j == begJ - 2 && (arr[begI + 1][begJ - 1] == COLOR_PLAYER || arr[begI + 1][begJ - 1] == COLOR_PLAYER_KING)) {
                    captureEnemyChecker(begI + 1, begJ - 1, arr[begI + 1][begJ - 1]);
                }
        }
        if (checker == COLOR_ENEMY_KING) {
            if (j == begJ + 2 && (arr[begI - 1][begJ + 1] == COLOR_PLAYER || arr[begI - 1][begJ + 1] == COLOR_PLAYER_KING)) {
                captureEnemyChecker(begI - 1, begJ + 1, arr[begI - 1][begJ + 1]);
            } else if (begJ > 1)
                if (j == begJ - 2 && (arr[begI - 1][begJ - 1] == COLOR_PLAYER || arr[begI - 1][begJ - 1] == COLOR_PLAYER_KING)) {
                    captureEnemyChecker(begI - 1, begJ - 1, arr[begI - 1][begJ - 1]);
                }
        } else if (checker == COLOR_PLAYER || checker == COLOR_PLAYER_KING) {
            if (j == begJ - 2 && (arr[i + 1][j + 1] == COLOR_ENEMY || arr[i + 1][j + 1] == COLOR_ENEMY_KING)) {
                captureEnemyChecker(i + 1, j + 1, arr[i + 1][j + 1]);
            } else if (j == begJ + 2 && (arr[i + 1][j - 1] == COLOR_ENEMY || arr[i + 1][j - 1] == COLOR_ENEMY_KING)) {
                captureEnemyChecker(i + 1, j - 1, arr[i + 1][j - 1]);
            }
        }
        if (checker == COLOR_PLAYER_KING) {
            if (j == begJ - 2 && i > 0 && (arr[i - 1][begJ - 1] == COLOR_ENEMY || arr[i - 1][begJ - 1] == COLOR_ENEMY_KING)) {
                captureEnemyChecker(i - 1, begJ - 1, arr[i - 1][begJ - 1]);
            } else if (j == begJ + 2 && i > 0 && (arr[i - 1][j - 1] == COLOR_ENEMY || arr[i - 1][j - 1] == COLOR_ENEMY_KING)) {
                captureEnemyChecker(i - 1, j - 1, arr[i - 1][j - 1]);
            }
        }
    }

    //Обнуление вражеской шашки и обновление счета
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
            if (countOfPlayers == 1 && temp == 0) {
                temp = 1;
                toast = Toast.makeText(context,
                        "К сожалению, Вы проиграли.",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else {
                if (temp == 0) {
                    temp = 1;
                    toast = Toast.makeText(context,
                            "Победа за игроком №2! Поздравляем!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            return true;
        } else if (score.getmScoreBlack() == 12) {
            if (countOfPlayers == 1 && temp == 0) {
                temp = 1;
                toast = Toast.makeText(context,
                        "Поздравляем! Вы победили!",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else {
                if (temp == 0) {
                    temp = 1;
                    toast = Toast.makeText(context,
                            "Победа за игроком №1! Поздравляем!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            return true;
        } else if (GameActivity.noMoves) {
            return true;
        }

        int countBlack = 0;
        int countWhite = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                switch (arr[i][j]) {
                    case COLOR_PLAYER:
                        countBlack++;
                        break;
                    case COLOR_ENEMY:
                        countWhite++;
                        break;
                    case COLOR_PLAYER_KING:
                        countBlack++;
                        break;
                    case COLOR_ENEMY_KING:
                        countWhite++;
                }
            }
        }
        if (countBlack == 0) {
            if (countOfPlayers == 1 && temp == 0) {
                temp = 1;
                toast = Toast.makeText(context,
                        "К сожалению, Вы проиграли.",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else {
                if (temp == 0) {
                    temp = 1;
                    toast = Toast.makeText(context,
                            "Победа за игроком №2! Поздравляем!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            return true;
        } else if (countWhite == 0) {
            if (countOfPlayers == 1 && temp == 0) {
                temp = 1;
                toast = Toast.makeText(context,
                        "Поздравляем! Вы победили!",
                        Toast.LENGTH_SHORT);
                toast.show();
            } else {
                if (temp == 0) {
                    temp = 1;
                    toast = Toast.makeText(context,
                            "Победа за игроком №1! Поздравляем!",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            return true;
        }
        return false;
    }

    //Отобразить в лог расстановку
    public void showBoard() {
        for (int i = 0; i < N; i++) {
            String str = "";
            for (int j = 0; j < N; j++) {
                str += arr[i][j] + " ";
            }
            Log.i(LOG_TAG, str);
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
