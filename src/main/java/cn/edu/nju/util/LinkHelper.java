package cn.edu.nju.util;

/**
 * Created by njucjc on 2017/10/8.
 */
public class LinkHelper {
    /**
     *
     * @param links1
     * @param links2
     * @return
     */
    public static String linkCartesian(String links1, String links2) {
        if(links1.equals(links2)) {
            return links1;
        }
        String [] strs1 = links1.split("#");
        String [] strs2 = links2.split("#");

        String [] cxts1 = strs1[0].split(" ");
        String [] cxts2 = strs2[0].split(" ");

        int length = cxts1.length > cxts2.length ? cxts2.length : cxts1.length;
        int index;
        int start = 0;
        for(index = 0; index < length; index++) { //目前的规则中，最长前缀仅有1个
            if(!cxts1[index].equals(cxts2[index])) {
                break;
            }
            start += cxts1[index].length() + 1;
        }

        if (index == length) {
            return cxts1.length > cxts2.length ? links1 : links2;
        }

        StringBuilder res = new StringBuilder();
        for(String s1 : strs1) {
            for(String s2 : strs2) {
                res.append(s1);
                res.append(" ");
                res.append(s2.substring(start));
                res.append("#");
            }
        }
        res.deleteCharAt(res.length() - 1);
        return res.toString();
    }

    public static  String [] splitLinks(String links) {
        return links.split("#");
    }

    public static void main(String[] args) {
        System.out.println(linkCartesian("v1 v2#v1 v4","v1 v2#v1 v5"));
        System.out.println(linkCartesian("v1 v2 v3","v1 v4"));
    }
}
