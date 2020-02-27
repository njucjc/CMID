package cn.edu.nju.node;

/**
 * Created by njucjc on 2017/10/7.
 */
public interface NodeType {
    public static final int NOT_NODE = 0;
    public static final int AND_NODE = 1;
    public static final int IMPLIES_NODE = 2;
    public static final int UNIVERSAL_NODE = 3;
    public static final int EXISTENTIAL_NODE = 4;
    public static final int BFUNC_NODE = 5;
    public static final int EMPTY_NODE = 6;
    public static final int OR_NODE = 7;

    public static final int BEFORE = 8;
    public static final int GATE = 9;
    public static final int EQUAL = 10;
    public static final int CONN = 11;
    public static final int OPPO = 12;
    public static final int NEXT = 13;

    int getNodeType();
    void setNodeType(int nodeType);
}
