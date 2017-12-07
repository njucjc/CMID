package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.node.TreeNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.util.BFuncHelper;
import cn.edu.nju.util.LinkHelper;
import cn.edu.nju.util.TimestampHelper;

import java.util.*;

/**
 * Created by njucjc on 2017/10/3.
 */
public abstract class Checker {
    /* Checker(rule) name*/
    protected String name;

    /* CCT root for constraint */
    protected CCTNode cctRoot;

    /*Syntax tree for constraint*/
    protected STNode stRoot;

    protected Map<String, Pattern> patternMap;

    private Set<String> incLinkSet;

    protected int checkTimes = 0;

    private Map<String, STNode> stMap; //记录与context set相关的语法树结点

    private Map<String, List<CCTNode>> cctMap; //记录与context set相关的CCT关键结点


    public Checker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap) {
        this.name = name;
        this.stRoot = stRoot;
        this.patternMap = patternMap;
        this.incLinkSet = new HashSet<>();
        this.checkTimes = 0;

        this.stMap = stMap;
        this.cctMap = new HashMap<>();

        //初始化
        for(String key : stMap.keySet()) {
            cctMap.put(key, new LinkedList<>());
        }

        //初始化CCT
        this.cctRoot = new CCTNode(stRoot.getNodeName(), stRoot.getNodeType());
        buildCCT(stRoot, this.cctRoot);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInc() {
        return incLinkSet.size();
    }

    protected boolean addIncLink(String link) {
        return incLinkSet.add(link);
    }

    protected Set<String> getIncLinkSet() {
        return incLinkSet;
    }

    /**
     * 做一次一致性检测
     * @return violated link
     */
    abstract public boolean doCheck();

    /**
     *
     * @param patternId
     * @param context
     * @return
     */
    public boolean add(String patternId, Context context) {
        if (!affected(patternId)) {
            return false;
        }
        List<CCTNode> criticalNodeList = cctMap.get(patternId);
        STNode stNode = stMap.get(patternId);
        Pattern pattern = patternMap.get(patternId);
        assert stNode.getNodeType() == STNode.EXISTENTIAL_NODE
                || stNode.getNodeType() == STNode.UNIVERSAL_NODE
                :"[DEBUG] Type Error.";
        for (CCTNode node : criticalNodeList) {
            //更新从关键节点到根节点的状态
            updateNodesToRoot(node);
            //创建一个以context相关联的新子树
            CCTNode newChild = new CCTNode(stNode.getFirstChild().getNodeName(),((STNode)(stNode.getFirstChild())).getNodeType(), context);
            buildCCT((STNode) stNode.getFirstChild(), newChild);
            //添加到本结点
            node.addChildeNode(newChild);
        }
        return true;
    }


    public boolean delete(String patternId, String timestamp) {
        if (!affected(patternId)) {
            return false;
        }
        List<CCTNode> criticalNodeList = cctMap.get(patternId);
        STNode stNode = stMap.get(patternId);
        Pattern pattern = patternMap.get(patternId);
        assert stNode.getNodeType() == STNode.EXISTENTIAL_NODE
                || stNode.getNodeType() == STNode.UNIVERSAL_NODE
                :"[DEBUG] Type Error.";

        for (CCTNode node : criticalNodeList) {
            //更新从关键节点到根节点的状态
            updateNodesToRoot(node);

            //TODO: 兼容动态检测接口
            //删除timestamp时刻过期结点，由于是按时间顺序删除，故只需看第一个结点
            List<TreeNode> childTreeNodes = node.getChildTreeNodes();
            Iterator<TreeNode> it = childTreeNodes.iterator();
            if (it.hasNext()) {//是否存在结点
                CCTNode child = (CCTNode)it.next(); //第一个结点
                if (TimestampHelper.timestampDiff(child.getContext().getTimestamp(), timestamp) == pattern.getFreshness()) {
                    removeCriticalNode((STNode) stNode.getFirstChild(), child);
                    it.remove();
                }
            }
        }
        return true;
    }



    /**
     * 根据语法树构造CCT
     * @param stRoot
     * @param cctRoot
     */
    protected void buildCCT(STNode stRoot, CCTNode cctRoot) {
        if (!stRoot.hasChildNodes()) {
            return ;
        }
        if(stRoot.getNodeType() == STNode.EXISTENTIAL_NODE || stRoot.getNodeType() == STNode.UNIVERSAL_NODE) {
            cctMap.get(stRoot.getContextSetName()).add(cctRoot); //add critical node information
            STNode stChild = (STNode) stRoot.getFirstChild();
            for(Context context : patternMap.get(stRoot.getContextSetName()).getContextList()) {
                //CCT结点创建默认为FC状态
                CCTNode cctChild = new CCTNode(stChild.getNodeName(), stChild.getNodeType(), context);
                buildCCT(stChild, cctChild);
                cctRoot.addChildeNode(cctChild);
            }
        }
        else {
            List<TreeNode> childNodes = stRoot.getChildTreeNodes();
            for (TreeNode n : childNodes) {
                STNode stChild = (STNode) n;
                CCTNode cctChild = new CCTNode(stChild.getNodeName(), stChild.getNodeType());
                buildCCT(stChild, cctChild);
                cctRoot.addChildeNode(cctChild);
            }
        }
    }


    private boolean affected(String contextSetName) {
        return stMap.containsKey(contextSetName);
    }


    /**
     * 从cctMap中删除cctRoot子树中相关的可能关键结点信息
     * @param stRoot
     * @param cctRoot
     */
    protected void removeCriticalNode(STNode stRoot, CCTNode cctRoot) {
        assert stRoot.getNodeType() == cctRoot.getNodeType()
                :"[DEBUG] Type error:" + stRoot.getNodeName() + " != " + cctRoot.getNodeName();
        if(!cctRoot.hasChildNodes()) {
            return;
        }
        if(stRoot.getNodeType() == STNode.UNIVERSAL_NODE || stRoot.getNodeType() == STNode.EXISTENTIAL_NODE) {
            cctMap.get(stRoot.getContextSetName()).remove(cctRoot);//删除相关信息
            STNode stChild = (STNode) stRoot.getFirstChild();

            //全称量词和存在量词的子节点数由其相关的context set大小决定
            List<Context> contextSet = patternMap.get(stRoot.getContextSetName()).getContextList();
            assert contextSet.size() == cctRoot.getChildTreeNodes().size():"[DEBUG] Size error.";
            for(int i = 0; i < contextSet.size(); i++) {
                removeCriticalNode(stChild, (CCTNode) cctRoot.getChildTreeNodes().get(i));
            }
        }
        else {
            List<TreeNode> childNodes = stRoot.getChildTreeNodes();
            assert childNodes.size() == cctRoot.getChildTreeNodes().size():"[DEBUG] Size error.";
            for (int i = 0; i < childNodes.size(); i++) {
                removeCriticalNode((STNode) childNodes.get(i), (CCTNode) cctRoot.getChildTreeNodes().get(i));
            }
        }

    }


    /**
     * 更新从关键结点到根结点路径上的所有结点状态信息
     * @param node
     */
    private void updateNodesToRoot(CCTNode node) {
        while(node != null) {
            node.setNodeStatus(CCTNode.PC_STATE); //更新为Partial checking
            node = (CCTNode) node.getParentTreeNode();
        }
    }

    /**
     * CCT求值函数，虽然结点带有状态，但是是最简单的实现，没用利用结点的历史状态信息
     * @param cctRoot
     * @param param
     * @return
     */
    protected boolean evaluation(CCTNode cctRoot, List<Context> param) {
        if(cctRoot.getContext() != null) {
            param.add(cctRoot.getContext());
        }

        boolean value = false;
        if(!cctRoot.hasChildNodes()) {//叶子结点只可能是全称量词、存在量词或bfunc
            if(cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
                value = true;
            }
            else if(cctRoot.getNodeType() == CCTNode.EXISTENTIAL_NODE) {
                value = false;
            }
            else {
                int size = param.size();
                assert size >= 1:"[DEBUG] Param error";
                value = BFuncHelper.bfun(cctRoot.getNodeName(), param.get(size - 1), param.get(size >= 2 ? size - 2:size - 1));
            }
            //设置本结点布尔值
            cctRoot.setNodeValue(value);
            String link = "";
            for(Context context : param) {
                link = link + context.toString() + " ";
            }
            if(!"".equals(link)) {
                link = link.substring(0, link.length() - 1);
            }
            //生成link
            cctRoot.setLink(link);
        }
        else {
            if(cctRoot.getNodeType() == CCTNode.NOT_NODE) {
                value = !evaluation((CCTNode) cctRoot.getFirstChild(), param);
                cctRoot.setNodeValue(value); //更新结点值
                cctRoot.setLink(((CCTNode) cctRoot.getFirstChild()).getLink()); //更新link信息
            }
            else if(cctRoot.getNodeType() == CCTNode.AND_NODE){
                CCTNode leftChild = (CCTNode) cctRoot.getChildTreeNodes().get(0);
                CCTNode rightChild = (CCTNode) cctRoot.getChildTreeNodes().get(1);
                boolean leftValue = evaluation(leftChild, param);
                boolean rightValue = evaluation(rightChild, param);

                value = leftValue && rightValue;
                cctRoot.setNodeValue(value); //更新结点值

                //更新link信息
                if(leftValue && !rightValue) {
                    cctRoot.setLink(rightChild.getLink());
                }
                else if(!leftValue && rightValue) {
                    cctRoot.setLink(leftChild.getLink());
                }
                else {
                    cctRoot.setLink(LinkHelper.linkCartesian(leftChild.getLink(), rightChild.getLink()));
                }
            }
            else if(cctRoot.getNodeType() == CCTNode.IMPLIES_NODE) {
                CCTNode leftChild = (CCTNode) cctRoot.getChildTreeNodes().get(0);
                CCTNode rightChild = (CCTNode) cctRoot.getChildTreeNodes().get(1);
                boolean leftValue = evaluation(leftChild, param);
                boolean rightValue = evaluation(rightChild, param);

                value =  !leftValue || (leftValue && rightValue);
                cctRoot.setNodeValue(value); //更新结点值
                //更新link信息
                if(value) {
                    cctRoot.setLink(LinkHelper.linkCartesian(leftChild.getLink(), rightChild.getLink()));
                }
                else {
                    cctRoot.setLink(rightChild.getLink());
                }
            }
            else if(cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
                List<TreeNode> childNodes = cctRoot.getChildTreeNodes();

                StringBuilder satisfiedLink = new StringBuilder();
                StringBuilder violatedLink = new StringBuilder();

                value = true;
                for (TreeNode n : childNodes) {
                    CCTNode child = (CCTNode)n;
                    boolean b = evaluation(child, param);
                    value = value && b;
                    if (b) {
                        if(value) {
                            satisfiedLink.append(child.getLink());
                            satisfiedLink.append("#");
                        }
                    }
                    else {
                        violatedLink.append(child.getLink());
                        violatedLink.append("#");
                    }

                }
                cctRoot.setNodeValue(value); //更新结点值
                if (!value) {
                    violatedLink.deleteCharAt(violatedLink.length() -1);
                    cctRoot.setLink(violatedLink.toString());
                }
                else {
                    satisfiedLink.deleteCharAt(satisfiedLink.length() - 1);
                    cctRoot.setLink(satisfiedLink.toString());
                }
            }
            else  if(cctRoot.getNodeType() == CCTNode.EXISTENTIAL_NODE) {
                List<TreeNode> childNodes = cctRoot.getChildTreeNodes();

                StringBuilder satisfiedLink = new StringBuilder();
                StringBuilder violatedLink = new StringBuilder();

                value = false;
                for (TreeNode n : childNodes) {
                    CCTNode child = (CCTNode)n;
                    boolean b = evaluation(child, param);
                    value = value || b;
                    if (b) {
                        satisfiedLink.append(child.getLink());
                        satisfiedLink.append("#");
                    }
                    else {
                        if(!value) {
                            violatedLink.append(child.getLink());
                            violatedLink.append("#");
                        }
                    }
                }
                cctRoot.setNodeValue(value);
                if (value) {
                    satisfiedLink.deleteCharAt(satisfiedLink.length() - 1);
                    cctRoot.setLink(satisfiedLink.toString());
                }
                else {
                    violatedLink.deleteCharAt(violatedLink.length() -1);
                    cctRoot.setLink(violatedLink.toString());
                }
            }
            else {
                assert false:"[DEBUG] Illegal CCT node: " + cctRoot.getNodeName() + ".";
            }
        }

        //本结点计算完毕就将结点状态更新为NC（无需重算状态）
        cctRoot.setNodeStatus(CCTNode.NC_STATE);
        //返回上一层
        if (cctRoot.getContext() != null) {
            param.remove(param.size() - 1);
        }

        return value;
    }


    public void printSyntaxTree() {
        BFSOrder(stRoot);
    }

    public void printCCT() {
        BFSOrder(cctRoot);
    }

    private void BFSOrder(TreeNode root) {
        if (root == null) {
            return ;
        }
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        TreeNode last = root;
        TreeNode nlast = root;
        while (!queue.isEmpty()) {
            TreeNode t = queue.poll();
            System.out.print(t.getNodeName());
            if(t instanceof CCTNode) {
                Context c = ((CCTNode) t).getContext();
                if(c != null) {
                    System.out.print("(" + c.getPlateNumber() + ") ");
                }
                else {
                    System.out.print(" ");
                }
            }
            else {
                System.out.print(" ");
            }

            for(TreeNode n : t.getChildTreeNodes()) {
                queue.add(n);
                nlast = n;
            }

            if(t == last) {
                System.out.println();
                last = nlast;
            }
        }

    }

    public int getCheckTimes() {
        return checkTimes;
    }
}
