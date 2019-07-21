package cn.edu.nju.context;

public interface State {
    public static final int STOP = 0;
    public static final int START = 1; /*每一条rule的checker*/
    public static final int TRACTION = 2;
    public static final int COAST = 3;
    public static final int BRAKE = 4;
}
