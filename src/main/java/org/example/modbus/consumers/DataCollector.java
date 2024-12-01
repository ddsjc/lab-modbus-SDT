package org.example.modbus.consumers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.example.modbus.ConfigReader;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class DataCollector {
    private static final String RABBITMQ_QUEUE = "modbus-data";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres1";
    private static final String DB_USER = ConfigReader.getProperty("db.user");
    private static final String DB_PASS = ConfigReader.getProperty("db.pass");

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (java.sql.Connection dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Connection rabbitConnection = factory.newConnection();
             Channel channel = rabbitConnection.createChannel()) {

            channel.queueDeclare(RABBITMQ_QUEUE, false, false, false, null);

            channel.basicConsume(RABBITMQ_QUEUE, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                int value = Integer.parseInt(message);
                String query = "INSERT INTO modbus_data (value, port, date) VALUES (?, ?, ?)";
                try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
                    statement.setInt(1, value);
                    statement.setString(2, delivery.getEnvelope().getRoutingKey());
                    statement.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, consumerTag -> {});

        } catch (SQLException | IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
