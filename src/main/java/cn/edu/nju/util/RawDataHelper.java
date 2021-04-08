package cn.edu.nju.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RawDataHelper {
    public static List<String> updateTimestamp(List<String> records) {
        String lastTime = "2020-12-22 00:00:01";
        int index = 11;

        long cnt = 0;
        List<String> res = new ArrayList<>();
        for(int i = 0; i < records.size(); i++) {
            String [] e = records.get(i).split(",");
            if (!e[index].equals(lastTime)) {
                if (cnt > 30) {
                    System.out.println("Record " + i + " > 30");
                }
                lastTime = e[index];
                cnt = 0;
            }
            e[index] = TimestampHelper.getDateToString(TimestampHelper.getStringToDate(e[index]) + 10000 * cnt);
            res.add(String.join(",", e));
            cnt++;
        }
        return res;
    }

    public static List<String> addOrder(List<String> records) {
        int index = 1;
        Map<String, Integer> ips = new HashMap<>();
        List<String> res = new ArrayList<>();
        for (String record : records) {
            String[] e = record.split(",");
            if (!ips.containsKey(e[index])) {
                ips.put(e[index], 1);
            }
            int num = ips.get(e[index]);
            res.add(record + "," + num);
            ips.put(e[index], num + 1);
        }

        return res;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }
        List<String> records = FileHelper.readFile(args[0]);
        records = updateTimestamp(records);
        records = addOrder(records);
        FileHelper.writeFile(args[1], records);
    }
}
