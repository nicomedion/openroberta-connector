package de.fhg.iais.roberta.connection.wired.karl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.iais.roberta.connection.AbstractConnector;
import de.fhg.iais.roberta.connection.ServerCommunicator;
import de.fhg.iais.roberta.util.Pair;

public class KarlConnector extends AbstractConnector<Karl> {
    private static final Logger LOG = LoggerFactory.getLogger(KarlConnector.class);
    private KarlCommunicator karlCommunicator;

    public KarlConnector(Karl robot) {
        super(robot);
    }

    @Override
    protected void runLoopBody() {
        //LOG.info(String.valueOf(this.state));
        switch ( this.state ) {
            case DISCOVER:
                this.karlCommunicator = new KarlCommunicator();
                this.fire(State.WAIT_FOR_CONNECT_BUTTON_PRESS);
                break;
            case CONNECT_BUTTON_IS_PRESSED:
                try {
                    this.brickData = new JSONObject();
                    this.brickData.put("Karl", true);
                    JSONObject serverResponse = this.serverCommunicator.pushRequest(this.brickData);
                    String command = serverResponse.getString("cmd");

                    switch ( command ) {
                        case CMD_REPEAT:
                            this.fire(State.WAIT_FOR_CMD);
                            LOG.info("Robot successfully registered with token {}, waiting for commands", this.token);
                            break;
                        case CMD_ABORT:
                            LOG.info("registration timeout");
                            this.fire(State.TOKEN_TIMEOUT);
                            this.fire(State.DISCOVER);
                            break;
                        default:
                            LOG.error("Unexpected command {} from server", command);
                            this.reset(State.ERROR_HTTP);
                    }
                } catch ( IOException e ) {
                    LOG.error("CONNECT {}", e.getMessage());
                    this.reset(State.ERROR_HTTP);
                }
                break;
            case WAIT_FOR_CMD:
                try {
                    JSONObject response = this.serverCommunicator.pushRequest(this.brickData);
                    String cmdKey = response.getString(KEY_CMD);
                    if ( cmdKey.equals(CMD_REPEAT) ) {
                        break;
                    } else if ( cmdKey.equals(CMD_DOWNLOAD) ) {
                        LOG.info("Download user program");
                        try {
                            Pair<byte[], String> program = this.serverCommunicator.downloadProgram(this.brickData);
                            System.out.println(new String(program.getFirst()));
                            File temp = File.createTempFile(program.getSecond(), ".py");
                            temp.deleteOnExit();

                            if ( !temp.exists() ) {
                                throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
                            }

                            try (FileOutputStream os = new FileOutputStream(temp)) {
                                os.write(program.getFirst());
                            }

                            this.fire(State.WAIT_UPLOAD);
                            boolean result = this.karlCommunicator.uploadFile(this.robot.getPort(), temp.getAbsolutePath());
                            if (!result) {
                                this.fire(State.ERROR_UPLOAD_TO_ROBOT);
                                this.fire(State.WAIT_FOR_CMD);
                            }

                        }catch ( Exception e ){
                            e.printStackTrace();
                        }
                    }
                } catch ( IOException e ) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                break;
        }
    }
}