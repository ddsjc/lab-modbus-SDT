package org.example.modbus.pickers;

import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.locator.BaseLocator;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.example.modbus.ConfigReader;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.modbus.ModbusConnection.getMaster;

public class PickerTwo {
    private static final String MODBUS_SERVER2_HOST = ConfigReader.getProperty("modbus.server2.host");
    private static final int MODBUS_SERVER2_PORT = Integer.parseInt(ConfigReader.getProperty("modbus.server2.port"));
    private static final int MODBUS_SERVER2_UNITID = Integer.parseInt(ConfigReader.getProperty("modbus.server2.unitId"));
    private static final String KAFKA_TOPIC = "modbus-data";
    private static final Logger logger = LoggerFactory.getLogger(PickerOne.class);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());

        KafkaProducer<String, Integer> producer = new KafkaProducer<>(props);
        try {
            while (true) {
                BaseLocator<Number> loc = BaseLocator.holdingRegister(MODBUS_SERVER2_UNITID, 1, DataType.TWO_BYTE_INT_SIGNED);
                Number number = getMaster(MODBUS_SERVER2_HOST, MODBUS_SERVER2_PORT).getValue(loc);
                logger.info("Sending data to Kafka: " + number);
                producer.send(new ProducerRecord<>(KAFKA_TOPIC, "PickerTwo", number.intValue()));
                Thread.sleep(1000);
            }
        } catch (ModbusInitException | ModbusTransportException | ErrorResponseException | InterruptedException e) {
            logger.error("Error in PickerOne: ", e);
        } finally {
            producer.close();
        }
    }
}
