package cn.edu.nju.server;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.util.TimestampHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Server extends AbstractCheckerBuilder implements Runnable{

    private DataInputStream inputFromClient;

    private DataOutputStream outputToClient;

    private boolean finished = false;

    private Queue<Context> contextQueue = new LinkedList<>();

    private Timer deleteTimer = new Timer();

    private Timer checkTimer = new Timer();

    ContextParser contextParser = new ContextParser();

    public Server(String configFilePath)  {
        super(configFilePath);
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            System.out.println("Server started at " + new Date());

            Socket socket = serverSocket.accept();

            inputFromClient = new DataInputStream(socket.getInputStream());

            outputToClient = new DataOutputStream(socket.getOutputStream());

        }catch(IOException e) {
            e.printStackTrace();
        }

        checkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    System.out.println("1:" + new Date());
                }
            }
        }, 10,10);


        deleteTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    System.out.println("2:" + new Date());
                }
            }
        },1000, 1000);
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
                    finished = true;
                    break;
                }
                Context context = contextParser.parseContext(count,pattern);
                context.setTimestamp(TimestampHelper.getCurrentTimestamp());
                synchronized (this) {
                    contextQueue.offer(context);
                }

                System.out.println("[DEBUG] '+' " + pattern);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if(finished) {
            checkTimer.cancel();
            deleteTimer.cancel();
        }
    }



    public static void main(String[] args) {
        new Thread(new Server(args[0])).start();
    }
}
