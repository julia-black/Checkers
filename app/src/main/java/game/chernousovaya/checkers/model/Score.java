package game.chernousovaya.checkers.model;


public class Score {
    int scoreWhite;
    int scoreBlack;

    public Score() {
        scoreWhite = 0;
        scoreBlack = 0;
    }



    public void incScoreWhite(int value) { //увеличить счет на value
        scoreWhite += value;

    }

    public void incScoreBlack(int value) {
        scoreBlack += value;

    }

    public int getScoreWhite() {
        return scoreWhite;
    }

    public void setScoreWhite(int scoreWhite) {
        this.scoreWhite = scoreWhite;
    }

    public int getScoreBlack() {
        return scoreBlack;
    }

    public void setScoreBlack(int scoreBlack) {
        this.scoreBlack = scoreBlack;
    }
}
