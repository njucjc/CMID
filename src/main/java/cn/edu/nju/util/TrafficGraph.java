package cn.edu.nju.util;

import java.util.*;

/**
 * Created by njucjc at 2020/2/3
 */
public class TrafficGraph {

    private static final Map<String, List<String>> trafficGraph = new HashMap<>();

    private static final Map<String, String> opposite = new HashMap<>();

    private static final Map<String, Integer> nodeType = new HashMap<>();

    private static final Map<String, List<String>> path = new HashMap<>();

    private static final Map<String, Integer> toInt = new HashMap<>();

    private static final Class getCurrentClass() {

        return new Object() {
            public Class getClassForStatic() {
                return this.getClass();
            }
        }.getClassForStatic();
    }

    static {
        List<String> neiList = FileHelper.readStreamFile(getCurrentClass().getResourceAsStream("nei.txt"));
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

        List<String> oppoList = FileHelper.readStreamFile(getCurrentClass().getResourceAsStream("oppo.txt"));
        for (String line : oppoList) {
            String [] pat = line.split(",");
            opposite.put(pat[0], pat[2]);
        }

        List<String> pathlist = FileHelper.readStreamFile(getCurrentClass().getResourceAsStream("path.txt"));
        for (String line : pathlist) {
            String [] pat = line.split(":");
            path.put(pat[0], Arrays.asList(pat[1].split(",")));
        }

    }

    public static String getOppo(String code) {
        return opposite.get(code);
    }

    public static List<String> getPath(String v, String w, int k) {
        return path.get(v + "_" + w + "_" + k);
    }

    public static List<String> getPath2(String v, String w, int k) {
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
        List<String> res = new ArrayList<>();
        System.out.println(nodeType.keySet().size());
        int i = 0;
        for (String w : nodeType.keySet()) {
            for (String v : nodeType.keySet()) {
                i++;
                for (int k = 1; k <= 3; k++) {
                    List<String> path = getPath2(w, v, k);
                    if (path != null) {
                        String str = w + "_" + v + "_"+ k + ":";
                        for (String p : path) {
                            str += p + ',';
                        }
                        str = str.substring(0, str.length() - 1);
                        System.out.println(i+" " + str);
                        res.add(str);
                        break;
                    }
                }
            }
        }

        FileHelper.writeFile("path.txt", res);

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
