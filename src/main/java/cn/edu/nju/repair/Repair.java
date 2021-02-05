package cn.edu.nju.repair;

import cn.edu.nju.checker.*;
import cn.edu.nju.node.Param;
import cn.edu.nju.node.STNode;
import cn.edu.nju.scheduler.Scheduler;
import cn.edu.nju.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import cn.edu.nju.pattern.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by njucjc at 2020/2/7
 */
public class Repair {

    private static Map<String, Pattern> patternMap;

    protected static List<Checker> checkerList;

    protected static Map<String, Checker> checkerMap;

    public static List<String> repairStep0(List<String> dataList, List<String> incList) {


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

        return res;
    }

    public static List<String> repairStep1(List<String> dataList, List<String> incList) {
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

        return res;
    }

    public static List<String> repairStep2(List<String> dataList, List<String> incList) {

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

        return res;
    }


    public static List<String> repairStep3(List<String> dataList, List<String> incList, String incPath) {

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
        return res;
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

    private static boolean comparePath(String path1, String path2) {
        List<String> p1 = FileHelper.readFile(path1);
        List<String> p2 = FileHelper.readFile(path2);

        if (p1.size() != p2.size()) {
            return false;
        }
        else {
            for(int i = 0; i < p1.size(); ++i) {
                if (!p1.get(i).equals(p2.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void parsePatternFile(String patternFilePath) {
        patternMap = new ConcurrentHashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(patternFilePath);

            NodeList patternList = document.getElementsByTagName("pattern");
            System.out.println("[INFO] FixPattern文件为" + patternFilePath + "，总共" + patternList.getLength() + "个patterns");
            for (int i = 0; i < patternList.getLength(); i++) {
                Node patNode = patternList.item(i);
                NodeList childNodes = patNode.getChildNodes();

                Map<String, Boolean> member = new HashMap<>();

                String id = null;
                member.put("id", false);

                int freshness = 0;
                member.put("freshness", false);

                String category = null;
                member.put("category", false);

                String subject = null;
                member.put("subject", false);

                String predicate = null;
                member.put("predicate", false);

                String object = null;
                member.put("object", false);

                String site = null;
                member.put("site", false);

                for(int j = 0; j < childNodes.getLength(); j++) {
                    if (childNodes.item(j).getNodeName().startsWith("#")) {
                        continue;
                    }
                    switch (childNodes.item(j).getNodeName()) {
                        case "id":
                            member.put("id", true);
                            id = childNodes.item(j).getTextContent();
                            break;
                        case "freshness": case "timestamp":
                            try {
                                member.put("freshness", true);
                                freshness = Integer.parseInt(childNodes.item(j).getTextContent());
                            } catch (NumberFormatException e) {
                                System.out.println("[INFO] 配置文件解析失败：FixPattern文件中的freshness配置值" + childNodes.item(j).getTextContent() + "无效");
                                System.exit(1);
                            }
                            break;
                        case "category":
                            member.put("category", true);
                            category = childNodes.item(j).getTextContent();
                            break;
                        case "subject":
                            member.put("subject", true);
                            subject = childNodes.item(j).getTextContent();
                            break;
                        case "predicate":
                            member.put("predicate", true);
                            predicate = childNodes.item(j).getTextContent();
                            break;
                        case "object":
                            member.put("object", true);
                            object = childNodes.item(j).getTextContent();
                            break;
                        case "site":
                            member.put("site", true);
                            site = childNodes.item(j).getTextContent();
                            break;
                        default:
                            System.out.println("[INFO] 配置文件解析失败：FixPattern文件" + patternFilePath + "存在非法的pattern标识符" + childNodes.item(j).getNodeName());
                            System.exit(1);
                    }
                }

                for(String key : member.keySet()) {
                    if (!member.get(key)) {
                        System.out.println("[INFO] 配置文件解析失败：FixPattern文件" + patternFilePath + "缺少pattern标识符" + key);
                        System.exit(1);
                    }
                }



                patternMap.put(id, new Pattern(id, freshness, category, subject, predicate, object, site));
            }

            /* for(String key : patternMap.keySet()) {
                System.out.println("[DEBUG] " + patternMap.get(key));
            } */

        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[INFO] 配置文件解析失败：FixPattern文件" + patternFilePath + "不存在");
            System.exit(1);
        }

        if (patternMap.isEmpty()) {
            System.out.println("[INFO]  配置文件解析失败：FixPattern文件" + patternFilePath + "没有pattern");
            System.exit(1);
        }
    }

    private static void parseRuleFile(String ruleFilePath) {
        checkerList = new CopyOnWriteArrayList<>();
        checkerMap = new ConcurrentHashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(ruleFilePath);

            NodeList ruleList = document.getElementsByTagName("rule");
            System.out.println("[INFO] Rule文件为" + ruleFilePath + "，总共" + ruleList.getLength() + "条rules");

            for(int i = 0; i < ruleList.getLength(); i++){
                STNode treeHead = new STNode();

                Node ruleNode = ruleList.item(i);

                Node idNode = ruleNode.getChildNodes().item(1);
                Node formulaNode = ruleNode.getChildNodes().item(3);

                Map<String,STNode> stMap = new HashMap<>();
                buildSyntaxTree(formulaNode.getChildNodes(), treeHead, stMap, ruleFilePath);

                assert treeHead.hasChildNodes():"[DEBUG] Create syntax tree failed !";

                STNode root = (STNode)treeHead.getFirstChild();
                root.setParentTreeNode(null);

                Checker checker = new PccChecker(idNode.getTextContent(), root, patternMap, stMap);


                checkerList.add(checker);
                for(String key : stMap.keySet()) {
                    checkerMap.put(stMap.get(key).getContextSetName(), checker);
                }

                //System.out.println("[DEBUG] " + checker.getName());
                //checker.printSyntaxTree();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("[INFO] 配置文件解析失败：Rule文件" + ruleFilePath + "不存在");
            System.exit(1);
        }

        if (checkerList.isEmpty()) {
            System.out.println("[INFO] 配置文件解析失败：Rule文件" + ruleFilePath + "没有rule");
            System.exit(1);
        }
    }

    private static void buildSyntaxTree(NodeList list, STNode root, Map<String,STNode> stMap, String ruleFilePath) {
        for(int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE && !list.item(i).getNodeName().equals("param")) {
                Element e = (Element)list.item(i);
                STNode stNode = null;
                String nodeName = e.getNodeName();

                switch (nodeName) {
                    case "forall":
                        stNode = new STNode(nodeName, STNode.UNIVERSAL_NODE, e.getAttribute("in"));
                        stMap.put(e.getAttribute("in"), stNode);
                        break;
                    case "exists":
                        stNode = new STNode(nodeName, STNode.EXISTENTIAL_NODE, e.getAttribute("in"));
                        stMap.put(e.getAttribute("in"),stNode);
                        break;
                    case "and":
                        stNode = new STNode(nodeName, STNode.AND_NODE);
                        break;
                    case "or":
                        stNode = new STNode(nodeName, STNode.OR_NODE);
                        break;
                    case "not":
                        stNode = new STNode(nodeName, STNode.NOT_NODE);
                        break;
                    case "implies":
                        stNode = new STNode(nodeName, STNode.IMPLIES_NODE);
                        break;
                    case "bfunction":
                        stNode = new STNode(e.getAttribute("name"), STNode.BFUNC_NODE, buildParam(e));
                        break;
                    default:
                        System.out.println("[INFO] 配置文件解析失败：Rule文件" + ruleFilePath +  "存在非法的一致性规则标识符" + nodeName);
                        System.exit(1);
                        break;
                }

                buildSyntaxTree(e.getChildNodes(), stNode, stMap, ruleFilePath);
                root.addChildeNode(stNode);
            }
        }
    }

    private static List<Param> buildParam(Element e) {
        List<Param> res = new ArrayList<>();
        NodeList list = e.getChildNodes();
        for (int i = 1; i < list.getLength(); i+= 2) {
            Element element = (Element) list.item(i);
            int pos = Integer.parseInt(element.getAttribute("pos"));
            String op = element.getAttribute("op");
            String defaultValue = element.getAttribute("default_value");
            res.add(new Param(pos, op, defaultValue));
        }
        return res;
    }

    private static void dataValidityJudgment(String filePath, String changeHandlerType) {
        if (changeHandlerType.contains("dynamic")) return;
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fr);

            String change;
            while ((change = bufferedReader.readLine()) != null) {
                String [] elems = change.split(",");
                if (changeHandlerType.contains("time-based")) {
                    if (elems.length != 2) {
                        System.out.println("[INFO] 配置文件解析错误：数据文件" + filePath + "格式错误");
                        System.exit(1);
                    }
                }
                else {
                    if (elems.length != 6) {
                        System.out.println("[INFO] 配置文件解析错误：数据文件" + filePath + "格式错误");
                        System.exit(1);
                    }

                    if (!"+".equals(elems[0]) && !"-".equals(elems[0])) {
                        System.out.println("[INFO] 配置文件解析错误：数据文件" + filePath + "存在非法的数据操作类型" + elems[0]);
                        System.exit(1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length == 1) {
            boolean isParted = Interaction.init();
            System.out.println("[INFO] 开始修复过程");
            Properties properties = ConfigHelper.getConfig(args[0]);

            String dataPath = properties.getProperty("dataFilePath");
            if (dataPath == null) {
                System.out.println("[INFO] 配置文件解析失败：缺少dataFilePath配置项");
                System.exit(1);
            }
            else if(!FileHelper.isFileExists(dataPath)) {
                System.out.println("[INFO] 配置文件解析失败：数据文件" + dataPath + "不存在");
                System.exit(1);
            }

            String changeHandlerType = properties.getProperty("changeHandlerType");
            dataValidityJudgment(dataPath, changeHandlerType);

            List<String> dataList = FileHelper.readFile(dataPath);

            String incPath = properties.getProperty("incFilePath");
            if (incPath == null) {
                System.out.println("[INFO] 配置文件解析失败：缺少incFilePath配置项");
                System.exit(1);
            }
            else if(!FileHelper.isFileExists(incPath)) {
                System.out.println("[INFO] 配置文件解析失败：INC文件" + incPath + "不存在");
                System.exit(1);
            }

            // check inc list
            List<String> incList = FileHelper.readFile(incPath);
            for (String inc : incList) {
                String[] elem = inc.split(" ");
                if (!elem[0].startsWith("rule") || elem[0].endsWith(":")) {
                    break;
                }

                if (elem.length != 5 || !elem[0].equals("rule_05")) {
                    System.out.println("[INFO] 配置文件解析失败：INC文件" + incPath + "内容格式不正确，无法正常解析");
                    System.exit(1);
                }
            }

           //fix pattern
            String fixPatternFilePath = properties.getProperty("fixPatternFilePath");
            if (fixPatternFilePath == null) {
                System.out.println("[INFO] 配置文件解析失败：缺少fixPatternFilePath配置项");
                System.exit(1);
            }
            else if (!FileHelper.isFileExists(fixPatternFilePath)) {
                System.out.println("[INFO] 配置文件解析失败：FixPattern文件" + fixPatternFilePath + "不存在");
                System.exit(1);
            }
            parsePatternFile(fixPatternFilePath);
            //fix rule
            String fixRuleFilePath = properties.getProperty("fixRuleFilePath");
            if (fixRuleFilePath == null) {
                System.out.println("[INFO] 配置文件解析失败：缺少fixRuleFilePath配置项");
                System.exit(1);
            }
            else if (!FileHelper.isFileExists(fixRuleFilePath)) {
                System.out.println("[INFO] 配置文件解析失败：FixRule文件" + fixRuleFilePath + "不存在");
                System.exit(1);
            }
            parseRuleFile(fixRuleFilePath);

            Interaction.say("开始一致性修复", isParted);

            List<String> res;
            res = repairStep3(dataList, incList, incPath);

            System.out.println("[INFO] 一致性修复结束");

            Interaction.say("创建并输出一致性修复结果文件", isParted);
            FileHelper.writeFile(properties.getProperty("fixedFilePath"), res);
            System.out.println("[INFO] 成功创建并输出一致性修复结果到文件");

            String fixedFilePath = properties.getProperty("fixedFilePath");
            String truePath = properties.getProperty("trueFilePath");


            Interaction.say("进行修复结果分析", isParted);
            if (comparePath(fixedFilePath, truePath)) {
                System.out.println("[INFO] 对比结束，结果为：");
                System.out.println("[INFO] 修复结果与理想结果一致，修复成功");
            }
            else {
                System.out.println("[INFO] 对比结束，结果为：");
                System.out.println("[INFO] 修复结果与理想结果不一致，修复失败");
            }
        }
        else {
            System.out.println("Usage: java Main [configFilePath].");
        }
    }
}
