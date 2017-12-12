package cn.edu.nju.server;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.TimestampHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Server extends AbstractCheckerBuilder implements Runnable{

    private DataInputStream inputFromClient;

    private DataOutputStream outputToClient;

    private BlockingQueue<Context> contextQueue = new LinkedBlockingQueue<>();

    private Timer checkTimer = new Timer();

    private int lost = 0;


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
            public synchronized void run() {
                System.out.println("job0 start.");
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
                    System.out.println("Check add");
                    scheduler.update();
                    if(scheduler.schedule()) {
                        doCheck();
                    }
                }
                System.out.println("job0 end.");
            }
        }, 10,10);


        checkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public synchronized void run() {
                System.out.println("job1 start");
                String currentTimestamp = TimestampHelper.getCurrentTimestamp();
                for(String key : patternMap.keySet()) {
                    Pattern pattern = patternMap.get(key);
                    Checker checker = checkerMap.get(pattern.getId());
                    checker.delete(pattern.getId(), currentTimestamp);
                    pattern.deleteFirstByTime(currentTimestamp);
                }
                scheduler.update();
                if(scheduler.schedule()) {
                    doCheck();
                }
                System.out.println("job1 end");
            }
        },1000, 100);

        //清除堆积的未处理数据，
        checkTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public synchronized void  run() {
                System.out.println("job2 start.");
                String timestamp = TimestampHelper.getCurrentTimestamp();
                Iterator<Context> it = contextQueue.iterator();
                while(it.hasNext()) {
                    Context context = it.next();
                    if (TimestampHelper.timestampDiff(context.getTimestamp(), timestamp) >= 500) {
                        it.remove();
                        lost++;
                    } else {
                        break;
                    }
                }
                System.out.println("job2 end");
            }
        },1000, 500);

    }

    @Override
    public void run() {
        int count = 0;
        long startTime = System.nanoTime();
        try {
            while (true) {
                String pattern = inputFromClient.readUTF();
                if ("exit".equals(pattern)) {
                    System.out.println("Good bye! Server closed at " + new Date());
                    break;
                }
                Context context = contextParser.parseContext(count,pattern);
                context.setTimestamp(TimestampHelper.getCurrentTimestamp());
                contextQueue.offer(context);
                count++;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        while(true) {
            System.out.println("Test Empty.");
            if(contextQueue.isEmpty()) {
                break;
            }
        }

        long endTime = System.nanoTime();
        checkTimer.cancel();

        int inc = 0;
        for (Checker checker : checkerList) {
            inc += checker.getInc();
        }
        LogFileHelper.getLogger().info("run time: " + (endTime - startTime) / 1000000 + " ms");
        LogFileHelper.getLogger().info("Total Inc: " + inc);
        LogFileHelper.getLogger().info("Lost: " + lost );
    }

    public static void main(String[] args) {
        new Thread(new Server(args[0])).start();
    }
}
