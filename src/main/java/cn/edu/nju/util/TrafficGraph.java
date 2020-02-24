package cn.edu.nju.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by njucjc at 2020/2/3
 */
public class TrafficGraph {

    private static final Map<String, List<String>> trafficGraph = new HashMap<>();

    private static final Map<String, String> opposite = new HashMap<>();

    private static final Map<String, Integer> nodeType = new HashMap<>();

    private static final Map<String, Integer> toInt = new HashMap<>();

    static {
        List<String> neiList = FileHelper.readFile("res/nei.txt");
        for (String line : neiList) {
            String [] pat = line.split(",");
            if (trafficGraph.containsKey(pat[0])) {
                trafficGraph.get(pat[0]).add(pat[2]);
            }
            else {
                List<String> l = new ArrayList<>();
                l.add(pat[2]);
                trafficGraph.put(pat[0], l);
            }

            nodeType.put(pat[0], Integer.parseInt(pat[1]));
            nodeType.put(pat[2], Integer.parseInt(pat[3]));
        }

        int i = 0;
        for (String key : nodeType.keySet()) {
            toInt.put(key, i);
            i++;
        }

        List<String> oppoList = FileHelper.readFile("res/oppo.txt");
        for (String line : oppoList) {
            String [] pat = line.split(",");
            opposite.put(pat[0], pat[2]);
        }

    }

    public static String getOppo(String code) {
        if (opposite.containsKey(code)) {
            return opposite.get(code);
        }
        else if (getNodeType(code) == 3){
            return code;
        }
        else
            return null;

    }

    public static List<String> getPath(String v, String w, int k) {
        Set<String> keySet = nodeType.keySet();
        if (!keySet.contains(v) || !keySet.contains(w)) {
            return null;
        }

        Map<String, Boolean> visited = new HashMap<>();
        for (String key : keySet) {
            visited.put(key, false);
        }
        List<String> path = new ArrayList<>();
        boolean ok = existPathLenK(visited, path, v, w, k);
        return ok ? path : null;
    }

    private static boolean existPathLenK(Map<String, Boolean> visited, List<String> path, String v, String w, int k) {
        visited.put(v, true);
        path.add(v);
        if (v.equals(w) && k == 0) {
            return true;
        }
        else if (k > 0) {
            if (trafficGraph.get(v) != null) {
                for (String p : trafficGraph.get(v)) {
                    if (k > 1 && nodeType.get(p) == 3) continue; //路径中间不能出现收费站
                    if (!visited.get(p) && existPathLenK(visited, path, p, w, k - 1))
                        return true;
                    visited.put(p, false);
                }
            }
        }
        path.remove(v);
        return false;
    }

    public static int getNodeType(String code) {
        if (nodeType.containsKey(code))
            return nodeType.get(code);
        else {
            System.out.println(code + " is not exist.");
            return -1;
        }
    }

    public static void main(String[] args) {
        List<String> path = getPath("3C2013", "3D2010", 1);
        System.out.println("Path 3C2013 to 3D2010:");
        for (String v : path) {
            System.out.println(v + " ");
        }

        System.out.println("Opposite of 3D0B15:");
        String oppo = getOppo("3D0B15");
        System.out.println(oppo);

    }

    public static int codeToInt(String code) {
        return toInt.get(code);
    }

    public static Map<String, List<String>> getTrafficGraph() {
        return trafficGraph;
    }

    public static Map<String, String> getOpposite() {
        return opposite;
    }
}
