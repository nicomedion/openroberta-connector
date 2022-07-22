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

/**
 * Communicator class for the robotino robot. Handles network communication between the robotino and the connector.
 */
public class RobotinoROSCommunicator implements IWirelessCommunicator {
    private static final Logger LOG = LoggerFactory.getLogger(RobotinoROSCommunicator.class);

    private static final String USERNAME = "robotino";

    private final String name;
    private final InetAddress address;

    private String password = "robotino";

    private final String workingDirectory;
    private String firmwareVersion = "";

    public RobotinoROSCommunicator(Robotino robotino) {
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
        if (password.isEmpty()){
            password = "robotino";
        }
        try (SshConnection ssh = new SshConnection(this.address, USERNAME, this.password)) {
            ssh.copyLocalToRemote(binaryFile, "/home/robotino/robertaPrograms", fileName);
            LOG.info("starting roslaunch...");
            //execute launch script and immediately move on

            ssh.command("workspacePath=$(/home/robotino/robertaPrograms/getWorkspace.sh);" +
                    " source /opt/ros/noetic/setup.bash; source  $workspacePath" +
                   " ;bash /home/robotino/robertaPrograms/launchRobertaScript.sh " + fileName + "&> /dev/null & disown $!");
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
        deviceInfo.put("robot", "robotinoROS");
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
