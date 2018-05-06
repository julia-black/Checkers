package game.chernousovaya.checkers.model;

public class Move {
    Cell mBegCell;
    Cell mEndCell;
    int evalFunc;


    public Move(Cell mBegCell, Cell mEndCell, int evalFunc) {
        this.mBegCell = mBegCell;
        this.mEndCell = mEndCell;
        this.evalFunc = evalFunc;
    }

    public int getEvalFunc() {
        return evalFunc;
    }

    public void setEvalFunc(int evalFunc) {
        this.evalFunc = evalFunc;
    }

    public Move() {
    }
    public Move (PairCell pairCell, int evalFunc){
        this.mBegCell = pairCell.getmBegCell();
        this.mEndCell = pairCell.getmEndCell();
        this.evalFunc = evalFunc;
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

    public PairCell getPairCell(){
        return new PairCell(this.mBegCell, this.mEndCell);
    }


}
