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

    public static List<String> readStreamFile(InputStream path) {
        List<String> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(path));

            while (true) {
                String str = br.readLine();
                if (str == null) break;
                list.add(str);
            }
            br.close();
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

    public static boolean isFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static void createNewFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            //不存在
            try {
                file.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }
}