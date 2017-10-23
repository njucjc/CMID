package cn.edu.nju.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by njucjc on 2017/10/13.
 */
public class LogFileHelper {
    /**
     * 将logStr添加到logFile的文件末尾
     * @param logFilePath
     * @param logStr
     */
    public static void writeLog(String logFilePath, String logStr) {
//        System.out.println(logStr);
        try {
            FileWriter writer = new FileWriter(logFilePath, true);
            writer.write(logStr );
            writer.write('\n');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createLogFile(String logFilePath) {
        File logFile = new File(logFilePath);
        try {
            if(logFile.exists()) {
                logFile.delete();
            }
            logFile.createNewFile();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
