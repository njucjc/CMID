package cn.edu.nju;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class MainApp extends Application {

    public static StageController stageController = new StageController();
    public static Map<String, String> allStages = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        allStages.put("环境上下文一致性错误检测平台", "fxml/MainFrame.fxml");

        stageController.showStage("环境上下文一致性错误检测平台");


    }


    public static void main(String[] args) {
        launch(args);
    }
}

