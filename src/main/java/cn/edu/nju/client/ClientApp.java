package cn.edu.nju.client;

import cn.edu.nju.StageController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class ClientApp extends Application {

    public static Map<String, String> allStages = new HashMap<>();
    public static StageController stageController = new StageController(allStages);

    @Override
    public void start(Stage primaryStage) throws Exception{
        allStages.put("环境上下文一致性错误检测客户端", "fxml/ClientFrame.fxml");

        stageController.showStage("环境上下文一致性错误检测客户端");


    }


    public static void main(String[] args) {
        launch(args);
    }
}
