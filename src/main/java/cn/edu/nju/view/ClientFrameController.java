package cn.edu.nju.view;

import cn.edu.nju.client.Client;
import cn.edu.nju.client.ClientApp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;


public class ClientFrameController {
    @FXML
    private Button dataFileSelect;

    @FXML
    private Hyperlink dataFileLink;

    @FXML
    private Button start;

    private boolean isPaused;
    private boolean isFinished;

    private Client client;

    @FXML
    private void initialize() {
        isPaused = true;
        isFinished = true;

        dataFileLink.setText("");
    }

    @FXML
    private void handleStartClient() {
        if (isPaused) {
            start.setText("暂停");
            client.go();
        }
        else {
            start.setText("启动");
            client.pause();
        }
        dataFileSelect.setDisable(true);
        isPaused = !isPaused;

        if (isFinished) {
            if (!dataFileLink.getText().equals("")) {
                isFinished = false;
                client = new Client(dataFileLink.getText());
                new Thread(client).start();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText(null);
                alert.setContentText("请选择数据文件路径");
                alert.show();

                isPaused = true;
                start.setText("启动");
                start.setDisable(false);
                dataFileSelect.setDisable(false);
            }
        }
    }

    @FXML
    private void handleStopClient() {
        isPaused = true;
        isFinished = true;

        client.finish();
        start.setText("启动");
        start.setDisable(false);
        dataFileSelect.setDisable(false);
    }

    @FXML
    private void handleExitClient() {
        for(String name : ClientApp.allStages.keySet()) {
            ClientApp.stageController.closeStage(name);
        }
        System.exit(0);
    }

    @FXML
    private void handleDataFileSelect() {
        dataFileLink.setText(FileGUIHelper.chooseFile(dataFileLink.getText(),"data", "*.txt").replaceAll("\\\\", "/"));
    }

    @FXML
    private void handleDataFileOpen() {
        FileGUIHelper.openFile(dataFileLink.getText());
    }

}
