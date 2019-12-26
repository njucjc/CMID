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

    public ConPccChecker(Checker checker, int taskNum, ExecutorService checkExecutorService) {
        super(checker);
        this.pcc = new PccChecker();
        this.addNum = 0;
        this.taskNum = taskNum;
        this.checkExecutorService = checkExecutorService;
    }

    @Override
    public boolean add(String patternId, Context context) {
        if (patternId.equals(stRoot.getContextSetName())) {
            addNum++;
        }
        return super.add(patternId, context);
    }

    @Override
    protected boolean evaluation(CCTNode cctRoot, List<Context> param) {
        int size = cctRoot.getChildTreeNodes().size();
        if (addNum == 0 || stMap.size() < 2 || size <= 1) { // 无新增分支，直接增量检测
            addNum = 0;
            return pcc.evaluation(cctRoot, param);
        }

        if (addNum == 1) {
            List<Context> p1 = new ArrayList<>(param);
            Future<Result> f1 = checkExecutorService.submit(new CheckTask(cctRoot, p1, pcc, 0, size - 2));

            CCTNode newRoot = (CCTNode) cctRoot.getLastChild();
            int workload = newRoot.getChildTreeNodes().size();
            int workerNum = workload;
            if (workerNum >= taskNum) {
                workerNum = taskNum;
            }

            List<Future<Result>> subResultList = new ArrayList<>();

            for (int i = 0; i < workerNum; i++) {
                List<Context> p = new ArrayList<>();
                p.add(newRoot.getContext());
                Future<Result> subResult = checkExecutorService.submit(new CheckTask(newRoot, p, new EccChecker(), i * workload / workerNum, (i + 1) * workload / workerNum - 1));
                subResultList.add(subResult);
            }

            boolean andValue = true;
            boolean orValue = false;
            StringBuilder satisfiedLink = new StringBuilder();
            StringBuilder violatedLink = new StringBuilder();

            Result r1 = null;
            try {
                r1 = f1.get();
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
            if (newRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
                value = andValue;

            } else {
                value = orValue;

            }

            newRoot.setNodeValue(value);

            if (value) {
                newRoot.setLink(satisfiedLink.toString());
            } else {
                newRoot.setLink(violatedLink.toString());
            }

            newRoot.setNodeStatus(NodeStatus.NC_STATE);

            if (cctRoot.getNodeType() == NodeType.UNIVERSAL_NODE) {
                cctRoot.setNodeValue(r1.getValue() && newRoot.getNodeValue());
            } else {
                cctRoot.setNodeValue(r1.getValue() || newRoot.getNodeValue());
            }
            cctRoot.setNodeStatus(NodeStatus.NC_STATE);

            String link = "";
            if (cctRoot.getNodeValue()) {
                if (r1.getValue()) {
                    link = link + r1.getLink() + "#";
                }
                if (newRoot.getNodeValue()) {
                    link = link + newRoot.getLink() + "#";
                }
            } else {
                if (!r1.getValue()) {
                    link = link + r1.getLink() + "#";
                }
                if (!newRoot.getNodeValue()) {
                    link = link + newRoot.getLink() + "#";
                }
            }

            cctRoot.setLink(link);
        }
        else {
            int workload = size;
            int workerNum = workload;
            if ( workerNum >= taskNum) {
                workerNum = taskNum;
            }

            List<Future<Result>> subResultList = new ArrayList<>();

            for(int i = 0; i < workerNum; i++) {
                List<Context> p = new ArrayList<>();
                p.addAll(param);
                Future<Result> subResult = checkExecutorService.submit(new CheckTask(cctRoot, p, new PccChecker(),i * workload / workerNum, (i + 1) * workload / workerNum - 1));
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

            cctRoot.setNodeStatus(NodeStatus.NC_STATE);
        }



        addNum = 0; //新增分支数清零
        return cctRoot.getNodeValue();
    }

    @Override
    public boolean doCheck() {
        return super.doCheck();
    }

}
