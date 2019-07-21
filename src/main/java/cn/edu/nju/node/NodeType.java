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

    public static final int ELECTRIC_RANGE = 13;
    public static final int VOLTAGE_RANGE = 14;
    public static final int ACC_RANG = 15;
    public static final int ACC_RATE_RANG = 16;
    public static final int MAX_SPEED = 17;
    public static final int MIN_SPEED = 18;
    public static final int TRANS_TO_BRAKE = 19;
    public static final int TRANS_TO_TRACTION = 20;

    int getNodeType();
    void setNodeType(int nodeType);
}
