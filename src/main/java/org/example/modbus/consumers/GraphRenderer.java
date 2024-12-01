package org.example.modbus.consumers;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class GraphRenderer {
    private TimeSeries normalSeries;
    private TimeSeries anomalySeries;
    private TimeSeries predictionSeries;
    private int predictionWindow;

    public GraphRenderer(String key, int predictionWindow) {
        this.predictionWindow = predictionWindow;

        normalSeries = new TimeSeries("Normal Values " + key);
        anomalySeries = new TimeSeries("Anomalies " + key);
        predictionSeries = new TimeSeries("Predictions " + key);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(normalSeries);
        dataset.addSeries(anomalySeries);
        dataset.addSeries(predictionSeries);

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Data Analysis and Prediction " + key, "Time", "Value", dataset);

        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesPaint(2, Color.GREEN);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesShapesVisible(2, true);
        plot.setRenderer(renderer);

        ChartPanel chartPanel = new ChartPanel(chart);

        JFrame frame = new JFrame("Data Analysis and Prediction Chart " + key);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    public void addNormalPoint(long timestamp, double value) {
        normalSeries.addOrUpdate(new Millisecond(new Date(timestamp)), value);
    }

    public void addAnomalyPoint(long timestamp, double value) {
        anomalySeries.addOrUpdate(new Millisecond(new Date(timestamp)), value);
    }

    public void updatePrediction(long timestamp) {
        if (normalSeries.getItemCount() >= predictionWindow) {
            SimpleRegression regression = new SimpleRegression();
            for (int i = normalSeries.getItemCount() - predictionWindow; i < normalSeries.getItemCount(); i++) {
                regression.addData(normalSeries.getTimePeriod(i).getMiddleMillisecond(), normalSeries.getValue(i).doubleValue());
            }
            double predictedValue = regression.predict(timestamp + 1000);
            predictionSeries.addOrUpdate(new Millisecond(new Date(timestamp + 1000)), predictedValue);
        }
    }
}