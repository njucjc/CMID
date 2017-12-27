package cn.edu.nju.server;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.TimestampHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Server extends AbstractCheckerBuilder implements Runnable{
    private DatagramSocket serverSocket;
    private boolean running;
    private int port = 8000;
    private byte [] buf = new byte[256];

//    private Timer checkTimer = new Timer();
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(100);

    private Set<String> timeTaskSet = ConcurrentHashMap.newKeySet();


    private ContextParser contextParser = new ContextParser();

    public Server(String configFilePath)  {
        super(configFilePath);
        try {
            serverSocket = new DatagramSocket(port);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    class ContextTimeoutTask extends TimerTask {
        private String timestamp;

        public ContextTimeoutTask(String timestamp) {
            this.timestamp = timestamp;
        }

        @Override
        public void run() {
            for(String key : patternMap.keySet()) {
                Pattern pattern = patternMap.get(key);
                Checker checker = checkerMap.get(pattern.getId());
                checker.delete(pattern.getId(), timestamp);
                pattern.deleteFirstByTime(timestamp);
            }
            scheduler.update();
            if(scheduler.schedule()) {
                doCheck();
            }
            timeTaskSet.remove(timestamp);
        }
    }

    @Override
    public void run() {
        running = true;

        int count = 0;
        long startTime = System.nanoTime();
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                String msg = new String(packet.getData(),0, packet.getLength());
                if ("exit".equals(msg)) {
                    System.out.println("Good bye! Server closed at " + new Date());
                    running = false;
                    break;
                }
                int num = Integer.parseInt(msg.substring(0, msg.indexOf(",")));
                msg = msg.substring(msg.indexOf(",") + 1);
                Context context = contextParser.parseContext(num, msg);
                context.setTimestamp(TimestampHelper.getCurrentTimestamp());
                count++;
                //do checking
                for(String key : patternMap.keySet()) {
                    Pattern p = patternMap.get(key);
                    if(p.isBelong(context)) {
                        String delTime = TimestampHelper.plusMillis(context.getTimestamp(), p.getFreshness());
                        if(timeTaskSet.add(delTime)) {
//                            checkTimer.schedule(new ContextTimeoutTask(delTime), TimestampHelper.parserDate(delTime));
                            scheduledExecutorService.schedule(new ContextTimeoutTask(delTime), p.getFreshness(), TimeUnit.MILLISECONDS);
                        }
                        p.addContext(context);
                        Checker checker = checkerMap.get(p.getId());
                        checker.add(p.getId(),context);
                    }
                }
                scheduler.update();
                if(scheduler.schedule()) {
                    doCheck();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        while (!timeTaskSet.isEmpty());
        scheduledExecutorService.shutdown();
//        checkTimer.cancel();
        long endTime = System.nanoTime();

        int inc = 0;
        for (Checker checker : checkerList) {
            inc += checker.getInc();
        }
        LogFileHelper.getLogger().info("run time: " + (endTime - startTime) / 1000000 + " ms");
        LogFileHelper.getLogger().info("Total Inc: " + inc);
        LogFileHelper.getLogger().info("Receive: " + count );
    }

    public static void main(String[] args) {
        Thread serverThread = new Thread(new Server(args[0]));
        serverThread.setPriority(Thread.MAX_PRIORITY);
        serverThread.start();
    }
}
