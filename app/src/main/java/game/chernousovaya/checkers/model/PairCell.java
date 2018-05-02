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

    public PairCell(int x0, int y0, int x1, int y1) {
        this.mBegCell = new Cell(x0, y0);
        this.mEndCell = new Cell(x1, y1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        PairCell pairCell = (PairCell) obj;
        return this.mBegCell.getX() == pairCell.mBegCell.getX()
                && this.mBegCell.getY() == pairCell.mBegCell.getY()
                && this.mEndCell.getX() == pairCell.mEndCell.getX()
                && this.mEndCell.getY() == pairCell.mEndCell.getY();
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

    @Override
    public String toString() {
        return "PairCell{" +
                "mBegCell=" + mBegCell +
                ", mEndCell=" + mEndCell +
                '}';
    }
}
