package cn.edu.nju.node;

/**
 * Created by njucjc on 2017/10/7.
 */
public class STNode extends TreeNode implements NodeType{
    private int nodeType;

    private String contextSetName;

    public STNode(String nodeName, int nodeType) {
        super(nodeName);
        synchronized (this) {
            this.nodeType = nodeType;
            this.contextSetName = "";
        }
    }

    public STNode(String nodeName, int nodeType, String contextSetName) {
        super(nodeName);
        synchronized (this) {
            this.nodeType = nodeType;
            this.contextSetName = contextSetName;
        }
    }

    public  STNode() {
        super("");
        synchronized (this) {
            this.nodeType = EMPTY_NODE;
            this.contextSetName = "";
        }
    }

    public synchronized String getContextSetName() {
        return contextSetName;
    }

    public synchronized void setContextSetName(String contextSetName) {
        this.contextSetName = contextSetName;
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
