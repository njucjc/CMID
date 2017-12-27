package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.util.LinkHelper;
import cn.edu.nju.util.LogFileHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by njucjc on 2017/10/7.
 */
public class EccChecker extends Checker{

    public EccChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap) {
        super(name, stRoot, patternMap, stMap);
    }

    /**
     *
     * @return violated link
     */
    @Override
    public synchronized boolean doCheck() {
        checkTimes++;
        clearCCTMap();
        cctRoot = new CCTNode(stRoot.getNodeName(), stRoot.getNodeType());
        buildCCT(stRoot, cctRoot);
        List<Context> param = new CopyOnWriteArrayList<>();
        evaluation(cctRoot, param);
        if (cctRoot.getNodeValue()) {
            return true;
        }
        else {
            for(String link : LinkHelper.splitLinks(cctRoot.getLink())) {
                if(addIncLink(link)) {
                    LogFileHelper.getLogger().info(getName() + " " + link);
                }
            }
            return false;
        }

    }

}
