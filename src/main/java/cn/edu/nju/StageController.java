package cn.edu.nju;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.HashMap;

public class StageController {

    private HashMap<String, Stage> stages = new HashMap<>();

    public void addStage(String name, Stage stage) {
        stages.put(name, stage);
    }

    public Stage getStage(String name) {
        return stages.get(name);
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
            if(name.equals("环境上下文一致性错误检测平台")){
                stage.setOnCloseRequest( event-> {
                    // 关闭所有窗口
                    for(String n : MainApp.allStages.keySet()) {
                        MainApp.stageController.closeStage(n);
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
        loadStages(name, MainApp.allStages.get(name));
        this.getStage(name).show();
    }

    public void closeStage(String name) {
        Stage stage = this.getStage(name);
        if (stage != null) {
            this.getStage(name).close();
        }
    }

}

