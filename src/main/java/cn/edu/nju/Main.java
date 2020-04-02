package cn.edu.nju;

import cn.edu.nju.builder.CheckerBuilder;
import cn.edu.nju.repair.Repair;
import cn.edu.nju.util.ChangeFileHelper;
import cn.edu.nju.util.ConfigHelper;

import java.util.Properties;

public class Main  {




    public static void main(String[] args) {
        if (args.length == 1) {
            Properties properties = ConfigHelper.getConfig(args[0]);

            ChangeFileHelper changeFileHelper = new ChangeFileHelper(properties.getProperty("patternFilePath"));
            changeFileHelper.parseChangeFile(properties.getProperty("dataFilePath"), properties.getProperty("changeFilePath"));

            check(args[0]);
            Repair.repairStep0(properties);
            modifyConfig(properties, args[0], 0);

            changeFileHelper = new ChangeFileHelper(properties.getProperty("patternFilePath"));
            changeFileHelper.parseChangeFile(properties.getProperty("dataFilePath"), properties.getProperty("changeFilePath"));

            check(args[0]);
            Repair.repairStep2(properties);
            modifyConfig(properties, args[0], 1);

            changeFileHelper = new ChangeFileHelper(properties.getProperty("patternFilePath"));
            changeFileHelper.parseChangeFile(properties.getProperty("dataFilePath"), properties.getProperty("changeFilePath"));

            check(args[0]);
            Repair.repairStep3(properties);
            modifyConfig(properties, args[0], 2);

        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }

    }

    private static void check(String path) {
        new CheckerBuilder(path).run();
    }

    private static void modifyConfig(Properties properties, String path,int step) {
        String rule = properties.getProperty("ruleFilePath");
        String pattern = properties.getProperty("patternFilePath");
        String data = properties.getProperty("dataFilePath");
        String change = properties.getProperty("changeFilePath");
        String log = properties.getProperty("logFilePath");

        String old_regex = "_" + step;
        String new_regex = "_" + ((step + 1) % 3);
        properties.setProperty("ruleFilePath", rule.split(old_regex)[0] + new_regex + ".xml");
        properties.setProperty("patternFilePath", pattern.split(old_regex)[0] + new_regex + ".xml");

        properties.setProperty("dataFilePath", data.split(old_regex)[0] + new_regex + ".txt");
        properties.setProperty("changeFilePath", change.split(old_regex)[0] + new_regex + ".txt");

        properties.setProperty("logFilePath", log.split(old_regex)[0] + new_regex + ".log");

        ConfigHelper.setConfig(properties, path);
    }
}
