package cn.edu.nju.server;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import cn.edu.nju.checker.Checker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextParser;
import cn.edu.nju.util.ChangeHelper;
import cn.edu.nju.util.LogFileHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.*;

/**
 * Created by njucjc on 2017/10/29.
 */


public class Server extends AbstractCheckerBuilder implements Runnable{
    private DatagramSocket serverSocket;
    private boolean running;
    private byte [] buf = new byte[256];

    public Server(String configFilePath)  {
        super(configFilePath);
        if (!changeHandlerType.contains("dynamic")) {
            System.out.println("[INFO] 非动态检测，请将changeHandlerType配置为dynamic");
            System.exit(1);
        }
        System.out.println("[INFO] 服务器开始启动");

        try {
            serverSocket = new DatagramSocket(port);
        }catch(IOException e) {
            e.printStackTrace();
        }
        System.out.println("[INFO] 成功绑定" + port + "端口");
    }

    @Override
    public void run() {
        running = true;

        int count = 0;

        long timeSum = 0;

        Map<Long, List<String>> deleteChanges = new TreeMap<>();

        System.out.println("[INFO] 服务器启动完毕，开始等待接收数据");
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                String msg = new String(packet.getData(),0, packet.getLength());
                if ("exit".equals(msg)) {
                    running = false;
                    break;
                }

                assert count != -1:"counter overflow.";

                Context context = ContextParser.jsonToContext(count, msg);
                List<String> changes = ChangeHelper.toChanges(context, deleteChanges, patternList);

                long start = System.nanoTime();

                for(String chg : changes) {
                    changeHandler.doContextChange(chg);
                }

                long end = System.nanoTime();
                timeSum += (end - start);

                count++;

                int inc = 0;
                for (Checker checker : checkerList) {
                    inc += checker.getInc();
                }

                System.out.print( "[INFO] INC: "+ inc + "\tTotal Checking time: " + (timeSum / 1000000)  +" ms\r");

            }

            long start = System.nanoTime();
            Iterator<Map.Entry<Long, List<String>>> it = deleteChanges.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, List<String>> entry = it.next();
                long key = entry.getKey();
                for (String chg : deleteChanges.get(key)) {
                    changeHandler.doContextChange(chg);
                }
            }
            long end = System.nanoTime();
            timeSum += (end - start);

        }
        catch (IOException e) {
            e.printStackTrace();
        }

        changeHandler.shutdown();
        int inc = 0;
        for (Checker checker : checkerList) {
            inc += checker.getInc();
        }

        System.out.println();
        LogFileHelper.getLogger().info("[INFO] Total INC: " + inc, true);
        LogFileHelper.getLogger().info("[INFO] Total checking time: " +  timeSum / 1000000 + " ms", true);

        serverSocket.close();

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
