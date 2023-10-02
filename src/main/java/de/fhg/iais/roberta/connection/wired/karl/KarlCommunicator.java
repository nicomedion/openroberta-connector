package de.fhg.iais.roberta.connection.wired.karl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.iais.roberta.connection.wired.IWiredRobot;
import de.fhg.iais.roberta.util.Pair;

public class KarlCommunicator {
    private static final Logger LOG = LoggerFactory.getLogger(KarlCommunicator.class);
    private final IWiredRobot robot;
    KarlCommunicator(IWiredRobot robot){
        this.robot = robot;
    }
    public JSONObject getDeviceInfo() {
        JSONObject deviceInfo = new JSONObject();

        deviceInfo.put("firmwarename", this.robot.getType().toString());
        deviceInfo.put("robot", this.robot.getType().toString());
        deviceInfo.put("brickname", this.robot.getType().getPrettyText());
        return deviceInfo;
    }

    boolean uploadFile(String portName, String filePath) {
        if(!SystemUtils.IS_OS_LINUX){
            return false;
        }

        //portName = (SystemUtils.IS_OS_WINDOWS ? "" : "/dev/") + portName; // to hide the parameter, which should not be used

        //requires ampy to be installed
        //TODO find ampy version to bundle with OpenRoberta-Connector
        //TODO how to provide the karl lib? Preinstall on every karl?
        ArgsAdder argsRun = new ArgsAdder();
        argsRun.add("ampy", "-p", "/dev/"+portName, "run", filePath);
        ArgsAdder argsPut = new ArgsAdder();
        argsPut.add("ampy", "-p", "/dev/"+portName, "put", filePath, "main.py");

        try {
            LOG.info("command to be executed: {}", argsPut.toString());

            ProcessBuilder processBuilder = new ProcessBuilder(argsPut.getArgs());
            processBuilder.inheritIO();
            Process p = processBuilder.start();
            int eCode = p.waitFor();
            String errorOutput = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());

            if ( eCode > 0 ) {
                LOG.error("process to send file to karl failed: {}, {}", eCode, errorOutput);
                return false;
            }

            LOG.info("command to be executed: {}", argsRun.toString());

            processBuilder = new ProcessBuilder(argsRun.getArgs());
            processBuilder.inheritIO();
            p = processBuilder.start();
            eCode = p.waitFor();
            errorOutput = IOUtils.toString(p.getErrorStream(), Charset.defaultCharset());

            if ( eCode > 0 ) {
                LOG.error("process to run file from karl failed: {}, {}", eCode, errorOutput);
                return false;
            }

            LOG.info("command execution was successful");
        } catch ( IOException | InterruptedException e ) {
            String msg = "Error while running a process to flash an arduino: " + e.getMessage();
            LOG.error(msg);
            return false;
        }

        return true;
    }

    private static class ArgsAdder {
        private final List<String> args = new ArrayList<>();
        public ArgsAdder add(String... args) {
            for ( String arg : args ) {
                this.args.add(arg);
            }
            return this;
        }

        public List<String> getArgs() {
            return args;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for ( String arg : args ) {
                sb.append(arg).append(' ');
            }
            return sb.toString();
        }
    }

}
