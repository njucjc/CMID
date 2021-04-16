package cn.edu.nju.client;

import cn.edu.nju.context.ContextParser;
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
    int port;

    private List<String> contextStrList;
    private List<Long> sleepTime;


    public Client(String contextFilePath, String ip, int port)  {
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
                long diff = TimestampHelper.timestampDiff(ContextParser.jsonToContext(0, line).getTimestamp(), ContextParser.jsonToContext(0, lastLine).getTimestamp());
                sleepTime.add(diff);
                lastLine = line;
            }
            sleepTime.add(1L);

            this.port = port;
            this.socket = new DatagramSocket();
            this.address = InetAddress.getByName(ip);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        long sleepMillis = 0;
        long totalTime = 0;
        long startTime = System.nanoTime();
        long endTime = 0;
        for (int i = 0; i < contextStrList.size(); i++){

            System.out.print("Send " + i + " at " + TimestampHelper.getCurrentTimestamp() + ", sleep:" + sleepMillis + " ms\r");
            sleepMillis = sleepTime.get(i);
            sendMsg(contextStrList.get(i));
            endTime = System.nanoTime();

            long diff = (endTime - startTime) - totalTime * 1000000;
            totalTime += sleepMillis;

//            assert diff >= 0 : "Time error !";

            sleepMillis = (sleepMillis - diff / 1000000) > 0 ? (sleepMillis - diff / 1000000) : 0;

            try {
                Thread.sleep(sleepMillis);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        endTime = System.nanoTime();
        System.out.println("[INFO] 数据文件发送结束，耗时" + (endTime - startTime) / 1000000 + " ms");

        for (int i = 0; i < 10000; i++) {
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
                if (!new File(args[0]).exists()) {
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
            String ip = properties.getProperty("ip");
            int port = Integer.parseInt(properties.getProperty("port"));

            Thread client = new Thread(new Client(dataFilePath, ip, port));



            client.setPriority(Thread.MAX_PRIORITY);
            client.start();
        }
        else {
            System.out.println("Usage: java Client [configFilePath].");
        }
    }
}
