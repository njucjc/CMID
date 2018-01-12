package cn.edu.nju.client;

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
    int port = 8000;

    private List<String> contextStrList;
    private List<Long> sleepTime;


    public Client(String contextFilePath)  {
        this.contextStrList = new ArrayList<>();
        this.sleepTime = new ArrayList<>();


        try {
            FileReader fr = new FileReader(contextFilePath);
            BufferedReader br = new BufferedReader(fr);
            String line = null;
            String lastLine = br.readLine();
            contextStrList.add(lastLine);
            while ((line = br.readLine()) != null) {
                contextStrList.add(line);
                long diff = TimestampHelper.timestampDiff(line.split(",")[0], lastLine.split(",")[0]);
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
        for (int i = 0; i < contextStrList.size(); i++){

            System.out.println("Send " + i + " at " + TimestampHelper.getCurrentTimestamp() + ", sleep:" + sleepMillis + " ms");
            sleepMillis = sleepTime.get(i);
            sendMsg(i+ "," + contextStrList.get(i));
            endTime = System.nanoTime();

            long diff = (endTime - startTime) - totalTime * 1000000;
            totalTime += sleepMillis;

            assert diff >= 0 : "TIme error !";

            sleepMillis = (sleepMillis - diff / 1000000) > 0 ? (sleepMillis - diff / 1000000) : 0;

            try {
                Thread.sleep(sleepMillis);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        endTime = System.nanoTime();
        System.out.println("Total send time： " + (endTime - startTime) / 1000000 + " ms");

        while (true) {
            sendMsg("exit");
        }

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

    public static void main(String[] args) {
        if (args.length == 1) {
            Properties properties = new Properties();
            try {
                FileInputStream fis = new FileInputStream(args[0]);
                properties.load(fis);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread client = new Thread(new Client(properties.getProperty("contextFilePath")));
            client.setPriority(Thread.MAX_PRIORITY);
            client.start();
        }
        else {
            System.out.println("Usage: java Client [configFilePath].");
        }
    }
}
