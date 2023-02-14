package de.fhg.iais.roberta.util;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;

public final class SerialPortHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SerialPortHelper.class);

    private SerialPort serialPort;
    private String robot;

    private SerialPortHelper() {
    }

    private static final class InstanceHolder {
        private static final SerialPortHelper instance = new SerialPortHelper();
    }

    public static SerialPortHelper getInstance() {
        return InstanceHolder.instance;
    }

    public void initializeSerialPort(String robot, CharSequence port, int serialRate) {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        this.robot = robot;
        this.serialPort =
            Arrays.stream(serialPorts)
                .filter(comPort -> comPort.getSystemPortName().contains(port))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Port is not available!"));
        this.serialPort.setBaudRate(serialRate);
        this.serialPort.openPort(0);
        logInfo();
    }

    public void openPort(int safetySleepTime) {
        this.serialPort.openPort(safetySleepTime);
    }

    public void closePort() {
        if ( RobotPropertyHelper.getInstance().keepPortOpen(this.robot) ) {
            this.serialPort.closePort();
        }
    }

    public void readBytes(byte[] buffer, int length) {
        this.serialPort.readBytes(buffer, length);
    }

    public int bytesAvailable() {
        return this.serialPort.bytesAvailable();
    }

    public void logInfo() {
        LOG.info("SerialPort {} {} {} opened, logging with baud rate of {}",
            this.serialPort.getSystemPortName(),
            this.serialPort.getDescriptivePortName(),
            this.serialPort.getPortDescription(),
            this.serialPort.getBaudRate());
    }

}