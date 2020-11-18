package cn.edu.nju;

import cn.edu.nju.builder.CheckerBuilder;
import cn.edu.nju.util.FileHelper;

public class Main  {




    public static void main(String[] args) {
        if (args.length == 1) {
            CheckerBuilder builder = new CheckerBuilder();
            if (!FileHelper.isFileExists(args[0])) {
                System.out.println("[INFO] 配置文件不存在: " + args[0]);
                System.exit(1);
            }

            String msg = builder.parseConfigFile(args[0]);
            if (msg == null) {
                Thread checkerThread = new Thread(builder);
                checkerThread.setPriority(Thread.MAX_PRIORITY);
                checkerThread.start();
            }
            else {
                System.exit(1);
            }
        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }

    }
}
