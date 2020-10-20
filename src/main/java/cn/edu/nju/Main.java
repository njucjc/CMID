package cn.edu.nju;

import cn.edu.nju.builder.CheckerBuilder;
import cn.edu.nju.repair.Repair;
import cn.edu.nju.util.ChangeFileHelper;
import cn.edu.nju.util.ConfigHelper;

import java.util.Properties;

public class Main  {




    public static void main(String[] args) {
        if (args.length == 1) {
            check(args[0]);
        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }

    }

    public static void check(String path) {
        new CheckerBuilder(path).run();
    }
}
