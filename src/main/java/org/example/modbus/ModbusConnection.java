package org.example.modbus;

import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.IpParameters;

public class ModbusConnection {
    private static final ModbusFactory modbusFactory = new ModbusFactory();
    public static ModbusMaster getMaster(String host, int port) throws ModbusInitException {
        IpParameters parameters = new IpParameters();
        parameters.setHost(host);
        parameters.setPort(port);
        ModbusMaster master = modbusFactory.createTcpMaster(parameters, false);
        master.init();
        return master;
    }
}
