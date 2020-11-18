package cn.edu.nju.util;

import java.io.*;
import java.util.List;

public class FileHelper {
    /**
     * 创建临时文件
     * @param prefix is 临时文件的前缀
     * @param suffix is 临时文件的后缀
     */
    public static String createTempFile(String prefix,String suffix) {
        File tempfile = null;
        try{
            tempfile = File.createTempFile(prefix,suffix);
        }catch(IOException e){
            e.printStackTrace();
        }


        if (tempfile == null) {
            return "";
        }
        else {
            return tempfile.getAbsolutePath().replaceAll("\\\\", "/");
        }
    }

    public static void copyFile( String source, String dest) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(source));
            BufferedWriter bw = new BufferedWriter(new FileWriter(dest));
            String line;
            while ((line = br.readLine()) != null) {
                bw.write(line.trim() + "\n");
            }

            br.close();

            bw.flush();
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean isFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
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
