package cn.edu.nju.util;

import java.time.Year;
import java.util.Scanner;

public class Interaction {
    public static void say(String content) {
        Scanner in = new Scanner(System.in);
        String str;
        while(true) {
            System.out.println("[INFO] 是否" + content + "（Y/N）：");
            str = in.nextLine();

            if ("y".equals(str.toLowerCase())) {
                break;
            }
            else if ("n".equals(str.toLowerCase())) {
                System.exit(0);
            }

        }
    }
}
