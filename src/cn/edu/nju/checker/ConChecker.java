package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.pattern.Pattern;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by njucjc on 2017/12/27.
 */

public class ConChecker extends EccChecker{
    private int taskNum;

    private boolean findSplittingNode = false;

    private ExecutorService checkExecutorService = Executors.newCachedThreadPool();

    public ConChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap, int taskNum) {
        super(name, stRoot, patternMap, stMap);
        this.taskNum = taskNum;
    }


    @Override
    protected boolean evaluation(CCTNode cctRoot, List<Context> param) {
        boolean isSplittingNode = cctRoot.getNodeType() == CCTNode.EXISTENTIAL_NODE
                              || cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE;
        if (!findSplittingNode && isSplittingNode && cctRoot.getChildTreeNodes().size() >= taskNum) {
            findSplittingNode = true;

            int workload = cctRoot.getChildTreeNodes().size();
            List<Future<Boolean>> subResultList = new CopyOnWriteArrayList<>();
            List<CCTNode> subRootList = new CopyOnWriteArrayList<>();

            for(int i = 0; i < taskNum; i++) {
                CCTNode subRoot = new CCTNode(cctRoot.getNodeName(), cctRoot.getNodeType());
                subRootList.add(subRoot);

                for (int j = i * workload / taskNum; j < (i + 1) * workload / taskNum; j++) {
                    subRoot.addChildeNode(cctRoot.getChildTreeNodes().get(j));
                }

                Future<Boolean> subResult = checkExecutorService.submit(new ConCheckTask(subRoot, param));
                subResultList.add(subResult);
            }

            boolean andValue = true;
            boolean orValue = false;
            try {
                for (Future<Boolean> subResult : subResultList) {
                    andValue = andValue && subResult.get();
                    orValue = orValue || subResult.get();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }


            boolean value;
            if (cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
                value = andValue;

            } else {
                value = orValue;

            }

            StringBuffer satisfiedLink = new StringBuffer("");
            StringBuffer violatedLink = new StringBuffer("");

            for (CCTNode subRoot : subRootList) {
                if(subRoot.getNodeValue()) {
                    satisfiedLink.append(subRoot.getLink());
                    satisfiedLink.append("#");
                }
                else {
                    violatedLink.append(subRoot.getLink());
                    violatedLink.append("#");
                }
            }
            cctRoot.setNodeValue(value);

            if(value) {
                int len = satisfiedLink.length();
                if(len > 0) {
                    satisfiedLink.deleteCharAt(len - 1);
                }
                cctRoot.setLink(satisfiedLink.toString());
            } else {
                int len = violatedLink.length();
                if(len > 0) {
                    violatedLink.deleteCharAt(len - 1);
                }
                cctRoot.setLink(violatedLink.toString());
            }
            return value;

        } else {
            return super.evaluation(cctRoot, param);
        }
    }

    @Override
    public synchronized boolean doCheck() {
        boolean value = super.doCheck();
        findSplittingNode = false;
        return value;
    }

    class ConCheckTask implements Callable<Boolean> {
        private CCTNode cctRoot;

        private List<Context> param;



        public ConCheckTask(CCTNode cctRoot, List<Context> param) {
            this.cctRoot = cctRoot;
            this.param = param;
        }

        @Override
        public Boolean call() throws Exception {
            System.out.println("I am here");
            boolean value;
            if (cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
                value = ConChecker.this.universalNodeEval(cctRoot, param);
            } else {
                value = ConChecker.this.existentialNodeEval(cctRoot, param);
            }
            return value;

        }
    }

}
