package cn.edu.nju.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Accuracy {
    public static void main(String[] args) {
        if(args.length == 2) {
            Set<String> groundTruth = readLogFile(args[0]);
            Set<String> myLog = readLogFile(args[1]);

            int right = 0, fault = 0;
            for(String logStr : myLog) {
                if(!groundTruth.contains(logStr)) {
                    fault++;
                }
                else {
                    right++;
                }
            }

            int miss = groundTruth.size() - right;

            System.out.println("Miss Rate: " + miss * 100.0 / groundTruth.size() + "% (" + miss + "/" + groundTruth.size() + ")");
            System.out.println("Fault Rate: " + fault * 100.0 / groundTruth.size() + "% (" + fault + "/" + groundTruth.size() + ")");


        }
        else {
            System.out.println("Usage: java Accuracy groundTruth.log myLog.log");
        }
    }

    public static Set<String> readLogFile (String filePath) {

        Set<String> strLines = new HashSet<>();

        BufferedReader bufferedReader;
        try {
            FileReader fr = new FileReader(filePath);
            bufferedReader = new BufferedReader(fr);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String [] strs = line.split(" ");
                if(strs.length >= 2) {
                    if(strs[1].split("_")[0].equals("ctx")) {
                        strLines.add(line);
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
