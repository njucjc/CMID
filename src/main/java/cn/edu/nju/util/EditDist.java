package cn.edu.nju.util;

import java.util.*;

public class EditDist {
    private static Map<String, Set<String>> map = new HashMap<>();
    static {
        map.put("rule", new HashSet<>(Arrays.asList("forall", "exists", "and", "not", "implies", "bfunction")));
        map.put("pattern", new HashSet<>(Arrays.asList("id", "freshness", "category", "subject", "predicate", "object", "site")));
    }
    public static String minEditDist(String fid, String type) {
        Set<String> keywords = map.get(type);

        for(String key : keywords) {
            if (key.startsWith(fid)) {
                return "，是否为：" + key;
            }
        }


        String ret = null;
        int dist = Integer.MAX_VALUE;

        for (String key : keywords) {
            int newDist = editDist(fid, key);
            if (newDist < dist) {
                dist = newDist;
                ret = key;
            }
        }
        if (dist <= fid.length() / 2) {
            return "，是否为：" + ret;
        }
        else {
            return "，可以为：" + keywords.toString();
        }
    }

    public static int editDist(String A, String B) {
        if (A == null && B == null) {
            return 0;
        }
        else if (A == null) {
            return B.length();
        }
        else if (B == null) {
            return A.length();
        }

        if(A.equals(B)) {
            return 0;
        }
        //dp[i][j]表示源串A位置i到目标串B位置j处最低需要操作的次数
        int[][] dp = new int[A.length() + 1][B.length() + 1];
        for(int i = 1;i <= A.length();i++)
            dp[i][0] = i;
        for(int j = 1;j <= B.length();j++)
            dp[0][j] = j;
        for(int i = 1;i <= A.length();i++) {
            for(int j = 1;j <= B.length();j++) {
                if(A.charAt(i - 1) == B.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1];
                else {
                    dp[i][j] = Math.min(dp[i - 1][j] + 1,
                            Math.min(dp[i][j - 1] + 1, dp[i - 1][j - 1] + 1));
                }
            }
        }
        return dp[A.length()][B.length()];
    }
}
