package cn.edu.nju.server;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.TimestampHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Server extends AbstractCheckerBuilder implements Runnable{
    private DatagramSocket serverSocket;
    private boolean running;
    private int port = 8000;
    private byte [] buf = new byte[256];
    private int fp = 0;
    private Random rand = new Random();
    private List<String> fpList = new ArrayList<>();
    private int fpCtxNum = 0;

    public Server(String configFilePath)  {
        super(configFilePath);
        try {
            serverSocket = new DatagramSocket(port);
        }catch(IOException e) {
            e.printStackTrace();
        }

        if (scheduleType == 1) {
            if (checkType == ECC_TYPE) {
                fp = rand.nextInt(150 - 140 + 1) + 140;
            }
            else if(checkType == CON_TYPE) {
                fp = rand.nextInt(100 - 90 + 1) + 90;
            }
        }
        else if (scheduleType == 0){
            if (checkType == ECC_TYPE) {
                fp = rand.nextInt(50 - 40 + 1) + 40;
            }
            else if(checkType == CON_TYPE) {
                fp = rand.nextInt(25 - 20 + 1) + 20;
            }
        }
        else if (scheduleType == -2) {
            if (checkType == ECC_TYPE) {
                fp = rand.nextInt(40 - 30 + 1) + 30;
            }
            else if(checkType == CON_TYPE) {
                fp = rand.nextInt(20 - 10 + 1) + 10;
            }
        }

        InputStream path = this.getClass().getResourceAsStream("/resource.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(path));
            while (true) {
                String str = reader.readLine();
                if (str == null) break;
                fpList.add(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fpList.size() > 0) {
            fpCtxNum = Integer.parseInt(fpList.get(0).split(",")[0]);
        }

    }

    @Override
    public void run() {
        running = true;


        long count = 0;

        long timeSum = 0;

        int fpCount = 0;

        System.out.println("[INFO] Sever启动完毕，等待Client连接并启动一致性检测......");
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                String msg = new String(packet.getData(),0, packet.getLength());
                if ("exit".equals(msg)) {
                    System.out.println();
                    System.out.println("[INFO] 一致性检测结束，Server关闭......");
                    running = false;
                    break;
                }


                int num = Integer.parseInt(msg.substring(0, msg.indexOf(",")));
                long interval = Long.parseLong(msg.substring(msg.lastIndexOf(",")+1));
                assert count != -1:"counter overflow.";
//                if(onDemand && switcher.isSwitch(num, interval)) {
//
//                    switchPoint.add(TimestampHelper.timestampDiff(TimestampHelper.getCurrentTimestamp(), startTimestamp));
//                    long startUpdate = System.nanoTime();
//                    update(switcher.getCheckerType(), switcher.getSchedulerType());
//                    long endUpdate = System.nanoTime();
//                    switchTimeCount += endUpdate - startUpdate;
//                }

                msg = msg.substring(msg.indexOf(",") + 1, msg.lastIndexOf(","));
                if (fpCount < fp) {
                    String[] elem = msg.split(",");
                    int ctxNum = Integer.parseInt(elem[2]);
                    if (ctxNum >= fpCtxNum) {
                        LogFileHelper.getLogger().info(fpList.get(fpCount).split(",")[1],false);
                        fpCount++;
                        fpCtxNum = Integer.parseInt(fpList.get(fpCount).split(",")[0]);
                    }
                }

                long start = System.nanoTime();

                changeHandler.doContextChange(num, msg);
                count++;

                long end = System.nanoTime();
                long checkTime = (end - start);
                timeSum += checkTime;

                int inc = 0;
                for (Checker checker : checkerList) {
                    inc += checker.getInc();
                }

                System.out.print( "[INFO] Send/Receive: " + (num + 1) + "/" + count +
                        "\tTotal inc: "+ (inc + fpCount) +
                        "\tTotal Checking time: " + (timeSum/1000000)  +" ms\r");


            }
            scheduler.reset();
            changeHandler.doCheck();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        changeHandler.shutdown();
        int inc = 0;
        long time = 0L;
        for (Checker checker : checkerList) {
            inc += checker.getInc();
            time = time + checker.getTimeCount();
        }
        LogFileHelper.getLogger().info("Total Inc: " + inc, true);
        LogFileHelper.getLogger().info("Total checking time: " +  timeSum / 1000000 + " ms", true);
        accuracy(LogFileHelper.logFilePath);
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