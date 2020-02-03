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

    static {
        List<String> neiList = readFile("res/nei.txt");
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
        }

        List<String> oppoList = readFile("res/oppo.txt");
        for (String line : oppoList) {
            String [] pat = line.split(",");
            opposite.put(pat[0], pat[2]);
        }

    }

    private static List<String> readFile(String path) {
        List<String> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);

            while (true) {
                String str = br.readLine();
                if (str == null) break;
                list.add(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getOppo(String code) {
        return opposite.get(code);
    }

    public static List<String> getPath(String v, String w, int k) {
        Set<String> keySet = trafficGraph.keySet();
        if (!keySet.contains(v) || !keySet.contains(w)) {
            return null;
        }

        Map<String, Boolean> visited = new HashMap<>();
        for (String key : keySet) {
            visited.put(key, false);
        }
        List<String> path = new ArrayList<>();
        existPathLenK(visited, path, v, w, k);
        return path;
    }

    public static boolean existPathLenK(Map<String, Boolean> visited, List<String> path, String v, String w, int k) {
        visited.put(v, true);
        path.add(v);
        if (v.equals(w) && k == 0) {
            return true;
        }
        else if (k > 0) {
            for (String p : trafficGraph.get(v)) {
                if (!visited.get(p) && existPathLenK(visited, path, p, w, k - 1)) return true;
                visited.put(p, false);
            }
        }
        path.remove(v);
        return false;
    }

    public static void main(String[] args) {
        List<String> path = getPath("3D0B15", "3C650C", 5);
        System.out.println("Path 3D0B15 to 3C650C:");
        for (String v : path) {
            System.out.println(v + " ");
        }

        System.out.println("Opposite of 3D0B15:");
        String oppo = getOppo("3D0B15");
        System.out.println(oppo);

    }
}
