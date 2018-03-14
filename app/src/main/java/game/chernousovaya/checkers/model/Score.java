package game.chernousovaya.checkers.model;


public class Score {
    int mScoreWhite;
    int mScoreBlack;

    public Score() {
        this.mScoreBlack = 0;
        this.mScoreWhite = 0;
    }

    public Score(int mScoreWhite, int mScoreBlack) {
        this.mScoreWhite = mScoreWhite;
        this.mScoreBlack = mScoreBlack;
    }

    public int getmScoreWhite() {
        return mScoreWhite;
    }

    public void setmScoreWhite(int mScoreWhite) {
        this.mScoreWhite = mScoreWhite;
    }

    public int getmScoreBlack() {
        return mScoreBlack;
    }

    public void setmScoreBlack(int mScoreBlack) {
        this.mScoreBlack = mScoreBlack;
    }

    public void incScoreWhite(int value) { //увеличить счет на value
        mScoreWhite += value;

    }

    public void incScoreBlack(int value) {
        mScoreBlack += value;

    }
}
