package de.fhg.iais.roberta.connection.wireless.robotino;

import de.fhg.iais.roberta.connection.wireless.IWirelessCommunicator;
import de.fhg.iais.roberta.util.PropertyHelper;
import de.fhg.iais.roberta.util.SshConnection;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Communicator class for the NAO robot. Handles network communication between the NAO and the connector.
 */
public class RobotinoCommunicator implements IWirelessCommunicator {
    private static final Logger LOG = LoggerFactory.getLogger(RobotinoCommunicator.class);

    private static final String USERNAME = "robotino";

    private final String name;
    private final InetAddress address;

    private String password = "";

    private final String workingDirectory;
    private String firmwareVersion = "";

    public RobotinoCommunicator(Robotino robotino) {
        this.name = robotino.getName();
        this.address = robotino.getAddress();

        if ( SystemUtils.IS_OS_WINDOWS ) {
            this.workingDirectory = System.getenv("APPDATA") + '/' + PropertyHelper.getInstance().getProperty("artifactId") + '/';
        } else {
            this.workingDirectory = System.getProperty("user.home") + '/' + PropertyHelper.getInstance().getProperty("artifactId") + '/';
        }
    }

    /**
     * Uploads a binary file to the Robotino robot.
     *
     * @param binaryFile the content of the file
     * @param fileName   the desired file name
     * @throws UserAuthException if the user is not correctly authorized
     * @throws IOException if something with the ssh connection or file transfer went wrong
     */
    public void uploadFile(byte[] binaryFile, String fileName) throws UserAuthException, IOException {
        //TODO CHANGE FOR ROBOTINO
        try (SshConnection ssh = new SshConnection(this.address, "pi", this.password)) {
            ssh.command("rm -rf /home/tests/" + "/robertaRosNode");
            ssh.command("mkdir -p /home/tests/" + "/robertaRosNode");
            ssh.copyLocalToRemote(binaryFile, "/home/tests/", fileName);

        } catch ( FileNotFoundException | TransportException | ConnectionException e ) {
            throw new IOException(e);
        }
    }

    /**
     * Sets the password for SSH communication with the Robotino.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the JSON device info needed for the server.
     *
     * @return the device info as a json object
     */
    public JSONObject getDeviceInfo() {
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("firmwarename", "Robotino");
        deviceInfo.put("robot", "robotino");
        deviceInfo.put("firmwareversion", this.firmwareVersion);
        deviceInfo.put("macaddr", "usb");
        deviceInfo.put("brickname", this.name);
        deviceInfo.put("battery", "1.0");
        return deviceInfo;
    }

    public String checkFirmwareVersion() throws UserAuthException, IOException {
        try (SshConnection ssh = new SshConnection(this.address, USERNAME, this.password)) {
            String msg = "CHANGETHIS";//ssh.command("naoqi-bin --version");
            String version = "CHANGETHIS";//msg.split("\n")[0].split(":")[1].trim();
            this.firmwareVersion = version.replace(".", "-");
            return this.firmwareVersion;
        } catch ( TransportException | ConnectionException e ) {
            throw new IOException(e);
        }
    }
}
