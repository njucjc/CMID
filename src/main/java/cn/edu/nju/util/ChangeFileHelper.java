package cn.edu.nju.util;

import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextStaticRepo;
import cn.edu.nju.pattern.Pattern;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by njucjc at 2018/1/22
 */
public class ChangeFileHelper {
    private List<Pattern> patternList = new ArrayList<>();
    private Map<String, List<String>> contextChangMap = new TreeMap<>();
    private ContextStaticRepo contextStaticRepo;

    public ChangeFileHelper(String patternXmlPath) {
        parsePatternXml(patternXmlPath);
    }

    private void parsePatternXml(String patternXmlPath) {
        SAXReader reader = new SAXReader();
        try {
            // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
            Document document = reader.read(patternXmlPath);
            // 通过document对象获取根节点patterns
            Element patterns = document.getRootElement();
            // 通过element对象的elementIterator方法获取迭代器
            Iterator it = patterns.elementIterator();
            // 遍历迭代器，获取根节点中的信息
            while (it.hasNext()) {
                Element pattern = (Element) it.next();
                Iterator itt = pattern.elementIterator();
                String[] attr = new String[7];
                int i = 0;
                while (itt.hasNext()) {
                    Element patternChild = (Element) itt.next();
                  //  System.out.println("name：" + patternChild.getName() + " value：" + patternChild.getStringValue());
                    attr[i++] = patternChild.getStringValue();
                }
                Pattern p = new Pattern(attr[0], Long.parseLong(attr[1]), attr[2], attr[3], attr[4], attr[5], attr[6]);
                patternList.add(p);
            }
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void parseChangeFile(String changeFilePath) {
        this.contextStaticRepo = new ContextStaticRepo(changeFilePath);
        File file = new File(changeFilePath.split("\\.")[0] + "_change.txt");

        FileWriter writer = null;
        try {
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            writer = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Context context;
        try {
            while ((context = contextStaticRepo.getContext()) != null) {
                String currentTimestamp = context.getTimestamp();

//                Set<String> keySet = contextChangMap.keySet();
//                String [] keyArray = new String[keySet.size()];
//                keySet.toArray(keyArray);
//                Arrays.sort(keyArray);
                Iterator<Map.Entry<String, List<String>>> it = contextChangMap.entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<String, List<String>> entry = it.next();
                    String timestamp = entry.getKey();
                    if(TimestampHelper.timestampCmp(timestamp, currentTimestamp) < 0) {
                        for(String change : contextChangMap.get(timestamp)) {
                            writer.write(change);
                            writer.write('\n');
                        }
                        it.remove();
                    }
                }

                String str = context.allForString();
                for(Pattern pattern: patternList) {
                    if(pattern.isBelong(context)) {
                        writer.write("+," + pattern.getId() + "," + str);
                        writer.write('\n');
                        String key = TimestampHelper.plusMillis(currentTimestamp, pattern.getFreshness());
                        context.setTimestamp(key);
                        if(!contextChangMap.containsKey(key)) {
                            contextChangMap.put(key, new ArrayList<>());
                        }
                        contextChangMap.get(key).add("-," + pattern.getId() + "," + context.allForString());
                    }
                    context.setTimestamp(currentTimestamp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Set<String> keySet = contextChangMap.keySet();
//        String [] keyArray = new String[keySet.size()];
//        keySet.toArray(keyArray);
//        Arrays.sort(keyArray);

        try {
            Iterator<Map.Entry<String, List<String>>> it = contextChangMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, List<String>> entry = it.next();
                String key = entry.getKey();
                for (String change : contextChangMap.get(key)) {
                    writer.write(change);
                    writer.write('\n');
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    public static void main(String[] args) {
        if(args.length == 2) {
            ChangeFileHelper changeFileHelper = new ChangeFileHelper(args[0]);
            changeFileHelper.parseChangeFile(args[1]);
        }
        else {
            System.out.println("Args Error.");
        }
//        ChangeFileHelper changeFileHelper = new ChangeFileHelper("resource/consistency_patterns.xml");
//        changeFileHelper.parseChangeFile("resource/changes/00_small.txt");
    }
}
