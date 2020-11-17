package cn.edu.nju.view;
import cn.edu.nju.MainApp;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

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
    private Button patternFileSelect;

    @FXML
    private ComboBox<String> techSelect;

    @FXML
    private ComboBox<String> schedSelect;

    @FXML
    private ComboBox<String> runTypeSelect;

    @FXML
    private Button dataFileSelect;

    @FXML
    private Button oracleFileSelect;

    @FXML
    private ComboBox<Integer> concurrentSelect;

    @FXML
    private Button start;

    @FXML
    private Button stop;

    @FXML
    private Button exit;

    @FXML
    private SplitPane splitPane;

    @FXML
    private void initialize() {
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
            }
            else {
                oracleFileSelect.setDisable(false);
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
            concurrentSelect.setDisable(!techSelect.getValue().equals("Con-C") && !techSelect.getValue().equals("CPCC"));
        }
        else {
            oracleFileSelect.setDisable(true);
            oracleFileSelect.setDisable(true);
        }
    }

    /**
     * 开始系统
     */
    @FXML
    private void handleStartSystem() {
        start.setDisable(true);
        setDisableSelect(true);
        //TODO：stopped = false
    }

    /**
     * 停止系统
     */
    @FXML
    private void handleStopSystem() {
        start.setDisable(false);
        setDisableSelect(false);
        //TODO: stopped = true
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
}
