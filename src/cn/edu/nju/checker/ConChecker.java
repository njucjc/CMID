package cn.edu.nju.checker;

import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;

import java.util.Map;

/**
 * Created by njucjc on 2017/12/27.
 */

public class ConChecker extends EccChecker{
    private int taskNum;

    public ConChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap, int taskNum) {
        super(name, stRoot, patternMap, stMap);
        this.taskNum = taskNum;
    }

    
}
