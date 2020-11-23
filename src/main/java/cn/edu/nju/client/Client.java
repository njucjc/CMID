package cn.edu.nju.client;

import cn.edu.nju.util.FileHelper;
import cn.edu.nju.util.TimestampHelper;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Client implements Runnable{
    private DatagramSocket socket;
    private InetAddress address;
    private int port = 8000;

    private List<String> contextStrList;
    private List<Long> sleepTime;

    private static boolean isPaused;

    private static boolean isFinished;

    public static double progress;

    public static synchronized void go() {
        isPaused = false;
    }

    public static synchronized void pause() {
        isPaused = true;
    }

    public static synchronized void finish() {
        isFinished = true;
    }

    public static synchronized void reset() {
        isPaused = false;
        isFinished = false;
        progress = 0.0;
    }

    public Client(int port, String contextFilePath)  {
        reset();
        this.contextStrList = new ArrayList<>();
        this.sleepTime = new ArrayList<>();


        try {
            FileReader fr = new FileReader(contextFilePath);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            String lastLine = br.readLine();
            int timestampIndex = 0;
            if(lastLine.contains("+")) { //第一行总是增加
                timestampIndex = 3;
            }
            contextStrList.add(lastLine);
            while ((line = br.readLine()) != null) {
                contextStrList.add(line);
                long diff = TimestampHelper.timestampDiff(line.split(",")[timestampIndex], lastLine.split(",")[timestampIndex]);
                sleepTime.add(diff);
                lastLine = line;
            }
            sleepTime.add(1L);

            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        System.out.println("Client begins to start.....");

        long sleepMillis = 0;
        long totalTime = 0;
        long startTime = System.nanoTime();
        long endTime = 0;

        sendMsg("timeFlag," + getTimestamp(contextStrList.get(0)) + "," + getTimestamp(contextStrList.get(contextStrList.size() - 1)));

        for (int i = 0; i < contextStrList.size(); ){
            if (isFinished) {
                break;
            }

            if (!isPaused) {
                System.out.println("Send " + i + " at " + TimestampHelper.getCurrentTimestamp() + ", sleep:" + sleepMillis + " ms");
                sleepMillis = sleepTime.get(i);
                sendMsg(i + "," + contextStrList.get(i) + "," + sleepMillis);
                endTime = System.nanoTime();

                long diff = (endTime - startTime) - totalTime * 1000000;
                totalTime += sleepMillis;

//            assert diff >= 0 : "Time error !";

                sleepMillis = (sleepMillis - diff / 1000000) > 0 ? (sleepMillis - diff / 1000000) : 0;

                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                progress = ((double) i) / contextStrList.size();
                i++;
            }

        }
        endTime = System.nanoTime();
        System.out.println("Total send time： " + (endTime - startTime) / 1000000 + " ms");

        if (!isFinished) {
            progress = 1.0;
            sendMsg("exit");
        }
        socket.close();
    }

    private void sendMsg(String msg) {
        byte [] buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        try {
            socket.send(packet);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTimestamp(String chg) {
        if (chg.contains("+") || chg.contains("-")) {
            return chg.split(",")[3];
        }
        else {
            return chg.split(",")[0];
        }
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            Properties properties = new Properties();
            try {
                if (!FileHelper.isFileExists(args[0])) {
                    System.out.println("[INFO] 配置文件不存在: " + args[0]);
                    System.exit(1);
                }

                FileInputStream fis = new FileInputStream(args[0]);
                properties.load(fis);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String dataFilePath = properties.getProperty("dataFilePath");
            String changeFilePath = properties.getProperty("changeFilePath");
            String changeHandlerType = properties.getProperty("changeHandlerType");

            Thread client;
            if("time".equals(changeHandlerType.split("-")[1])) {
                client = new Thread(new Client(8000, dataFilePath));
            }
            else {
                client = new Thread(new Client(8000, changeFilePath));
            }

            client.setPriority(Thread.MAX_PRIORITY);
            client.start();
        }
        else {
            System.out.println("Usage: java Client [configFilePath].");
        }
    }
}
