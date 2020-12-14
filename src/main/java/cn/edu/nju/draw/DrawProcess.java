package cn.edu.nju.draw;

import cn.edu.nju.builder.AbstractCheckerBuilder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class DrawProcess implements Runnable{

    private Label ruleNum;

    private Label patternNum;

    private Label interval;

    private Label avgInterval1;

    private Label avgInterval2;

    private Label checkTime;

    private Label checkProgress;

    private ProgressBar checkProgressBar;

    private XYChart.Series<String, Number> inDataSeries;

    private XYChart.Series<String, Number> showIncSeries;
    private XYChart.Series<String, Number> faultIncSeries;
    private XYChart.Series<String, Number> missIncSeries;


    public DrawProcess( Label ruleNum, Label patternNum, Label interval, Label avgInterval1, Label avgInterval2, Label checkTime, Label checkProgress, ProgressBar checkProgressBar,
                      XYChart.Series<String, Number> inDataSeries, XYChart.Series<String, Number> showIncSeries, XYChart.Series<String, Number> faultIncSeries, XYChart.Series<String, Number> missIncSeries) {
        this.ruleNum = ruleNum;
        this.patternNum = patternNum;
        this.interval = interval;
        this.avgInterval1 = avgInterval1;
        this.avgInterval2 = avgInterval2;
        this.checkTime = checkTime;
        this.checkProgress = checkProgress;
        this.checkProgressBar = checkProgressBar;
        this.inDataSeries = inDataSeries;
        this.showIncSeries = showIncSeries;
        this.faultIncSeries = faultIncSeries;
        this.missIncSeries = missIncSeries;
    }

    private synchronized void repaint() {

        Platform.runLater(()->{
            ruleNum.setText(padding(AbstractCheckerBuilder.ruleNum, 3));
            patternNum.setText(padding(AbstractCheckerBuilder.patternNum, 3));
            interval.setText(padding(AbstractCheckerBuilder.interval, 0) + " ms");
            avgInterval1.setText(padding(AbstractCheckerBuilder.avgInterval1, 0) + " ms");
            avgInterval2.setText(padding(AbstractCheckerBuilder.avgInterval2, 0) + " ms");
            checkTime.setText(padding(AbstractCheckerBuilder.totalTime / 1000000, 0) + " ms");
            checkProgress.setText(new DecimalFormat("##0.0%").format(AbstractCheckerBuilder.progress));
            checkProgressBar.setProgress(AbstractCheckerBuilder.progress);


            inDataSeries.setName("已检测：" + padding(AbstractCheckerBuilder.dataCount, 0));
            inDataSeries.getData().set(0, new XYChart.Data<>("", AbstractCheckerBuilder.dataCount));


            int [] inc = AbstractCheckerBuilder.accuracy(false);
            showIncSeries.setName("汇报数：" + padding(inc[0], 0));
            showIncSeries.getData().set(0, new XYChart.Data<>(" ", inc[0]));


            faultIncSeries.setName("误报数：" +  padding(inc[1], 0));
            faultIncSeries.getData().set(0, new XYChart.Data<>("",inc[1]));

            missIncSeries.setName("漏报数：" + padding(inc[2], 0) );
            missIncSeries.getData().set(0, new XYChart.Data<>("  ",inc[2]));


        });
    }

    private String padding(long num, int len) {
        DecimalFormat numFormatter = new DecimalFormat("#,###");
        StringBuilder res = new StringBuilder(numFormatter.format(num));

        if (res.length() < len) {
            for (int i = 0; i < len - res.length(); i++) {
                res.append("  ");
            }
        }
        return res.toString();
    }

    @Override
    public void run() {
        while(true) {
            repaint();
            try {
                TimeUnit.MILLISECONDS.sleep(150);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
