package cn.edu.nju;

import cn.edu.nju.builder.CheckerBuilder;

public class Main  {




    public static void main(String[] args) {
        if (args.length == 1) {
            CheckerBuilder checkerBuilder = new CheckerBuilder(args[0]);
            checkerBuilder.run();
            checkerBuilder.shutdown();
        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }

    }
}
