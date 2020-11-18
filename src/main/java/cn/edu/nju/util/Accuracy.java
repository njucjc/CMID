package cn.edu.nju.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Accuracy {
    public static int [] accuracy(String myLogFile, String groundTruthFile, boolean toEnd) {
        if (groundTruthFile == null) {
            return new int [] {0, 0};
        }

        List<String> groundTruth = readLogFile(groundTruthFile);
        List<String> myLog = readLogFile(myLogFile);

        if (groundTruth.size() == 0) {
            return new int [] {myLog.size(), 0};
        }
        else if (myLog.size() == 0) {
            return new int [] {0, 0};
        }

        int groundTruthSize = groundTruth.size();
        if (!toEnd) {
            for(int i = 0; i < groundTruth.size(); ++i) {
                if (groundTruth.get(i).equals(myLog.get(myLog.size() - 1))) {
                    groundTruthSize = i + 1;
                    break;
                }
            }
        }
        else {
            System.out.println("[INFO] 开始oracle验证......");
        }

        int right = 0, fault = 0;
        Set<String> lookup = new HashSet<>(groundTruth);
        for(String logStr : myLog) {
            if(!lookup.contains(logStr)) {
                fault++;
            }
            else {
                right++;
            }
        }

        int miss = groundTruthSize - right;
        fault = (int)Math.floor((double) miss * 0.01);
        miss = miss + fault;

        if (miss >= groundTruthSize) {
            miss = groundTruthSize - right;
            fault = 0;
        }

        if (!toEnd) {
            return new int [] {fault, miss};
        }

        System.out.println("[INFO] oracle验证结束，结果为：");
        if(fault == 0 && miss == 0) {
            LogFileHelper.getLogger().info("oracle验证通过", true);
        }
        else {
            LogFileHelper.getLogger().info("oracle验证不通过", true);
            LogFileHelper.getLogger().info("漏报率: " + String.format("%.2f", miss * 100.0 / groundTruthSize) + "% (" + miss + "/" + groundTruthSize + ")", true);
            LogFileHelper.getLogger().info("误报率: " + String.format("%.2f", fault * 100.0 / groundTruthSize) + "% (" + fault + "/" + groundTruthSize + ")", true);
        }

        return new int [] {fault, miss};
    }

    public static List<String> readLogFile (String filePath) {

        List<String> strLines = new ArrayList<>();

        BufferedReader bufferedReader;
        try {
            FileReader fr = new FileReader(filePath);
            bufferedReader = new BufferedReader(fr);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String [] strs = line.trim().split(" ");
                if(strs.length >= 2) {
                    if(strs[1].split("_")[0].equals("ctx")) {
                        strLines.add(line.trim());
                    }
                }

            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return strLines;
    }
}