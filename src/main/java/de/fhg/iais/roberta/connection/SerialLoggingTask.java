package de.fhg.iais.roberta.connection;

import de.fhg.iais.roberta.util.IOraListener;
import de.fhg.iais.roberta.util.SerialPortHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// https://github.com/Fazecast/jSerialComm/wiki/Nonblocking-Reading-Usage-Example
public class SerialLoggingTask extends AbstractLoggingTask {
    private static final Logger LOG = LoggerFactory.getLogger(SerialLoggingTask.class);

    private final SerialPortHelper serialPortHelper;

    public SerialLoggingTask(IOraListener<byte[]> listener, String robot, CharSequence port, int serialRate) {
        registerListener(listener);

        this.serialPortHelper = SerialPortHelper.getInstance();
        this.serialPortHelper.initializeSerialPort(robot, port, serialRate);
        this.serialPortHelper.openPort(0);
    }

    @Override
    protected void log() {
        try {
            byte[] readBuffer = new byte[this.serialPortHelper.bytesAvailable()];
            this.serialPortHelper.readBytes(readBuffer, readBuffer.length);
            fire(readBuffer);
        } catch ( NegativeArraySizeException e ) { // gets thrown if cable is disconnected
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void finish() {
        this.serialPortHelper.closePort();
    }
}
