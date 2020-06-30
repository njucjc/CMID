package cn.edu.nju;

import cn.edu.nju.builder.CheckerBuilder;
import cn.edu.nju.repair.Repair;

public class Main  {




    public static void main(String[] args) {
        if (args.length == 1) {
            check(args[0]);
        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }

    }

    private static void check(String path) {
        new CheckerBuilder(path).run();
    }
}
