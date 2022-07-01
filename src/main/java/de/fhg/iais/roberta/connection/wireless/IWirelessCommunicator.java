package de.fhg.iais.roberta.connection.wireless;

import net.schmizz.sshj.userauth.UserAuthException;

import java.io.IOException;

public interface IWirelessCommunicator {
    void setPassword(String password);
    void uploadFile(byte[] binaryFile, String fileName) throws IOException;

    }
