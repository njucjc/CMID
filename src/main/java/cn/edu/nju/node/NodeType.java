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

    public static final int SAME = 7;
    public static final int SZ_SPD_CLOSE = 8;
    public static final int SZ_LOC_CLOSE = 9;
    public static final int SZ_LOC_DIST = 10;
    public static final int SZ_LOC_DIST_NEQ = 11;
    public static final int SZ_LOC_RANGE = 12;
    public static final int SZ_ELECTRIC_RANGE = 13;
    public static final int SZ_VOLTAGE_RANGE = 14;
    public static final int SZ_POWER_RANGE = 15;
    public static final int SZ_SPEED_RANGE = 16;
    public static final int SZ_ACC_RANGE = 17;

    int getNodeType();
    void setNodeType(int nodeType);
}
