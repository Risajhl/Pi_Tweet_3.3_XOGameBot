import ir.pi.project.server.controller.ClientHandler;

public class Main {
    static int id;
    String username="XO_Bot";
    XOGame xoGame;

    public Main() {
        BotSender botSender=new BotSender();
        ClientHandler clientHandler = new ClientHandler(botSender);
        clientHandler.start();
        this.xoGame= new XOGame(clientHandler,botSender);

    }

    public void start(){
        xoGame.check();
    }

    public static void setId(int id) {
        Main.id = id;
    }

    public String getUsername() {
        return username;
    }
}
