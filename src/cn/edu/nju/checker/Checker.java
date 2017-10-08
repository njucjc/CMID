package cn.edu.nju.checker;

import cn.edu.nju.model.Context;
import cn.edu.nju.model.node.CCTNode;
import cn.edu.nju.model.node.STNode;
import cn.edu.nju.model.node.TreeNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by njucjc on 2017/10/3.
 */
public abstract class Checker {
    /* Checker(rule) name*/
    protected String name;

    /* CCT root for constraint */
    protected CCTNode cctRoot;

    /*Syntax tree for constraint*/
    protected STNode stRoot;

    protected Map<String, Set<Context>> contextSets;

    public Checker(String name, STNode stRoot, Map<String, Set<Context>> contextSets) {
        this.name = name;
        this.stRoot = stRoot;
        this.contextSets = contextSets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param op: addition(+) or deletion(-)
     * @param contextSetName: the name of the changed context set
     * @param context: context
     */
    abstract public void update(String op, String contextSetName, Context context);

    /**
     *
     * @return violated link
     */
    abstract public String doCheck();


}
