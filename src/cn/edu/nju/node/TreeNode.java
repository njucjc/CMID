package cn.edu.nju.node;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by njucjc on 2017/10/7.
 */
public class TreeNode {


    private String nodeName;
    private List<TreeNode> childTreeNodes;
    private TreeNode parentTreeNode;


    public TreeNode(String nodeName) {
        synchronized (this) {
            this.nodeName = nodeName;
            childTreeNodes = new CopyOnWriteArrayList<>();
        }
    }

    public TreeNode() {
        synchronized (this) {
            this.nodeName = "";
            childTreeNodes = new CopyOnWriteArrayList<>();
        }
    }



    public synchronized void addChildeNode(TreeNode child) {
        child.setParentTreeNode(this);
        childTreeNodes.add(child);
    }

    public synchronized boolean hasChildNodes() {
        return childTreeNodes != null && childTreeNodes.size() != 0;
    }

    public synchronized TreeNode getFirstChild() {
        if(!hasChildNodes()) {
            return null;
        }
        else {
            return childTreeNodes.get(0);
        }
    }

    public synchronized TreeNode getLastChild() {
        if(!hasChildNodes()) {
            return null;
        }
        else {
            return childTreeNodes.get(childTreeNodes.size() - 1);
        }
    }

    public synchronized String getNodeName() {
        return nodeName;
    }

    public synchronized void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public synchronized List<TreeNode> getChildTreeNodes() {
        return childTreeNodes;
    }

    public synchronized void setChildTreeNodes(List<TreeNode> childTreeNodes) {
        this.childTreeNodes = childTreeNodes;
    }

    public synchronized TreeNode getParentTreeNode() {
        return parentTreeNode;
    }

    public synchronized void setParentTreeNode(TreeNode parentTreeNode) {
        this.parentTreeNode = parentTreeNode;
    }
}
