package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.node.TreeNode;
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

    public ConChecker(String name, STNode stRoot, Map<String, Pattern> patternMap, Map<String, STNode> stMap, int taskNum) {
        super(name, stRoot, patternMap, stMap);
        this.taskNum = taskNum;
        this.findSplittingNode = false;
        this.checkExecutorService = Executors.newFixedThreadPool(taskNum);
    }


    @Override
    protected synchronized boolean evaluation(CCTNode cctRoot, List<Context> param) {
        boolean isSplittingNode = cctRoot.getNodeType() == CCTNode.EXISTENTIAL_NODE
                              || cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE;
        if (!findSplittingNode && isSplittingNode && cctRoot.getChildTreeNodes().size() >= taskNum) {
            findSplittingNode = true;

            int workload = cctRoot.getChildTreeNodes().size();
            List<Future<Boolean>> subResultList = new ArrayList<>();
            List<TreeNode> subRootList = new ArrayList<>();

           // System.out.println("Workload: " + workload);
            for(int i = 0; i < taskNum; i++) {
                CCTNode subRoot = new CCTNode(cctRoot.getNodeName(), cctRoot.getNodeType());
                subRootList.add(subRoot);

            //    System.out.println("[ " +i * workload / taskNum + "," + ((i + 1) * workload / taskNum - 1) + " ]");
                for (int j = i * workload / taskNum ; j < (i + 1) * workload / taskNum; j++) {
                    subRoot.addChildeNode(cctRoot.getChildTreeNodes().get(j));
                }

                List<Context> p = new CopyOnWriteArrayList<>();
                for(Context c : param) {
                    p.add(c);
                }

                Future<Boolean> subResult = checkExecutorService.submit(new ConCheckTask(subRoot, p));
                subResultList.add(subResult);
            }

            boolean andValue = true;
            boolean orValue = false;
            try {
                for (Future<Boolean> subResult : subResultList) {
                    boolean tmp = subResult.get();
                    andValue = andValue && tmp;
                    orValue = orValue || tmp;
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

            StringBuilder satisfiedLink = new StringBuilder();
            StringBuilder violatedLink = new StringBuilder();

            for (TreeNode n : subRootList) {
                CCTNode subRoot = (CCTNode) n;
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

    @Override
    public void shutdown() {
        checkExecutorService.shutdown();
        super.shutdown();
    }

    class ConCheckTask implements Callable<Boolean> {
        private CCTNode subcctRoot;

        private List<Context> param;

        private EccChecker checker = new EccChecker();

        public ConCheckTask(CCTNode subcctRoot, List<Context> param) {
            this.subcctRoot = subcctRoot;
            this.param = param;
        }

        @Override
        public Boolean call() throws Exception {
            return checker.evaluation(subcctRoot, param);

        }


        public void setSubcctRoot(CCTNode subcctRoot) {
            this.subcctRoot = subcctRoot;
        }


        public void setParam(List<Context> param) {
            this.param = param;
        }
    }

}
