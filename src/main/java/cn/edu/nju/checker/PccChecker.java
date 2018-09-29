package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.util.LinkHelper;
import cn.edu.nju.util.LogFileHelper;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by njucjc on 2017/10/7.
 */
public class PccChecker extends Checker{

    public PccChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap) {
        super(name, stRoot, patternMap, stMap);
    }

    public  PccChecker(Checker checker) {
        super(checker);
    }
    /**
     *
     * @return violated link
     */
    @Override
    public synchronized boolean doCheck() {
        long start = System.nanoTime();

        checkTimes++;
        List<Context> param = new CopyOnWriteArrayList<>();
        evaluation(cctRoot, param); //PCC计算

        boolean value = true;
        if (!cctRoot.getNodeValue()) {
            for(String link : LinkHelper.splitLinks(cctRoot.getLink())) {
                if(addIncLink(link)) {
                    LogFileHelper.getLogger().info(getName() + " " + link);
                }
            }
            value = false;
        }

        long end = System.nanoTime();
        timeCount = timeCount + (end - start);

        return value;
    }

    /**
     * 根据结点状态来判定是否需要重新计算value和link
     * @param cctRoot
     * @param param
     * @return
     */
    @Override
    protected synchronized boolean evaluation(CCTNode cctRoot, List<Context> param) {
        if(cctRoot.getNodeStatus() == CCTNode.NC_STATE) { //无需重算就直接返回
            return cctRoot.getNodeValue();
        }
        return super.evaluation(cctRoot, param);
    }
}
