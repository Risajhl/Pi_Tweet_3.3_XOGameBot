import ir.pi.project.server.controller.ClientHandler;
import ir.pi.project.server.db.Context;
import ir.pi.project.server.model.CurrentChat;
import ir.pi.project.shared.enums.Pages.MessagesPage;
import ir.pi.project.shared.enums.others.MessageStatus;
import ir.pi.project.shared.event.messages.NewMessageEvent;
import ir.pi.project.shared.model.Message;
import ir.pi.project.shared.model.User;

import java.util.List;

public class XOGame {
    ClientHandler clientHandler;
    BotSender botSender;
    Context context = new Context();
    DB db = new DB();

    public XOGame(ClientHandler clientHandler, BotSender botSender) {
        this.clientHandler = clientHandler;
        this.botSender = botSender;
    }

    public void check() {
        User bot = context.Users.get(Main.id);
        for (List<Integer> chat : bot.getChats()) {
            if (chat.size() == 1) {
                Message message = context.Messages.get(chat.get(0));
                if (message.getStatus() != MessageStatus.SEEN) {
                    message.setStatus(MessageStatus.SEEN);
                    context.Messages.update(message);

                    clientHandler.setCurrentUserId(Main.id);
                    clientHandler.setCurrentChat(new CurrentChat());
                    clientHandler.getCurrentChat().setTheOther(message.getSenderId());
                    String ans= """
                        Hello, Thanks for choosing XOBot!
                        to make new game use /XOBot-newGame,
                        to join a game use /XOBot-join-GameId
                        """;
                    botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
                }
            } else {
                for (Integer messageId : chat) {
                    Message message = context.Messages.get(messageId);
                    if (message.getSenderId() != Main.id) {
                        if (message.getStatus() != MessageStatus.SEEN) {
                            message.setStatus(MessageStatus.SEEN);
                            context.Messages.update(message);

                            clientHandler.setCurrentUserId(Main.id);
                            clientHandler.setCurrentChat(new CurrentChat());
                            clientHandler.getCurrentChat().setTheOther(message.getSenderId());

                            String text = message.getText();
                            String ans = "Sorry I can't understand your demand.";

                            if (text.startsWith("/XOBot")) {
                                sendResponse(text);
                            } else {
                                botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
                            }

                        }
                    }
                }
            }
        }
    }


    public void sendResponse(String text) {
        String ans = "Sorry I can't understand your demand.";
        text = text.substring(6);
        if (text.startsWith("-newGame") && !(text.length() >8)) {
            botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, getNewGame(), null));
        } else if (text.startsWith("-join-")) {
            clientHandler.setCurrentUserId(Main.id);
            clientHandler.getCurrentChat().setTheOther(clientHandler.getCurrentChat().getTheOther());
            XOGameModel xoGameModel = joinGame(text.substring(6));
            if (xoGameModel != null && xoGameModel.isStarted() && !xoGameModel.isOver()) {
                if(clientHandler.getCurrentChat().getTheOther()!=xoGameModel.getPlayer1()) {
                    xoGameModel.setPlayer2(clientHandler.getCurrentChat().getTheOther());
                    xoGameModel.setTurns();
                    db.update(xoGameModel);
                    sendForBoth(xoGameModel);
                }else {
                    ans = "You can't join the game you made";
                    botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
                }
            } else {
                ans = "GameNotFound";
                botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
            }
        } else if (text.startsWith("-hit-")) hit(text.substring(5));

        else botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));

    }


    public String getNewGame() {
        int id = 200000 + db.newID();
        XOGameModel xoGameModel = new XOGameModel(id);
        xoGameModel.setPlayer1(clientHandler.getCurrentChat().getTheOther());
        db.update(xoGameModel);

        return "New Game is created with id: " + id + "" +
                "\nAsk your friend to join with this id!";
    }

    public XOGameModel joinGame(String text) {
        try {
            int gameId = Integer.parseInt(text);
            for (XOGameModel xoGameModel : db.all())
                if (xoGameModel.getId() == gameId) {
                    xoGameModel.setStarted(true);
                    db.update(xoGameModel);
                    return xoGameModel;
                }
        } catch (Exception ignored) { }

        return null;
    }

    public void hit(String text) {
        String ans = "Sorry I can't understand your demand.";
        try {
            String[] parts = text.split("-", 2);
            int gameId = Integer.parseInt(parts[0]);
            int cellIndex = Integer.parseInt(parts[1]);
            if (cellIndex < 0 || cellIndex > 9) {
                ans = "you should choose from 1 to 9 !";
                botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
            }
            else {
                XOGameModel gameModel = null;
                for (XOGameModel xoGameModel : db.all()) {
                    if (xoGameModel.getId() == gameId) gameModel = xoGameModel;
                }
                if (gameModel != null && !gameModel.isOver()) {
                    if (gameModel.getTurn() == clientHandler.getCurrentChat().getTheOther()) {
                        if (!gameModel.getBoard()[cellIndex - 1].equals("X") && !gameModel.getBoard()[cellIndex - 1].equals("O")) {
                            if (clientHandler.getCurrentChat().getTheOther() == gameModel.getPlayer1())
                            {
                                gameModel.setCell(cellIndex - 1, "O");
                                gameModel.setTurn(gameModel.getPlayer2());
                            }
                            else {
                                gameModel.setCell(cellIndex - 1, "X");
                                gameModel.setTurn(gameModel.getPlayer1());
                            }
                            db.update(gameModel);

                            sendForBoth(gameModel);
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            checkWinner(gameModel);

                        } else {
                            ans = "this cell is already chosen";
                            botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
                        }
                    } else {
                        ans = "not your turn!";
                        botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
                    }
                } else {
                    ans = "There's no game with this id";
                    botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
                }
            }
        } catch (Exception e) {
            botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
        }

    }

    public String getBoard(XOGameModel xoGameModel, int playerId) {
        String[] board = xoGameModel.getBoard();
        String turn;
        if (xoGameModel.getTurn() == playerId) turn = "Your turn";
        else turn = "enemy's turn";

        return turn + " \n" +
                "| " + board[0] + " | " + board[1] + " | " + board[2] + " |" + "\n" +
                "| " + board[3] + " | " + board[4] + " | " + board[5] + " |" + "\n" +
                "| " + board[6] + " | " + board[7] + " | " + board[8] + " |" + "\n" +
                "to hit a cell use /XOBot-hit-"+xoGameModel.getId()+"-NumberOfTheCell";
    }

    public void sendForBoth(XOGameModel xoGameModel) {
        clientHandler.getCurrentChat().setTheOther(xoGameModel.getPlayer1());
        botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, getBoard(xoGameModel, xoGameModel.getPlayer1()), null));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        clientHandler.getCurrentChat().setTheOther(xoGameModel.getPlayer2());
        botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, getBoard(xoGameModel, xoGameModel.getPlayer2()), null));
    }

    public void checkWinner(XOGameModel xoGameModel) {
        String[] board = xoGameModel.getBoard();
        int winner = -1;
        for (int i = 0; i < 8; i++) {
            String line = switch (i) {
                case 0 -> board[0] + board[1] + board[2];
                case 1 -> board[3] + board[4] + board[5];
                case 2 -> board[6] + board[7] + board[8];
                case 3 -> board[0] + board[3] + board[6];
                case 4 -> board[1] + board[4] + board[7];
                case 5 -> board[2] + board[5] + board[8];
                case 6 -> board[0] + board[4] + board[8];
                case 7 -> board[2] + board[4] + board[6];
                default -> null;
            };
            if (line.equals("XXX")) {
                winner = xoGameModel.getPlayer2();
                break;
            } else if (line.equals("OOO")) {
                winner = xoGameModel.getPlayer1();
                break;
            }
        }


        if (winner != -1) {
            xoGameModel.setOver(true);
            db.update(xoGameModel);
            User user = context.Users.get(winner);
            String ans = "The winner is " + user.getUserName();
            clientHandler.getCurrentChat().setTheOther(xoGameModel.getPlayer1());
            botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clientHandler.getCurrentChat().setTheOther(xoGameModel.getPlayer2());
            botSender.addEvent(new NewMessageEvent(MessagesPage.DIRECT_CHATS, ans, null));
        }
    }


}
