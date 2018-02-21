package game.chernousovaya.checkers.model;


public class PairCell {

    private Cell mBegCell;
    private Cell mEndCell;

    public PairCell(Cell mBegCell, Cell mEndCell) {
        this.mBegCell = mBegCell;
        this.mEndCell = mEndCell;
    }

    public PairCell() {
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
}
