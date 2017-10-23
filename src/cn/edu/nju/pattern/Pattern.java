package cn.edu.nju.pattern;
import cn.edu.nju.context.Context;
import cn.edu.nju.util.TimestampHelper;

import java.util.*;
/**
 * Created by njucjc on 2017/10/23.
 */
public class Pattern {
    private String id;
    private long freshness;
    private String category;
    private String subject;
    private String predicate;
    private String object;
    private String site;

    private List<Context> contextList;

    public Pattern(String id,
                   long freshness,
                   String category,
                   String subject,
                   String predicate,
                   String object,
                   String site) {
        this.id = id;
        this.freshness = freshness;
        this.category = category;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.site = site;

        this.contextList = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public List<Context> getContextList() {
        return contextList;
    }

    /**
     * 判断一个context是否属于改pattern
     * @param context
     * @return
     */
    public boolean isBelong(Context context) {
        int status = context.getStatus();
        String plateNumber = context.getPlateNumber();

        boolean belong = false;
        if (status == 0) {
            if("run_with_service".equals(predicate)) {
                belong = false;
            }
            else {
                belong = true;
            }
        }
        else {
            if("run_without_service".equals(predicate)) {
                belong = false;
            }
            else {
                belong = true;
            }
        }

       if(!"any".equals(site)) {
            int num1 = Integer.parseInt(site.substring(site.length() - 1));
            int num2 = Integer.parseInt(plateNumber.substring(plateNumber.length() - 1));
            belong = belong && (num1 == num2);
        }

        return  belong;
    }

    /**
     * 添加一个context到pattern集合
     * @param context
     * @return
     */
    public boolean addContext(Context context) {
        if(!isBelong(context)) {
            return false;
        }
        contextList.add(context);
        return true;
    }

    /**
     * 删除与当前时间戳相距较远的context
     * @param timestamp 当前时间戳
     */
    public void delete(String timestamp) {
        for(Context context : contextList) {
            if(TimestampHelper.timestampDiff(context.getTimestamp(), timestamp) > freshness) {
                contextList.remove(context);
            }
            else { //context按timestamp的升序添加并排列，故只需找到第一个符合要求的就可退出循环
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "Pattern{" +
                "id=" + id +
                ", freshness=" + freshness +
                ", category=" + category +
                ", subject=" + subject +
                ", predicate=" + predicate +
                ", object=" + object +
                ", site=" + site +
                '}';
    }
}
