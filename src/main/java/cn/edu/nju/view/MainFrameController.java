package cn.edu.nju.view;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

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
    private void initialize() {
        inDataSeries.setName("已检测:10,000,000");
        inDataSeries.getData().add(new XYChart.Data<>("", 10000000));

        showIncSeries.setName("汇报INC:10");
        showIncSeries.getData().add(new XYChart.Data<>(" ",10));

        faultIncSeries.setName("误报:120");
        faultIncSeries.getData().add( new XYChart.Data<>("",120));

        missIncSeries.setName("漏报:100");
        missIncSeries.getData().add(new XYChart.Data<>("  ",100));


        dataChart.getData().addAll(inDataSeries);
        incChart.getData().addAll(showIncSeries, faultIncSeries, missIncSeries);

        //  missIncSeries.setName("漏报:10");
        //  missIncSeries.getData().set(0, new XYChart.Data<>("  ",10));
        //  incChart.getData().addAll(showIncSeries, faultIncSeries, missIncSeries);

    }
}
