package cn.edu.nju.node;

import cn.edu.nju.context.Context;

/**
 * Created by njucjc on 2017/10/3.
 */
public class CCTNode extends TreeNode implements NodeType, NodeStatus{

    /* Subtree value */
    private boolean nodeValue;

    /* Context assignment */
    private Context context;

    /* Tree node state */
    private int nodeStatus;

    private int nodeType;

    private String link;


    public CCTNode(String nodeName, int nodeType, Context context) {
            super(nodeName);
            synchronized (this) {
                this.nodeValue = false;
                this.context = context;
                this.nodeStatus = CCTNode.EC_STATE;
                this.nodeType = nodeType;
                this.link = "";
            }
    }

    public CCTNode(String nodeName, int nodeType) {
        super(nodeName);
        synchronized (this) {
            this.nodeValue = false;
            this.context = null;
            this.nodeStatus = CCTNode.EC_STATE;
            this.nodeType = nodeType;
            this.link = "";
        }
    }

    public synchronized boolean getNodeValue() {
        return nodeValue;
    }

    public synchronized Context getContext() {
        return context;
    }

    public synchronized void setNodeValue(boolean nodeValue) {
        this.nodeValue = nodeValue;
    }

    public synchronized void setContext(Context context) {
        this.context = context;
    }

    public synchronized String getLink() {
        return link;
    }

    public synchronized void setLink(String link) {
        this.link = link;
    }

    @Override
    public synchronized int getNodeStatus() {
        return nodeStatus;
    }

    @Override
    public synchronized void setNodeStatus(int nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    @Override
    public synchronized int getNodeType() {
        return nodeType;
    }

    @Override
    public synchronized void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }
}
