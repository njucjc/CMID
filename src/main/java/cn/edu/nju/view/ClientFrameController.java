package cn.edu.nju.view;

import cn.edu.nju.client.Client;
import cn.edu.nju.client.ClientApp;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;


public class ClientFrameController {
    @FXML
    private Button dataFileSelect;

    @FXML
    private Hyperlink dataFileLink;

    @FXML
    private Button start;

    @FXML
    private ComboBox<Integer> port;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    private boolean isPaused;
    private boolean isFinished;

    private Client client;

    @FXML
    private void initialize() {
        isPaused = true;
        isFinished = true;

        dataFileLink.setText("");
        for(int i = 8000; i < 9000; ++i) {
            port.getItems().add(i);
        }

        port.setValue(8000);
        port.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            port.setValue(newValue);
        });

        new Thread(() -> {
            while (true) {
                Platform.runLater(()-> {
                    progressBar.setProgress(Client.progress);
                    progressLabel.setText(new DecimalFormat("##0.0%").format(Client.progress));
                });

                try {
                    TimeUnit.MILLISECONDS.sleep(150);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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
                client = new Client(port.getValue(), dataFileLink.getText());
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
