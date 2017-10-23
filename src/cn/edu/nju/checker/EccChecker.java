package cn.edu.nju.checker;

import cn.edu.nju.context.Context;
import cn.edu.nju.node.CCTNode;
import cn.edu.nju.node.STNode;
import cn.edu.nju.node.TreeNode;
import cn.edu.nju.pattern.Pattern;
import cn.edu.nju.util.BFuncHelper;
import cn.edu.nju.util.LinkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by njucjc on 2017/10/7.
 */
public class EccChecker extends Checker{

    public EccChecker(String name, STNode stRoot, Map<String, Pattern> patternMap) {
        super(name, stRoot, patternMap);
    }

    /**
     *
     * @param op: addition(+) or deletion(-)
     * @param contextSetName: the name of the changed context set
     * @param context: context
     */
    @Override
     public void update(String op, String contextSetName, Context context) {
        //TODO:
     }

    /**
     *
     * @return violated link
     */
    @Override
    public String doCheck() {
        cctRoot = new CCTNode(stRoot.getNodeName(), stRoot.getNodeType());
        buildCCT(stRoot, cctRoot);
        List<Context> param = new ArrayList<>();
        evaluation(cctRoot, param);
        if(cctRoot.getNodeValue()) {
            return "";
        }
        else {
            return  cctRoot.getLink();
        }

    }

    /**
     * 根据语法树构造CCT
     * @param stRoot
     * @param cctRoot
     */
    private void buildCCT(STNode stRoot, CCTNode cctRoot) {
        if (!stRoot.hasChildNodes()) {
            return ;
        }
        if(stRoot.getNodeType() == STNode.EXISTENTIAL_NODE || stRoot.getNodeType() == STNode.UNIVERSAL_NODE) {
            STNode stChild = (STNode) stRoot.getFirstChild();
            for(Context context : patternMap.get(stRoot.getContextSetName()).getContextList()) {
                CCTNode cctChild = new CCTNode(stChild.getNodeName(), stChild.getNodeType(), context);
                buildCCT(stChild, cctChild);
                cctRoot.addChildeNode(cctChild);
            }
        }
        else {
            List<TreeNode> childNodes = stRoot.getChildTreeNodes();
            for (TreeNode n : childNodes) {
                STNode stChild = (STNode) n;
                CCTNode cctChild = new CCTNode(stChild.getNodeName(), stChild.getNodeType());
                buildCCT(stChild, cctChild);
                cctRoot.addChildeNode(cctChild);
            }
        }
    }

    private boolean  evaluation(CCTNode cctRoot, List<Context> param) {
        if(cctRoot.getContext() != null) {
           param.add(cctRoot.getContext());
        }

        boolean value = false;
        if(!cctRoot.hasChildNodes()) {//叶子结点只可能是全称量词、存在量词或bfunc
            if(cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
                value = true;
            }
            else if(cctRoot.getNodeType() == CCTNode.EXISTENTIAL_NODE) {
                value = false;
            }
            else {
                int size = param.size();
                assert size >= 1:"[DEBUG] Param error";
                value = BFuncHelper.bfun(cctRoot.getNodeName(), param.get(size - 1), param.get(size >= 2 ? size - 2:size - 1));
            }
            //设置本结点布尔值
            cctRoot.setNodeValue(value);
            String link = "";
            for(Context context : param) {
                link = link + context.toString() + ";";
            }
            //生成link
            cctRoot.setLink(link);
        }
        else {
            if(cctRoot.getNodeType() == CCTNode.NOT_NODE) {
                value = !evaluation((CCTNode) cctRoot.getFirstChild(), param);
                cctRoot.setNodeValue(value);
                cctRoot.setLink(((CCTNode) cctRoot.getFirstChild()).getLink());
            }
            else if(cctRoot.getNodeType() == CCTNode.AND_NODE){
                CCTNode leftChild = (CCTNode) cctRoot.getChildTreeNodes().get(0);
                CCTNode rightChild = (CCTNode) cctRoot.getChildTreeNodes().get(1);
                boolean leftValue = evaluation(leftChild, param);
                boolean rightValue = evaluation(rightChild, param);

                value = leftValue && rightValue;
                cctRoot.setNodeValue(value);
                if(leftValue && !rightValue) {
                    cctRoot.setLink(rightChild.getLink());
                }
                else if(!leftValue && rightValue) {
                    cctRoot.setLink(leftChild.getLink());
                }
                else {
                    cctRoot.setLink(LinkHelper.linkCartesian(leftChild.getLink(), rightChild.getLink()));
                }
            }
            else if(cctRoot.getNodeType() == CCTNode.IMPLIES_NODE) {
                CCTNode leftChild = (CCTNode) cctRoot.getChildTreeNodes().get(0);
                CCTNode rightChild = (CCTNode) cctRoot.getChildTreeNodes().get(1);
                boolean leftValue = evaluation(leftChild, param);
                boolean rightValue = evaluation(rightChild, param);

                value =  !leftValue || (leftValue && rightValue);
                cctRoot.setNodeValue(value);
                if(value) {
                    cctRoot.setLink(LinkHelper.linkCartesian(leftChild.getLink(), rightChild.getLink()));
                }
                else {
                    cctRoot.setLink(rightChild.getLink());
                }
            }
            else if(cctRoot.getNodeType() == CCTNode.UNIVERSAL_NODE) {
                List<TreeNode> childNodes = cctRoot.getChildTreeNodes();

                StringBuilder satisfiedLink = new StringBuilder();
                StringBuilder violatedLink = new StringBuilder();

                value = true;
                for (TreeNode n : childNodes) {
                    CCTNode child = (CCTNode)n;
                    boolean b = evaluation(child, param);
                    value = value && b;
                    if (b) {
                        if(value) {
                            satisfiedLink.append(child.getLink());
                            satisfiedLink.append("#");
                        }
                    }
                    else {
                        violatedLink.append(child.getLink());
                        violatedLink.append("#");
                    }

                }
                cctRoot.setNodeValue(value);
                if (!value) {
                    violatedLink.deleteCharAt(violatedLink.length() -1);
                    cctRoot.setLink(violatedLink.toString());
                }
                else {
                    satisfiedLink.deleteCharAt(satisfiedLink.length() - 1);
                    cctRoot.setLink(satisfiedLink.toString());
                }
            }
            else  if(cctRoot.getNodeType() == CCTNode.EXISTENTIAL_NODE) {
                List<TreeNode> childNodes = cctRoot.getChildTreeNodes();

                StringBuilder satisfiedLink = new StringBuilder();
                StringBuilder violatedLink = new StringBuilder();

                value = false;
                for (TreeNode n : childNodes) {
                    CCTNode child = (CCTNode)n;
                    boolean b = evaluation(child, param);
                    value = value || b;
                    if (b) {
                        satisfiedLink.append(child.getLink());
                        satisfiedLink.append("#");
                    }
                    else {
                        if(!value) {
                            violatedLink.append(child.getLink());
                            violatedLink.append("#");
                        }
                    }
                }
                cctRoot.setNodeValue(value);
                if (value) {
                    satisfiedLink.deleteCharAt(satisfiedLink.length() - 1);
                    cctRoot.setLink(satisfiedLink.toString());
                }
                else {
                    violatedLink.deleteCharAt(violatedLink.length() -1);
                    cctRoot.setLink(violatedLink.toString());
                }
            }
            else {
                assert false:"[DEBUG] Illegal CCT node: " + cctRoot.getNodeName() + ".";
            }
        }
        //返回上一层
        if (cctRoot.getContext() != null) {
            param.remove(param.size() - 1);
        }

        return value;
    }

}
