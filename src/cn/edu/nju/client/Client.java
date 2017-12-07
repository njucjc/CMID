package cn.edu.nju.client;

import cn.edu.nju.util.TimestampHelper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Client {
    private DataOutputStream toServer;
    private DataInputStream fromServer;

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

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Client begins to start.....");
        try {
            Socket socket = new Socket("localhost", 8000);


            fromServer = new DataInputStream(socket.getInputStream());

            toServer = new DataOutputStream(socket.getOutputStream());

            long sleepMillis = 0;
            long sleepNanos = 1;
            long t1 = System.nanoTime();
            for (int i = 0; i < contextStrList.size(); i++){
                try {
                    Thread.sleep(sleepMillis, (int) sleepNanos);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long startTime = System.nanoTime();
                sleepMillis = sleepTime.get(i);
                toServer.writeUTF(contextStrList.get(i));
                toServer.flush();
                long endTime = System.nanoTime();

                sleepNanos = endTime - startTime;
                long temp = (sleepMillis * 1000000 - sleepNanos);
                sleepMillis = temp / 1000000;
                sleepNanos = temp % 1000000;
            }
            long t2 = System.nanoTime();
            System.out.println("Total send time： " + (t2 - t1) / 1000000 + " ms");

            toServer.writeUTF("exit");
            toServer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(args[0]);
            properties.load(fis);
            fis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        new Client(properties.getProperty("contextFilePath"));
    }
}
