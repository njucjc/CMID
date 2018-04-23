package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.memory.GPUContextMemory;
import cn.edu.nju.memory.GPUPatternMemory;
import cn.edu.nju.memory.GPURuleMemory;
import cn.edu.nju.node.NodeType;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import jcuda.utils.KernelLauncher;

import java.util.*;

public class GAINChecker extends Checker {

    private int stSize;

    private int [] branchSize;

    private STNode [] constraintNodes; //(an array for storing a reordered syntax tree)

    private List<Integer> cunits; //an array for storing the start of each c-unit

    private GPURuleMemory gpuRuleMemory;

    private KernelLauncher genTruthValue;

    private KernelLauncher genLinks;

    private KernelLauncher updatePattern;

    private GPUContextMemory gpuContextMemory;

    private GPUPatternMemory gpuPatternMemory;

    private Map<String, Integer> patternIdMap;


    public GAINChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap,
                       KernelLauncher genTruthValue, KernelLauncher genLinks, KernelLauncher updatePattern,
                       GPUContextMemory gpuContextMemory, GPUPatternMemory gpuPatternMemory) {
        super(name, stRoot, patternMap, stMap);
        this.stSize = computeSTSize(stRoot);

        this.branchSize = new int[stSize];

        //计算cunit以及为语法树重排序(前序遍历)
        this.constraintNodes = new STNode[stSize];
        this.cunits = new ArrayList<>();
        cunits.add(-1);
        split(stRoot);

        //将语法树信息拷贝到GPU
        initGPURuleMemory();

        this.patternIdMap = gpuPatternMemory.getIndexMap();

        this.genTruthValue = genTruthValue;
        this.genLinks = genLinks;
        this.updatePattern = updatePattern;

        this.gpuContextMemory = gpuContextMemory;
        this.gpuPatternMemory = gpuPatternMemory;
    }


    private void initGPURuleMemory() {
        int [] parent = new int[stSize];
        int [] leftChild = new int[stSize];
        int [] rightChild = new int[stSize];
        int [] nodeType = new int[stSize];
        int [] patternId = new int[stSize];

        for(int i = 0; i < stSize; i++) {
            STNode p = (STNode) constraintNodes[i].getParentTreeNode();
            STNode l = (STNode) constraintNodes[i].getFirstChild();
            STNode r = (STNode) constraintNodes[i].getLastChild();

            parent[i] = p != null ? p.getNodeNum() : -1;
            leftChild[i] = l != null ? l.getNodeNum() : -1;
            rightChild[i] = r != null ? r.getNodeNum() : -1;
            patternId[i] = -1;

            int type = constraintNodes[i].getNodeType();
            if(type != NodeType.BFUNC_NODE) {
                nodeType[i] = type;
                if(type == NodeType.UNIVERSAL_NODE || type == NodeType.EXISTENTIAL_NODE) {
                    patternId[i] = patternIdMap.get(constraintNodes[i].getContextSetName());
                }
            }
            else {
                String name = constraintNodes[i].getNodeName();
                if("sz_loc_range".equals(name)) {
                    nodeType[i] = NodeType.SZ_LOC_RANGE;
                }
                else if("same".equals(name)) {
                    nodeType[i] = NodeType.SAME;
                }
                else if("sz_loc_close".equals(name)) {
                    nodeType[i] = NodeType.SZ_LOC_CLOSE;
                }
                else if("sz_spd_close".equals(name)) {
                    nodeType[i] = NodeType.SZ_SPD_CLOSE;
                }
                else if("sz_loc_dist".equals(name)) {
                    nodeType[i] = NodeType.SZ_LOC_DIST;
                }
                else if("sz_loc_dist_neq".equals(name)) {
                    nodeType[i] = NodeType.SZ_LOC_DIST_NEQ;
                }
                else {
                    assert false:"BFunc type error.";
                }
            }

            this.gpuRuleMemory = new GPURuleMemory(stSize, parent, leftChild, rightChild, nodeType, patternId);
        }
    }



    @Override
    public boolean doCheck() {
        assert false:"Something is being to do.";
        return false;
    }

    @Override
    public synchronized boolean add(String patternId, Context context) {
      //  return super.add(patternId, context);
        if(!affected(patternId)) {
            return false;
        }
        assert false:"Something is being to do.";
        return false;
    }

    @Override
    public synchronized boolean delete(String patternId, String timestamp) {
        if(!affected(patternId)) {
            return false;
        }
        assert false:"Something is being to do.";
        return false;
    }

    private int computeSTSize(STNode root) {
        if(root == null) {
            return 0;
        }
        int type = root.getNodeType();
        if(type == STNode.UNIVERSAL_NODE || type == NodeType.EXISTENTIAL_NODE || type == STNode.NOT_NODE) {
            return 1 + computeSTSize((STNode) root.getFirstChild());
        }
        else if(type == STNode.AND_NODE || type == STNode.IMPLIES_NODE) { //not support 'OR' node type
            return 1 + computeSTSize((STNode) root.getFirstChild()) + computeSTSize((STNode) root.getLastChild());
        }
        else if(type == STNode.BFUNC_NODE) {
            return 1;
        }
        else {
            assert false:"Node type error, type:  " +  type;
            return 0;
        }

    }

    private void split(STNode root) {
        Queue<STNode> rootOfCunit = new LinkedList<>();
        rootOfCunit.offer(root);
        int [] currentNodeNum = new int[1];
        currentNodeNum[0] = this.stSize - 1;
        while(!rootOfCunit.isEmpty()) {
            STNode rootOfNextCunit = rootOfCunit.poll();
            cunits.add(currentNodeNum[0]);
            parseCunit(rootOfCunit, rootOfNextCunit, currentNodeNum);
        }
    }

    private void parseCunit(Queue<STNode> rootOfCunit, STNode node,int []currentNodeNum) {
        //假定非全称/存在量词的子树中不会有全称/存在量词节点
        if(node == null) {
            return ;
        }
        node.setNodeNum(currentNodeNum[0]);
        constraintNodes[currentNodeNum[0]--] = node;
        int type = node.getNodeType();
        if (type == STNode.UNIVERSAL_NODE || type == STNode.EXISTENTIAL_NODE) {
            rootOfCunit.offer((STNode) node.getFirstChild());
        }
        else {
            parseCunit(rootOfCunit, (STNode) node.getLastChild(), currentNodeNum);
            parseCunit(rootOfCunit, (STNode) node.getFirstChild(), currentNodeNum);
        }
    }

    private int computeCCopyNum(int cunit) {
        STNode node = constraintNodes[cunit];
        int ccopyNum = 1;
        while(node != null) {
            int type = node.getNodeType();
            if(type == STNode.UNIVERSAL_NODE || type == STNode.EXISTENTIAL_NODE) {
                ccopyNum *= patternMap.get(node.getContextSetName()).getContextList().size();
            }
            node = (STNode) node.getParentTreeNode();
        }
        return ccopyNum;
    }

    private int computeRTTBranchSize(STNode root) {
        assert root != null:"root is null.";
        int type = root.getNodeType();
        int size = 0;
        if(type == STNode.UNIVERSAL_NODE || type == STNode.EXISTENTIAL_NODE) {
            size = 1 + patternMap.get(root.getContextSetName()).getContextList().size() * computeRTTBranchSize((STNode) root.getFirstChild());
        }
        else if(type == STNode.NOT_NODE) {
            size = 1 + computeRTTBranchSize((STNode)root.getFirstChild());
        }
        else if(type == STNode.AND_NODE || type == STNode.IMPLIES_NODE) {
            size = 1 + computeRTTBranchSize((STNode)root.getFirstChild()) + computeRTTBranchSize((STNode)root.getLastChild());
        }
        else if(type == STNode.BFUNC_NODE){
            size = 1;
        }
        else {
            assert false:"Type error.";
        }
        branchSize[root.getNodeNum()] = size;
        return size;
    }

    @Override
    public void reset() {
        this.gpuRuleMemory.free();
        super.reset();
    }
}
