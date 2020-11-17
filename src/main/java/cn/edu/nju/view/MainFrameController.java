package cn.edu.nju.view;
import cn.edu.nju.MainApp;
import cn.edu.nju.util.FileHelper;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class MainFrameController {

    @FXML
    private BarChart<String, Number> dataChart;
    private XYChart.Series<String, Number> inDataSeries = new XYChart.Series<>();

    @FXML
    private BarChart<String, Number> incChart;
    private XYChart.Series<String, Number> showIncSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> faultIncSeries = new XYChart.Series<>();
    private XYChart.Series<String, Number> missIncSeries = new XYChart.Series<>();

    @FXML
    private Label ruleNum;

    @FXML
    private Label patternNum;

    @FXML
    private Label interval;

    @FXML
    private Label checkTime;

    @FXML
    private ProgressBar checkProgressBar;

    @FXML
    private Label checkProgress;

    @FXML
    private Button logExport;

    @FXML
    private Button ruleFileSelect;

    @FXML
    private Hyperlink ruleFileLink;

    @FXML
    private Button patternFileSelect;

    @FXML
    private Hyperlink patternFileLink;

    @FXML
    private ComboBox<String> techSelect;

    @FXML
    private ComboBox<String> schedSelect;

    @FXML
    private ComboBox<String> runTypeSelect;

    @FXML
    private Button dataFileSelect;

    @FXML
    private Hyperlink dataFileLink;

    @FXML
    private Button oracleFileSelect;

    @FXML
    private Hyperlink oracleFileLink;

    @FXML
    private ComboBox<Integer> concurrentSelect;

    @FXML
    private Button start;

    @FXML
    private SplitPane splitPane;

    private String logFilePath = FileHelper.createTempFile("tempLog", ".log");

    boolean isPaused;

    @FXML
    private void initialize() {
        isPaused = true;
        techSelect.getItems().addAll("ECC", "Con-C", "GAIN", "PCC", "CPCC");
        schedSelect.getItems().addAll("Immed", "GEAS-ori", "GEAS-opt");
        runTypeSelect.getItems().addAll("static-change-based", "dynamic-change-based", "static-time-based", "dynamic-time-based");
        concurrentSelect.getItems().addAll(2, 4, 8, 16);

        oracleFileSelect.setDisable(true);
        runTypeSelect.setValue("static-change-based");
        runTypeSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            runTypeSelect.setValue(newValue);
            if (newValue.contains("time")) {
                schedSelect.getItems().remove(0, schedSelect.getItems().size());
                schedSelect.getItems().addAll("Immed");
            }
            else {
                schedSelect.getItems().remove(0, schedSelect.getItems().size());
                schedSelect.getItems().addAll("Immed", "GEAS-ori", "GEAS-opt");
            }

            if (newValue.contains("static")) {
                oracleFileSelect.setDisable(true);
                oracleFileLink.setDisable(true);
                oracleFileLink.setText("");
            }
            else {
                oracleFileSelect.setDisable(false);
                oracleFileLink.setDisable(false);
            }
        });

        concurrentSelect.setDisable(true);
        techSelect.setValue("ECC");
        techSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            techSelect.setValue(newValue);
            if (newValue.equals("Con-C") || newValue.equals("CPCC")) {
                concurrentSelect.setDisable(false);
            }
            else {
                concurrentSelect.setDisable(true);
            }
        });

        schedSelect.setValue("Immed");
        schedSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { schedSelect.setValue(newValue); });

        ruleFileLink.setText("");
        patternFileLink.setText("");
        dataFileLink.setText("");
        oracleFileLink.setText("");

        concurrentSelect.setValue(2);
        concurrentSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> { concurrentSelect.setValue(newValue); });

        double p = splitPane.getDividers().get(0).getPosition();
        splitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> splitPane.getDividers().get(0).setPosition(p));


        // 数据展示区初始化
        inDataSeries.setName("已检测:0");
        inDataSeries.getData().add(new XYChart.Data<>("", 0));

        showIncSeries.setName("汇报INC:0");
        showIncSeries.getData().add(new XYChart.Data<>(" ",0));

        faultIncSeries.setName("误报:0");
        faultIncSeries.getData().add( new XYChart.Data<>("",0));

        missIncSeries.setName("漏报:0");
        missIncSeries.getData().add(new XYChart.Data<>("  ",0));


        dataChart.getData().addAll(inDataSeries);
        incChart.getData().addAll(showIncSeries, faultIncSeries, missIncSeries);

        //  missIncSeries.setName("漏报:10");
        //  missIncSeries.getData().set(0, new XYChart.Data<>("  ",10));
        //  incChart.getData().addAll(showIncSeries, faultIncSeries, missIncSeries);
    }

    private void setDisableSelect(boolean disable) {
        ruleFileSelect.setDisable(disable);
        patternFileSelect.setDisable(disable);
        techSelect.setDisable(disable);
        schedSelect.setDisable(disable);
        runTypeSelect.setDisable(disable);
        dataFileSelect.setDisable(disable);
        if (!disable) {
            oracleFileSelect.setDisable(!runTypeSelect.getValue().contains("dynamic"));
            oracleFileLink.setDisable(!runTypeSelect.getValue().contains("dynamic"));

            concurrentSelect.setDisable(!techSelect.getValue().equals("Con-C") && !techSelect.getValue().equals("CPCC"));
        }
        else {
            oracleFileSelect.setDisable(true);
            oracleFileSelect.setDisable(true);
        }
    }

    private String chooseFile(String oldPath, String type, String suffix) {
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

    private void  openFile(String path) {
        if (path != null && !path.equals("")) {
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRuleFileSelect() {
        ruleFileLink.setText(chooseFile(ruleFileLink.getText(),"rule", "*.xml"));
    }

    @FXML
    private void handlePatternFileSelect() {
        patternFileLink.setText(chooseFile(patternFileLink.getText(),"pattern", "*.xml"));
    }

    @FXML
    private void handleDataFileSelect() {
        dataFileLink.setText(chooseFile(dataFileLink.getText(),"data", "*.txt"));
    }

    @FXML
    private void handleOracleFileSelect() {
        oracleFileLink.setText(chooseFile(oracleFileLink.getText(),"oracle", "*.log"));
    }

    @FXML
    private void handleRuleFileOpen() {
        openFile(ruleFileLink.getText());
    }

    @FXML
    private void handlePatternFileOpen() {
        openFile(patternFileLink.getText());
    }

    @FXML
    private void handleDataFileOpen() {
        openFile(dataFileLink.getText());
    }

    @FXML
    private void handleOracleFileOpen() {
        openFile(oracleFileLink.getText());
    }

    @FXML
    private void handleLogExport() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("日志文件", "*.log");

        fileChooser.getExtensionFilters().add(extFilter);
        Stage s = new Stage();
        File file = fileChooser.showSaveDialog(s);
        if (file == null)
            return;
        if(file.exists()){//文件已存在，则删除覆盖文件
            file.delete();
        }
        String exportFilePath = file.getAbsolutePath();

        FileHelper.copyFile(logFilePath, exportFilePath);
    }

    /**
     * 开始系统
     */
    @FXML
    private void handleStartSystem() {
        //TODO: initial checker (is null or not)

        if (isPaused) {
            start.setText("暂停");
            //TODO: consistency checking go
        }
        else {
            start.setText("启动");
            //TODO: consistency checking pause
        }

        setDisableSelect(true);
        logExport.setDisable(isPaused);

        isPaused = !isPaused;
    }

    /**
     * 停止系统
     */
    @FXML
    private void handleStopSystem() {
        isPaused = true;
        start.setText("启动");
        start.setDisable(false);
        setDisableSelect(false);
        logExport.setDisable(false);

        //TODO: consistency checking stop and set checker null
    }

    /**
     * 退出系统
     */
    @FXML
    private void handleExitSystem() {
        for(String name : MainApp.allStages.keySet()) {
            MainApp.stageController.closeStage(name);
        }
        System.exit(0);
    }

    @FXML
    private void handleHelp() {
        try {
            Desktop.getDesktop().browse(getClass().getClassLoader().getResource("manual/manual.pdf").toURI());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
