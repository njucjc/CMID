package cn.edu.nju.view;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FileGUIHelper {
    public static String chooseFile(String oldPath, String type, String suffix) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开" + type + "文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(type + "文件", suffix));
        File file = fileChooser.showOpenDialog(new Stage());

        if (file == null) {
            return oldPath;
        }
        else {
            return file.getAbsolutePath();
        }
    }

    public static void  openFile(String path) {
        if (path != null && !path.equals("")) {
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
