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

    private boolean isFinished;

    private Client client;

    @FXML
    private void initialize() {
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
        start.setDisable(true);
        dataFileSelect.setDisable(true);
        port.setDisable(true);

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

                start.setDisable(false);
                dataFileSelect.setDisable(false);
                port.setDisable(false);
            }
        }
    }

    @FXML
    private void handleStopClient() {
        isFinished = true;

        client.finish();

        start.setDisable(false);
        dataFileSelect.setDisable(false);
        port.setDisable(false);
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
