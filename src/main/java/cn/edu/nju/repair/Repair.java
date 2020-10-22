package cn.edu.nju.repair;

import cn.edu.nju.Main;
import cn.edu.nju.util.ConfigHelper;
import cn.edu.nju.util.FileHelper;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.TrafficGraph;

import java.util.*;

/**
 * Created by njucjc at 2020/2/7
 */
public class Repair {

    public static void repairStep0(Properties properties) {
        String dataPath = properties.getProperty("dataFilePath");
        List<String> dataList = FileHelper.readFile(dataPath);
        List<String> incList = FileHelper.readFile(properties.getProperty("logFilePath"));

        Set<Integer> oppoSet = new HashSet<>();
        Map<Integer, String> missMap = new HashMap<>();
        for (String inc : incList) {
            String [] elem = inc.split(" ");
            if (!elem[0].startsWith("rule") || elem[0].endsWith(":")) {
                break;
            }
            int index1 = Integer.parseInt(elem[1].split("_")[1]);
            int index2 = Integer.parseInt(elem[2].split("_")[1]);

            oppoSet.add(index1);
            oppoSet.add(index2);

            if (elem[0].equals("rule_01")) {
                List<String> path = TrafficGraph.getPath(TrafficGraph.getOppo(dataList.get(index1).split(",")[0]),
                                                         TrafficGraph.getOppo(dataList.get(index2).split(",")[0]),
                                                      2);
                missMap.put(index1, path.get(1));
            }
        }

        // init datalist and status
        for (int i = 0; i < dataList.size();i++) {
            if (oppoSet.contains(i)) {
                String code = TrafficGraph.getOppo(dataList.get(i).split(",")[0]);
                int type = TrafficGraph.getNodeType(code);
                dataList.set(i, code + "," + type);
            }
        }
        List<String> res = new ArrayList<>();

        for (int i = 0; i < dataList.size();i++) {
            res.add(dataList.get(i));
            if (missMap.keySet().contains(i)) {
                String code = missMap.get(i);
                int type = TrafficGraph.getNodeType(code);
                res.add(code + "," + type);
            }
        }

        FileHelper.writeFile(properties.getProperty("repairedFilePath"), res);
    }

    public static void repairStep1(Properties properties) {
        String dataPath = properties.getProperty("dataFilePath");
        List<String> dataList = FileHelper.readFile(dataPath);
        List<String> incList = FileHelper.readFile(properties.getProperty("logFilePath"));
        Set<Integer> redundantSet = new HashSet<>();
        for (String inc : incList) {
            String [] elem = inc.split(" ");
            if (!elem[0].startsWith("rule") || elem[0].endsWith(":")) {
                break;
            }

            int index= Integer.parseInt(elem[2].split("_")[1]);
            redundantSet.add(index);
        }

        List<String> res = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            if (!redundantSet.contains(i)) {
                res.add(dataList.get(i));
            }
        }

        FileHelper.writeFile(properties.getProperty("repairedFilePath"), res);
    }

    public static void repairStep2(Properties properties) {
        String dataPath = properties.getProperty("dataFilePath");
        List<String> dataList = FileHelper.readFile(dataPath);
        List<String> incList = FileHelper.readFile(properties.getProperty("logFilePath"));

        Map<Integer, List<String>> missMap = new HashMap<>();
        for (String inc : incList) {
            String[] elem = inc.split(" ");
            if (!elem[0].startsWith("rule") || elem[0].endsWith(":")) {
                break;
            }

            int index1 = Integer.parseInt(elem[1].split("_")[1]);
            int index2 = Integer.parseInt(elem[2].split("_")[1]);

            if ("rule_03".equals(elem[0])) { // 添加1个
                List<String> path = TrafficGraph.getPath(dataList.get(index1).split(",")[0],
                        dataList.get(index2).split(",")[0], 2);
                List<String> p = new ArrayList<>();
                p.add(path.get(1));
                missMap.put(index1, p);
            }
            else if ("rule_04".equals(elem[0])){ // 添加2个
                List<String> path = TrafficGraph.getPath(dataList.get(index1).split(",")[0],
                        dataList.get(index2).split(",")[0], 3);
                List<String> p1 = new ArrayList<>();
                p1.add(path.get(1));
                if (index2 - index1 == 1) {
                    p1.add(path.get(2));
                    missMap.put(index1, p1);
                }
                else {
                    List<String> p2 = new ArrayList<>();
                    p2.add(path.get(2));
                    missMap.put(index1, p1);
                    missMap.put(index2 - 1, p2);
                }
            }
        }

        List<String> res = new ArrayList<>();
        for (int i = 0; i < dataList.size();i++) {
            res.add(dataList.get(i));
            if (missMap.keySet().contains(i)) {
                for (String c : missMap.get(i)) {
                    int type = TrafficGraph.getNodeType(c);
                    res.add(c + "," + type);
                }
            }
        }

        FileHelper.writeFile(properties.getProperty("repairedFilePath"), res);
    }


    public static void repairStep3(Properties properties) {
        String dataPath = properties.getProperty("dataFilePath");
        List<String> dataList = FileHelper.readFile(dataPath);
        List<String> incList = FileHelper.readFile(properties.getProperty("logFilePath"));

        Set<Integer> deleteSet = new HashSet<>();
        for (String inc : incList) {
            String[] elem = inc.split(" ");
            if (!elem[0].startsWith("rule") || elem[0].endsWith(":")) {
                break;
            }

            int index = Integer.parseInt(elem[elem.length - 1].split("_")[1]);
            deleteSet.add(index);
        }
        List<String> res = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            if (!deleteSet.contains(i)) {
                res.add(dataList.get(i));
            }
        }
        FileHelper.writeFile(properties.getProperty("repairedFilePath"), res);
    }

    private static List<String> deleteCycle(List<String> dataList, List<Integer> status) {
        Map<String, List<Integer>> hexMap = new HashMap<>();
        for (int i = 0; i < dataList.size(); i++) {
            String key = dataList.get(i).split(",")[0];
            if (hexMap.containsKey(key)) {
                hexMap.get(key).add(i);
            }
            else {
                List<Integer> tmp = new ArrayList<>();
                tmp.add(i);
                hexMap.put(key, tmp);
            }
        }

        Set<Integer> deleteSet = new HashSet<>();
        for (String key : hexMap.keySet()) {
            List<Integer> idxList = hexMap.get(key);
            int start = idxList.get(0);
            int end = idxList.get(idxList.size() - 1);

            if (end - start > 1) {
                for (int i = start + 1; i <= end; i++) {
                    deleteSet.add(i);
                }
            }
        }

        List<String> res = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            if (deleteSet.contains(i) && status.get(i) != 0) {
                continue;
            }
            res.add(dataList.get(i));
        }
        return res;
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            Properties properties = ConfigHelper.getConfig(args[0]);
            Main.check(args[0]);

            System.out.println("[INFO] 开始一致性修复......");
            long start = System.nanoTime();
            int step = Integer.parseInt(properties.getProperty("step"));
            if (step == 0) {
                repairStep0(properties);
            }
            else if (step == 1) {
                repairStep1(properties);
            }
            else if (step == 2) {
                repairStep2(properties);
            }
            else if (step == 3) {
                repairStep3(properties);
            }

            long end = System.nanoTime();
            System.out.println("[INFO] 一致性修复结束......");
            LogFileHelper.getLogger().info("一致性修复成功", true);
            LogFileHelper.getLogger().info("Repair time: " + (end - start) / 1000000 + " ms", true);
        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }
    }
}
