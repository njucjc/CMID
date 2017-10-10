package cn.edu.nju.parser;

import cn.edu.nju.checker.*;
import cn.edu.nju.model.node.STNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import cn.edu.nju.model.Context;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


/**
 * Created by njucjc on 2017/10/4.
 */
public class Parser {
    /* Checker list */
    private List<Checker> checkerList;

    private Map<String, Set<Context>> contextSets;


    public Parser(String xmlFilePath) {
        this.checkerList = new ArrayList<>();
        this.contextSets = new HashMap<>();
        parserXML(xmlFilePath);
    }

    private void parserXML(String xmlFilePath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(xmlFilePath);

            NodeList ruleList = document.getElementsByTagName("rule");
            System.out.println("There is " + ruleList.getLength() + " rules here.");

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
               // Checker checker = new EccChecker(idNode.getTextContent(), root, contextSets);
                Checker checker = new PccChecker(idNode.getTextContent(), root, contextSets, stMap);
                checkerList.add(checker);

//                System.out.println("[DEBUG] " + checker.getName());
//                checker.printSyntaxTree();
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
                        if(!contextSets.containsKey(e.getAttribute("in"))) {
                            contextSets.put(e.getAttribute("in"),new HashSet<>());
                        }
                        break;
                    case "exists":
                        stNode = new STNode(nodeName, STNode.EXISTENTIAL_NODE, e.getAttribute("in"));
                        stMap.put(e.getAttribute("in"),stNode);
                        if(!contextSets.containsKey(e.getAttribute("in"))) {
                            contextSets.put(e.getAttribute("in"),new HashSet<>());
                        }
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
                    case "bfunc":
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

    public void doContextChange(String change) {
        System.out.println("[DEBUG] Change: " + change);

        String [] s0 = change.split(",");
        assert s0.length == 6:"[DEBUG] Illegal change：" + change + ".";
        String [] s1 = s0[s0.length - 1].split("_");
        assert s1.length == 3:"[DEBUG] Illegal change：" + change + ".";

        String op = s0[0];
        String contextSetName = s0[1];
        int id = Integer.parseInt(s0[2]);
        String timestamp = s0[3];
        String plateNumber = s0[4];
        double longitude = Double.parseDouble(s1[0]);
        double latitude = Double.parseDouble(s1[1]);
        double speed = Double.parseDouble(s1[2]);

        Context context = new Context(id,timestamp, plateNumber, longitude, latitude, speed);

        for(Checker checker : checkerList) {
            checker.update(op, contextSetName, context);
        }

    }

    public void doCheck() {
        for(Checker checker : checkerList) {
            String links = checker.doCheck();
//            System.out.println("[DEBUG] CCT: ");
//            checker.printCCT();
            if("".equals(links)) {
                System.out.println("[rule] " + checker.getName() + ": Pass!");
            }
            else {
                System.out.println("[rule] " + checker.getName() + ": Failed!");
                String[] strs = links.split("#");
                for (String s : strs) {
                    System.out.println(s);
                }

            }
        }
        System.out.println("======================================");
    }


    public static void main(String[] args) {
        Parser checker = new Parser("resource/rules.xml");
        try {
            FileReader fr = new FileReader("resource/changes/00.txt");
            BufferedReader br = new BufferedReader(fr);
            String change;
            int scheduleCount = 0;

            long startTime = System.currentTimeMillis();

            while ((change = br.readLine()) != null) {
                scheduleCount++;
                checker.doContextChange(change);
                System.out.println("[INFO] " + "schedule number: " + scheduleCount);
                checker.doCheck();
            }

            long endTime = System.currentTimeMillis(); //获取结束时间
            System.out.println("[INFO] run time： " + (endTime - startTime) + "ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
