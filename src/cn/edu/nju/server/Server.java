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

    public Server(String configFilePath)  {
        super(configFilePath);
        try {
            serverSocket = new DatagramSocket(port);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;

        long count = 0;
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
                changeHandler.doContextChange(num, msg);
                count++;

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        changeHandler.shutdown();

        long endTime = System.nanoTime();

        int inc = 0;
        for (Checker checker : checkerList) {
            inc += checker.getInc();
        }
        LogFileHelper.getLogger().info("run time: " + (endTime - startTime) / 1000000 + " ms");
        LogFileHelper.getLogger().info("Total Inc: " + inc);
        LogFileHelper.getLogger().info("Receive: " + count );
        shutdown();
    }

    public static void main(String[] args) {
        if(args.length == 1) {
            Thread serverThread = new Thread(new Server(args[0]));
            serverThread.setPriority(Thread.MAX_PRIORITY);
            serverThread.start();
        }
        else {
            System.out.println("Usage: java Server [configFilePath].");
        }
    }
}
