package game.chernousovaya.checkers;


import android.util.Log;

public class Board {
    private static final int N = 8;
    int[][] arr = new int[N][N];

    public Board() {
        //первоначальная расстановка шашек
        for (int i = 0; i < N ; i++) {
            for(int j = 0; j < N; j++){
                if(((i == 0 || i == 2) && j % 2 != 0)
                       ||( i ==1 && j % 2 == 0) ){
                    arr[i][j] = 1;
                }
                else if((i == 5 || i == 7) && j % 2 == 0
                        || (i == 6 && j % 2 != 0)){
                    arr[i][j] = 2;
                }
                else{
                    arr[i][j] = 0;
                }
            }
        }
    }

    public void showBoard(){
        for (int i = 0; i < N; i++) {
            String str = "";
            for (int j = 0; j < N; j++) {
               str += arr[i][j] + " ";
            }
            Log.i("Board",str);
        }
    }
}
