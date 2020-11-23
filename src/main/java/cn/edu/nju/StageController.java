package cn.edu.nju;

import cn.edu.nju.client.ClientApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class StageController {

    private Map<String, Stage> stages = new HashMap<>();

    private Map<String, String> allStages;

    public void addStage(String name, Stage stage) {
        stages.put(name, stage);
    }

    public Stage getStage(String name) {
        return stages.get(name);
    }

    public StageController(Map<String, String> allStages) {
        this.allStages = allStages;
    }

    public void loadStages(String name, String resources) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(resources));
            Pane pane = (Pane) loader.load();

            Scene scene = new Scene(pane);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle(name);
            stage.setResizable(false);
            if(name.equals("环境上下文一致性错误检测平台") || name.equals("环境上下文一致性错误检测客户端")){
                stage.setOnCloseRequest( event-> {
                    // 关闭所有窗口
                    for(String n : allStages.keySet()) {
                        closeStage(n);
                    }
                    // 退出所有线程
                    System.exit(0);
                });
            }

            this.addStage(name, stage);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showStage(String name) {
        loadStages(name, allStages.get(name));
        this.getStage(name).show();
    }

    public void closeStage(String name) {
        Stage stage = this.getStage(name);
        if (stage != null) {
            this.getStage(name).close();
        }
    }

}

