package cn.edu.nju.checker;

import cn.edu.nju.model.Context;
import cn.edu.nju.model.node.CCTNode;
import cn.edu.nju.model.node.STNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by njucjc on 2017/10/7.
 */
public class PccChecker extends Checker{

    private Map<String, STNode> stMap;

    private Map<String, List<CCTNode>> cctMap;


    public PccChecker(String name, STNode stRoot, Map<String, Set<Context>> contextSets, Map<String, STNode> stMap) {
        super(name, stRoot, contextSets);
        this.stMap = stMap;
        this.cctMap = new HashMap<>();
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
        //TODO:
        return null;
    }
}
