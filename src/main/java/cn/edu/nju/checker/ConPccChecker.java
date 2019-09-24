package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.NodeStatus;
import cn.edu.nju.node.NodeType;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class ConPccChecker extends PccChecker {
    private Checker pcc;

    private int addNum;

    private int taskNum;

    private ExecutorService checkExecutorService;


    public ConPccChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap, int taskNum, ExecutorService checkExecutorService) {
        super(name, stRoot, patternMap, stMap);
        this.pcc = new PccChecker();
        this.addNum = 0;
        this.taskNum = taskNum;
        this.checkExecutorService = checkExecutorService;
    }

    @Override
    public boolean add(String patternId, Context context) {
        addNum++;
        return super.add(patternId, context);
    }

    @Override
    protected boolean evaluation(CCTNode cctRoot, List<Context> param) {
        int size = cctRoot.getChildTreeNodes().size();
        if (addNum == 0 || stMap.size() < 2 || (addNum == size && size == 1)) { // 无新增分支，直接增量检测
            return super.evaluation(cctRoot, param);
        }
        List<Context> p1 = new ArrayList<>(param);
        Future<Result> f1 = checkExecutorService.submit(new CheckTask(cctRoot, p1, pcc, 0, size - 2));
        List<Context> p2 = new ArrayList<>(param);
        Future<Result> f2 = checkExecutorService.submit(new CheckTask(cctRoot, p2, pcc, size - 1, size - 1));

        try {
            Result r1 = f1.get();
            Result r2 = f2.get();

            if (cctRoot.getNodeType() == NodeType.UNIVERSAL_NODE) {
                cctRoot.setNodeValue(r1.getValue() && r2.getValue());
            }
            else {
                cctRoot.setNodeValue(r1.getValue() || r2.getValue());
            }
            cctRoot.setNodeStatus(NodeStatus.NC_STATE);

            String link = "";
            if (cctRoot.getNodeValue()) {
                if (r1.getValue()) {
                    link = link + r1.getLink() + "#";
                }
                if (r2.getValue()) {
                    link = link + r2.getLink() + "#";
                }
            }
            else {
                if (!r1.getValue()) {
                    link = link + r1.getLink() + "#";
                }
                if (!r2.getValue()) {
                    link = link + r2.getLink() + "#";
                }
            }

            cctRoot.setLink(link);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        addNum = 0; //新增分支数清零
        return cctRoot.getNodeValue();
    }

    @Override
    public boolean doCheck() {
        return super.doCheck();
    }
}
