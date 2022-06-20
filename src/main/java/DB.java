import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.pi.project.server.db.ID;
import ir.pi.project.shared.model.User;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;

public class DB {
    public XOGameModel get(int id) {
        try {
            File directory = new File("./src/main/resources/Info/xoGameModels");
            File Data = new File(directory, id + ".json");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(Data));
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            XOGameModel xoGameModel = gson.fromJson(bufferedReader, XOGameModel.class);
            bufferedReader.close();
            return xoGameModel;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }



    public void update(XOGameModel xoGameModel) {
        try {
            File directory = new File("./src/main/resources/Info/xoGameModels");
            if(!directory.exists()) directory.mkdirs();
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            File Data = new File(directory, xoGameModel.getId() + ".json");
            if (!Data.exists())
                Data.createNewFile();
            FileWriter writer = new FileWriter(Data);
            writer.write(gson.toJson(xoGameModel));
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public LinkedList<XOGameModel> all() {
        LinkedList<XOGameModel> xoGameModels=new LinkedList<>();
        File directory=new File("./src/main/resources/Info/xoGameModels");
        for (File file:
                directory.listFiles()) {
            XOGameModel xoGameModel=this.get(ID.getIdFromFileName(file.getName()));
            xoGameModels.add(xoGameModel);
        }
        return xoGameModels;
    }

    public int newID(){
        int s=0;
        try {
            File lastId = new File("./src/main/resources/lastId");
            Scanner sc = new Scanner(lastId);
            int q = sc.nextInt();
            s=q;
            FileOutputStream fout = new FileOutputStream(lastId, false);
            PrintStream out = new PrintStream(fout);
            q++;
            out.println(q);
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return s;
    }
}
