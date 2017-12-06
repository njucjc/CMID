package cn.edu.nju.server;

import cn.edu.nju.client.Client;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.util.TimestampHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Server extends Thread{

    private DataInputStream inputFromClient;

    private DataOutputStream outputToClient;

    ContextParser contextParser = new ContextParser();

    public Server()  {
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            System.out.println("Server started at " + new Date());

            Socket socket = serverSocket.accept();

            inputFromClient = new DataInputStream(socket.getInputStream());

            outputToClient = new DataOutputStream(socket.getOutputStream());

        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int count = 0;
        try {
            while (true) {
                String pattern = inputFromClient.readUTF();
                count++;
                if ("exit".equals(pattern)) {
                    System.out.println("Good bye! Server closed at " + new Date());
                    break;
                }
                Context context = contextParser.parseContext(count,pattern);
                context.setTimestamp(TimestampHelper.getCurrentTimestamp());
                System.out.println(context.getTimestamp());

                System.out.println("[DEBUG] '+' " + pattern);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        new Server().start();
    }
}
