package de.fhg.iais.roberta.connection.wired.karl;

import de.fhg.iais.roberta.connection.AutoConnector;
import de.fhg.iais.roberta.connection.IConnector;
import de.fhg.iais.roberta.connection.IRobot;
import de.fhg.iais.roberta.connection.wired.AbstractWiredRobot;
import de.fhg.iais.roberta.connection.wired.WiredRobotType;

public class Karl extends AbstractWiredRobot {
    /**
     * Constructor for wired robots.
     *
     * @param port the robot port
     */
    public Karl(String port) {
        super(WiredRobotType.KARL, port);
    }

    @Override
    public IConnector<? extends IRobot> createConnector() {
        return new KarlConnector(this);
    }
}
