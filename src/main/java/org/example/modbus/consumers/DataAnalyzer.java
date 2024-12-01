package org.example.modbus.consumers;

import com.rabbitmq.client.*;
import org.example.modbus.ConfigReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataAnalyzer {
    private static final String RABBITMQ_QUEUE = "modbus-data";
    private static final String RABBITMQ_ANOMALIES_QUEUE = "modbus-anomalies";
    private static final int ANOMALY_THRESHOLD = 90; // example threshold
    private static final int PREDICTION_WINDOW = 10; // number of points to use for prediction

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private Map<String, GraphRenderer> graphRenderers;

    public DataAnalyzer() {
        factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(RABBITMQ_QUEUE, false, false, false, null);
            channel.queueDeclare(RABBITMQ_ANOMALIES_QUEUE, false, false, false, null);

            graphRenderers = new HashMap<>();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shouldStop.set(true)));

        try {
            channel.basicConsume(RABBITMQ_QUEUE, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                int value = Integer.parseInt(message);
                String key = delivery.getEnvelope().getRoutingKey();
                long timestamp = System.currentTimeMillis();

                graphRenderers.computeIfAbsent(key, k -> new GraphRenderer(k, PREDICTION_WINDOW));
                GraphRenderer renderer = graphRenderers.get(key);

                if (Math.abs(value) > ANOMALY_THRESHOLD) {
                    channel.basicPublish("", RABBITMQ_ANOMALIES_QUEUE, null, message.getBytes());
                    renderer.addAnomalyPoint(timestamp, value);
                } else {
                    renderer.addNormalPoint(timestamp, value);
                }
                renderer.updatePrediction(timestamp);
            }, consumerTag -> {});

            while (!shouldStop.get()) {
                Thread.sleep(100);
            }

            channel.close();
            connection.close();
        } catch (IOException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DataAnalyzer().start();
    }
}
