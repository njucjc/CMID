package cn.edu.nju.builder;

import cn.edu.nju.checker.Checker;
import cn.edu.nju.checker.EccChecker;
import cn.edu.nju.checker.PccChecker;
import cn.edu.nju.context.Context;
import cn.edu.nju.context.ContextRepoService;
import cn.edu.nju.context.ContextStaticRepo;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.BatchScheduler;
import cn.edu.nju.scheduler.Scheduler;
import cn.edu.nju.util.LogFileHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by njucjc on 2017/10/23.
 */
public class CheckerBuilder {
    public static final int ECC_TYPE = 0;
    public static final int PCC_TYPE = 1;    /*每一条rule的checker*/

    private List<Checker> checkerList;

    /*调度checker的策略*/
    private Scheduler scheduler;

    /*用于获取context，从文件中读入或从server读入*/
    private ContextRepoService contextRepoService;

    /*所有pattern*/
    private Map<String, Pattern> patternMap;

    private Map<String, Checker> checkerMap;

    private int checkType = ECC_TYPE;

    public CheckerBuilder(String configFilePath) {
        parseConfigFile(configFilePath);
    }

    private void parseConfigFile(String configFilePath) {
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream(configFilePath);
            properties.load(fis);
            fis.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        //check type
        String technique = properties.getProperty("technique");
        if("PCC".equals(technique)) {
            this.checkType = PCC_TYPE;
        } else if("ECC".equals(technique)){
            this.checkType = ECC_TYPE;
        } else {
            assert false:"[DEBUG] Checking technique error: " + technique;
        }

        //pattern
        String patternFilePath = properties.getProperty("patternFilePath");
        parsePatternFile(patternFilePath);

        //rule
        String ruleFilePath = properties.getProperty("ruleFilePath");
        parseRuleFile(ruleFilePath);

        //context
        String contextFilePath = properties.getProperty("contextFilePath");
        this.contextRepoService = new ContextStaticRepo(contextFilePath);

        //log
        String logFilePath = properties.getProperty("logFilePath");
        LogFileHelper.initLogger(logFilePath);


        //schedule
        String schedule = properties.getProperty("schedule");
        if(schedule.matches("[0-9]+")) {
            this.scheduler = new BatchScheduler(Integer.parseInt(schedule));
            System.out.println("[DEBUG] " + schedule);
        } else {
            assert false:"[DEBUG] Schedule error: " + schedule;
        }


    }

    private void parsePatternFile(String patternFilePath) {
        this.patternMap = new HashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(patternFilePath);

            NodeList patternList = document.getElementsByTagName("pattern");
            System.out.println("[DEBUG] There is " + patternList.getLength() + " patterns");
            for (int i = 0; i < patternList.getLength(); i++) {
                Node patNode = patternList.item(i);
                NodeList childNodes = patNode.getChildNodes();

                String id = childNodes.item(1).getTextContent();
                long freshness = Long.parseLong(childNodes.item(3).getTextContent());
                String category = childNodes.item(5).getTextContent();
                String subject = childNodes.item(7).getTextContent();
                String predicate = childNodes.item(9).getTextContent();
                String object = childNodes.item(11).getTextContent();
                String site = childNodes.item(13).getTextContent();

                patternMap.put(id, new Pattern(id, freshness, category, subject, predicate, object, site));
            }

            for(String key : patternMap.keySet()) {
                System.out.println("[DEBUG] " + patternMap.get(key));
            }
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseRuleFile(String ruleFilePath) {
        this.checkerList = new ArrayList<>();
        this.checkerMap = new HashMap<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(ruleFilePath);

            NodeList ruleList = document.getElementsByTagName("rule");
            System.out.println("[DEBUG] There is " + ruleList.getLength() + " rules here.");

            for(int i = 0; i < ruleList.getLength(); i++){
                STNode treeHead = new STNode();

                Node ruleNode = ruleList.item(i);

                Node idNode = ruleNode.getChildNodes().item(1);
                Node formulaNode = ruleNode.getChildNodes().item(3);

                Map<String,STNode> stMap = new HashMap<>();
                buildSyntaxTree(formulaNode.getChildNodes(), treeHead, stMap);

                assert treeHead.hasChildNodes():"[DEBUG] Create syntax tree failed !";

                STNode root = (STNode)treeHead.getFirstChild();
                root.setParentTreeNode(null);

                Checker checker;
                if(checkType == PCC_TYPE) {
                    checker = new PccChecker(idNode.getTextContent(), root, this.patternMap, stMap);
                }
                else{
                    checker = new EccChecker(idNode.getTextContent(), root, this.patternMap, stMap);
                }

                checkerList.add(checker);
                for(String key : stMap.keySet()) {
                    checkerMap.put(stMap.get(key).getContextSetName(), checker);
                }

                System.out.println("[DEBUG] " + checker.getName());
                checker.printSyntaxTree();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buildSyntaxTree(NodeList list, STNode root, Map<String,STNode> stMap) {
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
                    case "not":
                        stNode = new STNode(nodeName, STNode.NOT_NODE);
                        break;
                    case "implies":
                        stNode = new STNode(nodeName, STNode.IMPLIES_NODE);
                        break;
                    case "bfunction":
                        stNode = new STNode(e.getAttribute("name"), STNode.BFUNC_NODE);
                        break;
                    default:
                        assert false : "[DEBUG] Syntax tree node type error!";
                        break;
                }

                buildSyntaxTree(e.getChildNodes(), stNode, stMap);
                root.addChildeNode(stNode);
            }
        }
    }

    public void run() {
        Context context;
        try {
            long startTime = System.nanoTime();
            while ( (context =  contextRepoService.getContext()) != null) {
                doContextChange(context);
            }
            long endTime = System.nanoTime(); //获取结束时间
            int incCount = 0;
            int checkTimes = 0;
            for(Checker checker : checkerList) {
                incCount += checker.getInc();
                checkTimes += checker.getCheckTimes();
                LogFileHelper.getLogger().info(checker.getName() + ": " + checker.getInc() + " times" );
            }
            LogFileHelper.getLogger().info("Total INC: " + incCount + " times");
            LogFileHelper.getLogger().info("Total check: " + checkTimes + " times");
            LogFileHelper.getLogger().info("run time： " + (endTime - startTime) / 1000000 + " ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doContextChange(Context context) {
        Set<String> timeSet = new TreeSet<>();
        //按升序收集过期时间
        for(String key : patternMap.keySet()) {
            Pattern pattern = patternMap.get(key);
            timeSet.addAll(pattern.getOutOfDateTimes(context.getTimestamp()));
        }

        //按时间顺序删除context
        for(String timestamp : timeSet) {
            for(String key : patternMap.keySet()) {
                Pattern pattern = patternMap.get(key);
                pattern.deleteFirstByTime(timestamp);
                Checker checker = checkerMap.get(pattern.getId());
                checker.delete(pattern.getId(), timestamp);
            }
            scheduler.update();
            if(scheduler.schedule()) {
                doCheck();
            }
        }

        //在相关的pattern里添加context
        System.out.println("[DEBUG] '+' " + context.toString());
        for(String key : patternMap.keySet()) {
            Pattern pattern = patternMap.get(key);
            if(pattern.isBelong(context)) {
                pattern.addContext(context);
                Checker checker = checkerMap.get(pattern.getId());
                checker.add(pattern.getId(),context);
            }
        }
        scheduler.update();
        if(scheduler.schedule()) {
            doCheck();
        }
    }

    private void doCheck() {
        for(Checker checker : checkerList) {
            boolean value = checker.doCheck();
//            System.out.println("[DEBUG] " + checker.getName() + " CCT: ");
//            checker.printCCT();
            if(value) {
                System.out.println("[rule] " + checker.getName() + ": Pass!");
            }
            else {
                System.out.println("[rule] " + checker.getName() + ": Failed!");
            }
        }
        System.out.println("============================================================================================");
    }

    public static void main(String[] args) {
        CheckerBuilder checkerBuilder = new CheckerBuilder("resource/config.properties");
    }


}
