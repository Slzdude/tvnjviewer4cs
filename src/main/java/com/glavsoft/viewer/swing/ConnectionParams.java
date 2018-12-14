package com.glavsoft.viewer.swing;

import com.glavsoft.utils.Strings;

public class ConnectionParams {
    public static final int DEFAULT_SSH_PORT = 22;
    private static final int DEFAULT_RFB_PORT = 5900;
    public String hostName;
    private int portNumber;
    public String sshUserName;
    public String sshHostName;
    private int sshPortNumber;
    private boolean useSsh;

    public ConnectionParams(String hostName, int portNumber) {
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.sshUserName = "";
        this.sshHostName = "";
        this.sshPortNumber = 22;
        this.useSsh = false;
    }

    public ConnectionParams(String hostName, int portNumber, boolean useSsh, String sshHostName, int sshPortNumber, String sshUserName) {
        this(hostName, portNumber);
    }

    public ConnectionParams(ConnectionParams cp) {
        this.hostName = cp.hostName;
        this.portNumber = cp.portNumber;
        this.sshUserName = cp.sshUserName;
        this.sshHostName = cp.sshHostName;
        this.sshPortNumber = cp.sshPortNumber;
        this.useSsh = cp.useSsh;
    }

    public ConnectionParams() {
        this.hostName = "";
        this.sshUserName = "";
        this.sshHostName = "";
    }

    public boolean isHostNameEmpty() {
        return Strings.isTrimmedEmpty(this.hostName);
    }

    public void parseRfbPortNumber(String port) {
        try {
            this.portNumber = Integer.parseInt(port);
        } catch (NumberFormatException var3) {
        }

    }

    public void parseSshPortNumber(String port) {
        try {
            this.sshPortNumber = Integer.parseInt(port);
        } catch (NumberFormatException var3) {
        }

    }

    public boolean useSsh() {
        return this.useSsh && !Strings.isTrimmedEmpty(this.sshHostName);
    }

    public void setUseSsh(boolean useSsh) {
        this.useSsh = useSsh;
    }

    public int getPortNumber() {
        return 0 == this.portNumber ? 5900 : this.portNumber;
    }

    public int getSshPortNumber() {
        return 0 == this.sshPortNumber ? 22 : this.sshPortNumber;
    }

    public void completeEmptyFieldsFrom(ConnectionParams from) {
        if (null != from) {
            if (Strings.isTrimmedEmpty(this.hostName) && !Strings.isTrimmedEmpty(from.hostName)) {
                this.hostName = from.hostName;
            }

            if (0 == this.portNumber && from.portNumber != 0) {
                this.portNumber = from.portNumber;
            }

            if (Strings.isTrimmedEmpty(this.sshUserName) && !Strings.isTrimmedEmpty(from.sshUserName)) {
                this.sshUserName = from.sshUserName;
            }

            if (Strings.isTrimmedEmpty(this.sshHostName) && !Strings.isTrimmedEmpty(from.sshHostName)) {
                this.sshHostName = from.sshHostName;
            }

            if (0 == this.sshPortNumber && from.sshPortNumber != 0) {
                this.sshPortNumber = from.sshPortNumber;
            }

            this.useSsh |= from.useSsh;
        }
    }

    public String toString() {
        return this.hostName != null ? this.hostName : "";
    }

    public boolean equals(Object obj) {
        if (null != obj && obj instanceof ConnectionParams) {
            if (this == obj) {
                return true;
            } else {
                ConnectionParams o = (ConnectionParams) obj;
                return this.isEqualsNullable(this.hostName, o.hostName) && this.getPortNumber() == o.getPortNumber() && this.useSsh == o.useSsh && this.isEqualsNullable(this.sshHostName, o.sshHostName) && this.getSshPortNumber() == o.getSshPortNumber() && this.isEqualsNullable(this.sshUserName, o.sshUserName);
            }
        } else {
            return false;
        }
    }

    private boolean isEqualsNullable(String one, String another) {
        return one == another || (null == one ? "" : one).equals(null == another ? "" : another);
    }

    public int hashCode() {
        long hash = (long) ((this.hostName != null ? this.hostName.hashCode() : 0) + this.portNumber * 17 + (this.useSsh ? 781 : 693) + (this.sshHostName != null ? this.sshHostName.hashCode() : 0) * 23 + (this.sshUserName != null ? this.sshUserName.hashCode() : 0) * 37 + this.sshPortNumber * 41);
        return (int) hash;
    }
}
