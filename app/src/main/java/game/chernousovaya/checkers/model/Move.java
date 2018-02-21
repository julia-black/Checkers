package game.chernousovaya.checkers.model;

public class Move {
    Cell mBegCell;
    Cell mEndCell;
    int mScoreEnemy;


    public int getmScoreEnemy() {
        return mScoreEnemy;
    }

    @Override
    public String toString() {
        return "Move{" +
                "mBegCell=" + mBegCell +
                ", mEndCell=" + mEndCell +
                ", mScoreEnemy=" + mScoreEnemy +
                '}';
    }

    public Move() {
    }

    public Cell getmBegCell() {
        return mBegCell;
    }

    public void setmBegCell(Cell mBegCell) {
        this.mBegCell = mBegCell;
    }

    public Cell getmEndCell() {
        return mEndCell;
    }

    public void setmEndCell(Cell mEndCell) {
        this.mEndCell = mEndCell;
    }

    public void setmScoreEnemy(int mScoreEnemy) {
        this.mScoreEnemy = mScoreEnemy;
    }


    public Move(Cell mBegCell, Cell mEndCell, int mScoreEnemy) {
        this.mBegCell = mBegCell;
        this.mEndCell = mEndCell;
        this.mScoreEnemy = mScoreEnemy;
    }
}
