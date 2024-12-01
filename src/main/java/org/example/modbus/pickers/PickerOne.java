package org.example.modbus.pickers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;
import org.example.modbus.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.example.modbus.ModbusConnection.getMaster;

public class PickerOne {
    private static final String MODBUS_SERVER1_HOST = ConfigReader.getProperty("modbus.server1.host");
    private static final int MODBUS_SERVER1_PORT = Integer.parseInt(ConfigReader.getProperty("modbus.server1.port"));
    private static final int MODBUS_SERVER1_UNITID = Integer.parseInt(ConfigReader.getProperty("modbus.server1.unitId"));
    private static final String RABBITMQ_QUEUE = "modbus-data";
    private static final Logger logger = LoggerFactory.getLogger(PickerOne.class);

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(RABBITMQ_QUEUE, false, false, false, null);

            while (true) {
                BaseLocator<Number> loc = BaseLocator.holdingRegister(MODBUS_SERVER1_UNITID, 1, DataType.TWO_BYTE_INT_SIGNED);
                Number number = getMaster(MODBUS_SERVER1_HOST, MODBUS_SERVER1_PORT).getValue(loc);
                logger.info("Sending data to RabbitMQ: " + number);
                channel.basicPublish("", RABBITMQ_QUEUE, null, Integer.toString(number.intValue()).getBytes());
                Thread.sleep(1000);
            }
        } catch (ModbusInitException | ModbusTransportException | ErrorResponseException | InterruptedException | IOException | TimeoutException e) {
            logger.error("Error in PickerOne: ", e);
        }
    }
}
