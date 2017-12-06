package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.node.TreeNode;
import cn.edu.nju.pattern.Pattern;

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

    public Checker(String name, STNode stRoot, Map<String, Pattern> patternMap) {
        this.name = name;
        this.stRoot = stRoot;
        this.patternMap = patternMap;
        this.incLinkSet = new HashSet<>();
        this.checkTimes = 0;
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
     *
     * @param patternId
     * @param context
     * @return
     */
    abstract public boolean add(String patternId, Context context);

    abstract public boolean delete(String patternId, String timestamp);

    /**
     *
     * @return violated link
     */
    abstract public boolean doCheck();

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
