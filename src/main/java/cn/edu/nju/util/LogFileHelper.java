package cn.edu.nju.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.*;

/**
 * Created by njucjc on 2017/10/13.
 */
public class LogFileHelper {
    private LogFileHelper() {}
    private static Logger LOGGER = Logger.getLogger(LogFileHelper.class.getName());
    public static void  initLogger(String logFilePath) {
        try{
            //Creating  and fileHandler
            Handler fileHandler  = new FileHandler(logFilePath);

            //Assigning handlers to LOGGER object
            Handler [] handlers = LOGGER.getHandlers();
            if (handlers != null) {
                for (Handler handler : handlers) {
                    handler.close();
                    LOGGER.removeHandler(handler);
                }
            }
            LOGGER.addHandler(fileHandler);

            //Setting levels to handlers and LOGGER
            fileHandler.setLevel(Level.ALL);
            LOGGER.setLevel(Level.ALL);

            fileHandler.setFormatter(new MyFormatter());

        }catch(IOException exception){
            LOGGER.log(Level.SEVERE, "Error occur in FileHandler.", exception);
        }
    }

    public synchronized static Logger getLogger() {
        return LOGGER;
    }

    public static void main(String  args[]) {
        getLogger().info("Hello World.");
        LOGGER.info("123");
    }

}

class MyFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        return /*"[" + record.getLevel() +  "] " + */
                record.getMessage() + "\n";
    }
}
