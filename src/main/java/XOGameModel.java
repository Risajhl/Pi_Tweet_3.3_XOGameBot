import java.util.Random;

public class XOGameModel {
    private int id;
    private int player1=-1;
    private int player2=-1;
    private String[] board;
    private boolean isStarted;
    private boolean isOver;
    private int turn;

    public XOGameModel(int id){
        this.id=id;
        this.board=new String[9];
        for(int i=0;i<9;i++)
            board[i]=String.valueOf(i+1);
        this.isStarted=false;
        this.isOver=false;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlayer1() {
        return player1;
    }

    public void setPlayer1(int player1) {
        this.player1 = player1;
    }

    public int getPlayer2() {
        return player2;
    }

    public void setPlayer2(int player2) {
        this.player2 = player2;
    }

    public void setTurns(){
        this.turn = ((int) (Math.random() * 2)) == 0 ? player1 : player2;

    }

    public String[] getBoard() {
        return board;
    }

    public void setBoard(String[] board) {
        this.board = board;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public void setCell(int index,String string){
        board[index]=string;
    }
}
