package cn.edu.nju.server;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.pattern.Pattern;
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
                    Context c = contextQueue.poll();
                    if(c != null) {
                        for(String key : patternMap.keySet()) {
                            Pattern pattern = patternMap.get(key);
                            if(pattern.isBelong(c)) {
                                pattern.addContext(c);
                                Checker checker = checkerMap.get(pattern.getId());
                                checker.add(pattern.getId(),c);
                            }
                        }
                        scheduler.update();
                        if(scheduler.schedule()) {
                            doCheck();
                        }
                    }
                }
            }
        }, 10,10);


        deleteTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    String currentTimestamp = TimestampHelper.getCurrentTimestamp();
                    for(String key : patternMap.keySet()) {
                        Pattern pattern = patternMap.get(key);
                        pattern.deleteFirstByTime(currentTimestamp);
                        Checker checker = checkerMap.get(pattern.getId());
                        checker.delete(pattern.getId(), currentTimestamp);
                    }
                    scheduler.update();
                    if(scheduler.schedule()) {
                        doCheck();
                    }
                }
            }
        },1000, 100);
    }

    @Override
    public void run() {
        int count = 0;
        try {
            while (true) {
                String pattern = inputFromClient.readUTF();
                if ("exit".equals(pattern)) {
                    System.out.println("Good bye! Server closed at " + new Date());
                    break;
                }
                Context context = contextParser.parseContext(count,pattern);
                context.setTimestamp(TimestampHelper.getCurrentTimestamp());
                synchronized (this) {
                    contextQueue.offer(context);
                }
                count++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            synchronized (this) {
                if(contextQueue.isEmpty()) {
                    break;
                }
            }
        }

        checkTimer.cancel();
        deleteTimer.cancel();

    }

    public static void main(String[] args) {
        new Thread(new Server(args[0])).start();
    }
}
