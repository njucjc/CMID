package cn.edu.nju.util;

import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by njucjc on 2017/10/13.
 */
public class LogFile {
    /**
     * 将logStr添加到logFile的文件末尾
     * @param logFilePath
     * @param logStr
     */
    public static void writeLog(String logFilePath, String logStr) {
        try {
            FileWriter writer = new FileWriter(logFilePath, true);
            writer.write(logStr);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
