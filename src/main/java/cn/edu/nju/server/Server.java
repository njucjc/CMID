package cn.edu.nju.server;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.util.Accuracy;
import cn.edu.nju.util.FileHelper;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.TimestampHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Server extends AbstractCheckerBuilder{
    private DatagramSocket serverSocket;
    public static int port = 8000;
    private byte [] buf = new byte[256];

    private static String startTime = "2007-10-26 11:00:00:000";
    private static String endTime = "2007-10-26 23:01:00:111";


    public Server() {
        reset();
        try {
            serverSocket = new DatagramSocket(port);
        }catch(IOException e) {
            e.printStackTrace();
        }
        port++;
    }

    @Override
    public void run() {

        System.out.println("[INFO] Sever启动完毕，端口为：" + (port - 1) + "，等待Client连接并启动一致性检测......");
        try {
            while (!isFinished) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                String msg = new String(packet.getData(),0, packet.getLength());
                if ("exit".equals(msg)) {
                    System.out.println();
                    System.out.println("[INFO] 一致性检测结束，Server关闭......");
                    break;
                }

                if (msg.contains("timeFlag")) {
                    startTime = msg.split(",")[1];
                    endTime = msg.split(",")[2];
                    continue;
                }

                if (isPaused) {
                    continue;
                }

                int num = Integer.parseInt(msg.substring(0, msg.indexOf(",")));
                interval = Long.parseLong(msg.substring(msg.lastIndexOf(",")+1));
                assert dataCount != -1:"counter overflow.";

                msg = msg.substring(msg.indexOf(",") + 1, msg.lastIndexOf(","));
                progress = ((double) diff(msg, startTime)) / TimestampHelper.timestampDiff(startTime, endTime);

                long start = System.nanoTime();

                changeHandler.doContextChange(num, msg);
                dataCount++;

                long end = System.nanoTime();
                long checkTime = (end - start);
                totalTime += checkTime;

                int inc = 0;
                for (Checker checker : checkerList) {
                    inc += checker.getInc();
                }

                System.out.print( "[INFO] Send/Receive: " + (num + 1) + "/" + dataCount +
                        "\tTotal inc: "+ inc +
                        "\tTotal Checking time: " + (totalTime / 1000000)  +" ms\r");


            }

            if (!isFinished) {
                progress = 1.0;
            }
            scheduler.reset();
            changeHandler.doCheck();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        changeHandler.shutdown();
        int inc = 0;
        for (Checker checker : checkerList) {
            inc += checker.getInc();
        }
        LogFileHelper.getLogger().info("Total Inc: " + inc, true);
        LogFileHelper.getLogger().info("Total checking time: " +  totalTime / 1000000 + " ms", true);

        accuracy(true);
        shutdown();
    }

    private long diff(String chg1, String timestamp) {
        String [] elem1 = chg1.split(",");

        if (changeHandlerType.contains("time")) {
            return TimestampHelper.timestampDiff(elem1[0], timestamp);
        }
        else {
            return TimestampHelper.timestampDiff(elem1[3], timestamp);
        }
    }


    public static void main(String[] args) {
        if(args.length == 1) {
            Server server = new Server();
            if (!FileHelper.isFileExists(args[0])) {
                System.out.println("[INFO] 配置文件不存在: " + args[0]);
                System.exit(1);
            }
            String msg = server.parseConfigFile(args[0]);
            if (msg == null) {
                Thread checkerThread = new Thread(server);
                checkerThread.setPriority(Thread.MAX_PRIORITY);
                checkerThread.start();
            }
            else {
                System.exit(1);
            }
        }
        else {
            System.out.println("Usage: java Server [configFilePath].");
        }
    }
}
