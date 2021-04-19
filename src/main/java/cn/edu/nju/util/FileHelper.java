package cn.edu.nju.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by njucjc at 2020/2/7
 */
public class FileHelper {
    public static List<String> readFile(String path) {
        List<String> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);

            while (true) {
                String str = br.readLine();
                if (str == null) break;
                list.add(str);
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void writeFile(String path, List<String> content) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileWriter fw = new FileWriter(file, true);

            for (String line : content) {
                fw.write(line + '\n');
            }

            fw.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}