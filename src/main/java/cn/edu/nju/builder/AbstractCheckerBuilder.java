package cn.edu.nju.builder;

import cn.edu.nju.change.*;
import cn.edu.nju.checker.*;
import cn.edu.nju.memory.GPUContextMemory;
import cn.edu.nju.memory.GPUPatternMemory;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.scheduler.BatchScheduler;
import cn.edu.nju.scheduler.GEASchduler;
import cn.edu.nju.scheduler.Scheduler;
import cn.edu.nju.util.LogFileHelper;
import cn.edu.nju.util.PTXFileHelper;
import jcuda.driver.CUcontext;
import jcuda.driver.CUdevice;
import jcuda.driver.JCudaDriver;
import jcuda.utils.KernelLauncher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static jcuda.driver.JCudaDriver.*;

public abstract class AbstractCheckerBuilder {

    public static final int ECC_TYPE = 0;
    public static final int PCC_TYPE = 1; /*每一条rule的checker*/
    public static final int CON_TYPE = 2;
    public static final int GAIN_TYPE = 3;

    protected List<Checker> checkerList;

    /*调度checker的策略*/
    protected Scheduler scheduler;

    protected String dataFilePath; //context data

    protected String changeFilePath; //context change

    /*所有pattern*/
    protected Map<String, Pattern> patternMap;

    protected Map<String, Checker> checkerMap;

    private int checkType = ECC_TYPE;

    private int taskNum;

    private ExecutorService checkExecutorService;

    protected ChangeHandler changeHandler;

    protected String changeHandlerType;

    private KernelLauncher genTruthValue;

    private KernelLauncher genLinks;

    private KernelLauncher updatePattern;

    private CUcontext cuContext;

    private List<String> contexts;

    public AbstractCheckerBuilder(String configFilePath) {
        parseConfigFile(configFilePath);
    }

    private void parseConfigFile(String configFilePath) {
        //不要随意更换处理顺序
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
        } else if("Con-C".equals(technique)) {
            this.checkType = CON_TYPE;
        } else if("GAIN".equals(technique)) {
            this.checkType = GAIN_TYPE;
        } else {
            assert false:"[DEBUG] Checking technique error: " + technique;
        }

        //taskNum
        String taskNumStr = properties.getProperty("taskNum");
        if(taskNumStr.matches("[0-9]+")) {
            this.taskNum = Integer.parseInt(taskNumStr);
            if (taskNum == 0) {
                assert false:"[DEBUG] taskNum error: " + taskNumStr;
            }
            System.out.println("[DEBUG] " + taskNum);
        } else {
            assert false:"[DEBUG] taskNum error: " + taskNumStr;
        }

        this.checkExecutorService = Executors.newFixedThreadPool(taskNum);
        //pattern
        String patternFilePath = properties.getProperty("patternFilePath");
        parsePatternFile(patternFilePath);

        String cudaSourceFilePath = properties.getProperty("cudaSourceFilePath");
        //如果是GAIN需要初始化GPU内存

        //context file path
        this.dataFilePath = properties.getProperty("dataFilePath");
        this.changeFilePath = properties.getProperty("changeFilePath");

        if(this.checkType == GAIN_TYPE) {
            //开启异常捕获
            JCudaDriver.setExceptionsEnabled(true);

            //初始化设备
            cuInit(0);
            CUdevice device = new CUdevice();
            cuDeviceGet(device, 0);
            cuContext = new CUcontext();
            cuCtxCreate(cuContext, 0, device);

           // initGPUMemory();
            contexts = fileReader(this.dataFilePath);
            loadKernelFunction(cudaSourceFilePath);
        }

        //rule
        String ruleFilePath = properties.getProperty("ruleFilePath");
        parseRuleFile(ruleFilePath);


        //log
        String logFilePath = properties.getProperty("logFilePath");
        LogFileHelper.initLogger(logFilePath);


        //schedule
        String schedule = properties.getProperty("schedule");
        //change handler
        this.changeHandlerType = properties.getProperty("changeHandlerType");
        if(schedule.matches("[0-9]+")) {
            this.scheduler = new BatchScheduler(Integer.parseInt(schedule));
            System.out.println("[DEBUG] " + schedule);
        }
        else if ("GEAS".equals(schedule) && ("dynamic-change-based".equals(changeHandlerType) || "static-change-based".equals(changeHandlerType))) {
            this.scheduler = new GEASchduler(this.checkerList);
            System.out.println("[DEBUG] " + schedule);
        }
        else {
            assert false:"[DEBUG] Schedule error: " + schedule;
        }

        //change handler

        if("static-time-based".equals(changeHandlerType)) {
            this.changeHandler = new StaticTimebasedChangeHandler(patternMap, checkerMap, scheduler, checkerList);
        }
        else if("dynamic-time-based".equals(changeHandlerType)) {
            this.changeHandler = new DynamicTimebasedChangeHandler(patternMap, checkerMap, scheduler, checkerList);
        }
        else if("static-change-based".equals(changeHandlerType)) {
            this.changeHandler = new StaticChangebasedChangeHandler(patternMap, checkerMap, scheduler, checkerList);
        }
        else if("dynamic-change-based".equals(changeHandlerType)) {
            this.changeHandler = new DynamicChangebasedChangeHandler(patternMap, checkerMap, scheduler, checkerList);
        }


    }

    private void parsePatternFile(String patternFilePath) {
        this.patternMap = new ConcurrentHashMap<>();
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
        this.checkerList = new CopyOnWriteArrayList<>();
        this.checkerMap = new ConcurrentHashMap<>();
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

                Checker checker = null;
                if(checkType == PCC_TYPE) {
                    checker = new PccChecker(idNode.getTextContent(), root, this.patternMap, stMap);
                }
                else if (checkType == ECC_TYPE){
                    checker = new EccChecker(idNode.getTextContent(), root, this.patternMap, stMap);
                } else if(checkType == CON_TYPE){ //CON-C
                    checker = new ConChecker(idNode.getTextContent(), root, this.patternMap, stMap, taskNum, checkExecutorService);
                } else if(checkType == GAIN_TYPE) {
                    checker = new GAINChecker(idNode.getTextContent(), root, this.patternMap, stMap,
                            genTruthValue, genLinks, updatePattern, //kernel function
                            contexts, cuContext); //GPU memory
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

    protected List<String> fileReader(String filePath) {
        List<String> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fr);

            String change;
            while ((change = bufferedReader.readLine()) != null) {
                list.add(change);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
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

    private void loadKernelFunction(String cudaSourceFilePath) {
        String ptxFile = null;
        try {
            ptxFile = PTXFileHelper.preparePtxFile(cudaSourceFilePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.genTruthValue = KernelLauncher.load(ptxFile ,"gen_truth_value");
        this.genLinks = KernelLauncher.load(ptxFile , "gen_links");
        this.updatePattern = KernelLauncher.load(ptxFile , "update_pattern");
    }

    public void shutdown() {
        checkExecutorService.shutdown();
        if(checkType == GAIN_TYPE) {
            GPUPatternMemory.getInstance(patternMap.keySet()).free();
            GPUContextMemory.getInstance(contexts).free();
            for (Checker checker : checkerList) {
                checker.reset();
            }
        }
    }
}
