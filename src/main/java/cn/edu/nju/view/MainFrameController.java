package cn.edu.nju.view;
import cn.edu.nju.MainApp;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

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
    private Button end;

    @FXML
    private Button exit;

    @FXML
    private void initialize() {
        techSelect.getItems().addAll("ECC", "Con-C", "GAIN", "PCC", "CPCC");
        schedSelect.getItems().addAll("Immed", "GEAS-ori", "GEAS-opt");
        runTypeSelect.getItems().addAll("static-change-based", "dynamic-change-based", "static-time-based", "dynamic-time-based");
        concurrentSelect.getItems().addAll(4, 8, 16);

        runTypeSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.contains("time")) {
                schedSelect.getItems().remove(0, schedSelect.getItems().size());
                schedSelect.getItems().addAll("Immed");
            }
            else {
                schedSelect.getItems().remove(0, schedSelect.getItems().size());
                schedSelect.getItems().addAll("Immed", "GEAS-ori", "GEAS-opt");
            }
        });

        concurrentSelect.setDisable(true);
        techSelect.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("Con-C")) {
                concurrentSelect.setDisable(false);
            }
            else {
                concurrentSelect.setDisable(true);
            }
        });


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
