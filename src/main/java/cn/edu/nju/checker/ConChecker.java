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

public class ConChecker extends EccChecker {
    private int taskNum;

    private boolean findSplittingNode;

    private ExecutorService checkExecutorService;

    public ConChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap, int taskNum, ExecutorService checkExecutorService) {
        super(name, stRoot, patternMap, stMap);
        this.taskNum = taskNum;
        this.findSplittingNode = false;
        this.checkExecutorService = checkExecutorService;//Executors.newFixedThreadPool(taskNum);
    }

    public ConChecker(Checker checker, int taskNum, ExecutorService checkExecutorService) {
        super(checker);
        this.taskNum = taskNum;
        this.findSplittingNode = false;
        this.checkExecutorService = checkExecutorService;
    }

    @Override
    protected synchronized boolean evaluation(CCTNode cctRoot, List<Context> param) {
        boolean isSplittingNode = cctRoot.getNodeType() == CCTNode.EXISTENTIAL_NODE
                              || cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE;
        if (!findSplittingNode && isSplittingNode && cctRoot.getChildTreeNodes().size() >= taskNum) {
            findSplittingNode = true;

            int workload = cctRoot.getChildTreeNodes().size();
            List<Future<Result>> subResultList = new ArrayList<>();

            for(int i = 0; i < taskNum; i++) {
                List<Context> p = new CopyOnWriteArrayList<>();
                p.addAll(param);
                Future<Result> subResult = checkExecutorService.submit(new ConCheckTask(cctRoot, p, i * workload / taskNum, (i + 1) * workload / taskNum - 1));
                subResultList.add(subResult);
            }

            boolean andValue = true;
            boolean orValue = false;
            StringBuilder satisfiedLink = new StringBuilder();
            StringBuilder violatedLink = new StringBuilder();
            try {
                for (Future<Result> subResult : subResultList) {
                    Result tmpResult = subResult.get();
                    boolean tmp = tmpResult.getValue();
                    andValue = andValue && tmp;
                    orValue = orValue || tmp;
                    if (tmp) {
                        satisfiedLink.append(tmpResult.getLink());
                        satisfiedLink.append("#");
                    } else {
                        violatedLink.append(tmpResult.getLink());
                        violatedLink.append("#");
                    }
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
    protected synchronized void removeCriticalNode(STNode stRoot, CCTNode cctRoot) {
        clearCCTMap();
    }

    @Override
    public synchronized boolean doCheck() {
        boolean value = super.doCheck();
        findSplittingNode = false;
        return value;
    }

    class ConCheckTask implements Callable<Result> {
        private CCTNode subcctRoot;

        private List<Context> param;

        private EccChecker checker = new EccChecker();

        private int start;

        private int end;

        public ConCheckTask(CCTNode subcctRoot, List<Context> param, int start, int end) {
            this.subcctRoot = subcctRoot;
            this.param = param;
            this.start = start;
            this.end = end;
        }

        @Override
        public Result call() throws Exception {
            if (subcctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
                return checker.universalNodeEval(subcctRoot, param, start, end);
            } else {
                return checker.existentialNodeEval(subcctRoot, param, start, end);
            }

        }


        public void setSubcctRoot(CCTNode subcctRoot) {
            this.subcctRoot = subcctRoot;
        }


        public void setParam(List<Context> param) {
            this.param = param;
        }
    }

}
