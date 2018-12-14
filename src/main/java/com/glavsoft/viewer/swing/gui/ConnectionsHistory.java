package com.glavsoft.viewer.swing.gui;

import com.glavsoft.core.SettingsChangedEvent;
import com.glavsoft.rfb.IChangeSettingsListener;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.swing.ConnectionParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ConnectionsHistory implements IChangeSettingsListener {
    private static int MAX_ITEMS = 32;
    public static final String CONNECTIONS_HISTORY_ROOT_NODE = "com/glavsoft/viewer/connectionsHistory";
    public static final String NODE_HOST_NAME = "hostName";
    public static final String NODE_PORT_NUMBER = "portNumber";
    public static final String NODE_SSH_USER_NAME = "sshUserName";
    public static final String NODE_SSH_HOST_NAME = "sshHostName";
    public static final String NODE_SSH_PORT_NUMBER = "sshPortNumber";
    public static final String NODE_USE_SSH = "useSsh";
    public static final String NODE_PROTOCOL_SETTINGS = "protocolSettings";
    private Map settingsMap;
    LinkedList connections;
    private ConnectionParams workingConnectionParams;

    public ConnectionsHistory(ConnectionParams workingConnectionParams) {
        this.workingConnectionParams = workingConnectionParams;
        this.settingsMap = new HashMap();
        this.connections = new LinkedList();
        this.retrieve();
    }

    private void retrieve() {
        Preferences root = Preferences.userRoot();
        Preferences connectionsHistoryNode = root.node("com/glavsoft/viewer/connectionsHistory");

        try {
            byte[] emptyByteArray = new byte[0];
            String[] orderNums = connectionsHistoryNode.childrenNames();
            SortedMap conns = new TreeMap();
            HashSet uniques = new HashSet();
            String[] arr$ = orderNums;
            int len$ = orderNums.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                String orderNum = arr$[i$];
                int num = 0;

                try {
                    num = Integer.parseInt(orderNum);
                } catch (NumberFormatException var19) {
                }

                Preferences node = connectionsHistoryNode.node(orderNum);
                String hostName = node.get("hostName", (String) null);
                if (null != hostName) {
                    ConnectionParams cp = new ConnectionParams(hostName, node.getInt("portNumber", 0), node.getBoolean("useSsh", false), node.get("sshHostName", ""), node.getInt("sshPortNumber", 0), node.get("sshUserName", ""));
                    if (!uniques.contains(cp)) {
                        uniques.add(cp);
                        conns.put(num, cp);
                        byte[] bytes = node.getByteArray("protocolSettings", emptyByteArray);
                        if (bytes.length != 0) {
                            try {
                                ProtocolSettings settings = (ProtocolSettings) (new ObjectInputStream(new ByteArrayInputStream(bytes))).readObject();
                                settings.refine();
                                this.settingsMap.put(cp, settings);
                            } catch (IOException var17) {
                                Logger.getLogger(this.getClass().getName()).fine("Cannot deserialize ProtocolSettings: " + var17.getMessage());
                            } catch (ClassNotFoundException var18) {
                                Logger.getLogger(this.getClass().getName()).severe("Cannot deserialize ProtocolSettings : " + var18.getMessage());
                            }
                        }
                    }
                }
            }

            int itemsCount = 0;

            for (Iterator i$ = conns.values().iterator(); i$.hasNext(); ++itemsCount) {
                ConnectionParams cp = (ConnectionParams) i$.next();
                if (itemsCount < MAX_ITEMS) {
                    this.connections.add(cp);
                } else {
                    connectionsHistoryNode.node(cp.hostName).removeNode();
                }
            }
        } catch (BackingStoreException var20) {
            Logger.getLogger(this.getClass().getName()).severe("Cannot retrieve connections history info: " + var20.getMessage());
        }

    }

    public LinkedList getConnectionsList() {
        return this.connections;
    }

    public ProtocolSettings getSettings(ConnectionParams cp) {
        return (ProtocolSettings) this.settingsMap.get(cp);
    }

    public void save() {
        Preferences root = Preferences.userRoot();
        Preferences connectionsHistoryNode = root.node("com/glavsoft/viewer/connectionsHistory");

        try {
            String[] hosts = connectionsHistoryNode.childrenNames();
            String[] arr$ = hosts;
            int len$ = hosts.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                String host = arr$[i$];
                connectionsHistoryNode.node(host).removeNode();
            }
        } catch (BackingStoreException var8) {
            Logger.getLogger(this.getClass().getName()).severe("Cannot remove node: " + var8.getMessage());
        }

        int num = 0;
        Iterator i$ = this.connections.iterator();

        while (i$.hasNext()) {
            ConnectionParams cp = (ConnectionParams) i$.next();
            if (num >= MAX_ITEMS) {
                break;
            }

            if (!Strings.isTrimmedEmpty(cp.hostName)) {
                this.addNode(cp, connectionsHistoryNode, num++);
            }
        }

    }

    private void addNode(ConnectionParams connectionParams, Preferences connectionsHistoryNode, int orderNum) {
        ProtocolSettings settings = (ProtocolSettings) this.settingsMap.get(connectionParams);
        Preferences node = connectionsHistoryNode.node(String.valueOf(orderNum));
        node.put("hostName", connectionParams.hostName);
        node.putInt("portNumber", connectionParams.getPortNumber());
        if (connectionParams.useSsh()) {
            node.putBoolean("useSsh", connectionParams.useSsh());
            node.put("sshUserName", connectionParams.sshUserName != null ? connectionParams.sshUserName : "");
            node.put("sshHostName", connectionParams.sshHostName != null ? connectionParams.sshHostName : "");
            node.putInt("sshPortNumber", connectionParams.getSshPortNumber());
        }

        if (settings != null) {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(settings);
                node.putByteArray("protocolSettings", byteArrayOutputStream.toByteArray());
            } catch (IOException var9) {
                Logger.getLogger(this.getClass().getName()).severe("Cannot serialize ProtocolSettings: " + var9.getMessage());
            }
        }

        try {
            node.flush();
        } catch (BackingStoreException var8) {
            Logger.getLogger(this.getClass().getName()).severe("Cannot retrieve connections history info: " + var8.getMessage());
        }

    }

    void reorderConnectionsList(ConnectionParams connectionParams, ProtocolSettings settings) {
        while (this.connections.remove(connectionParams)) {
        }

        LinkedList cpList = new LinkedList();
        cpList.addAll(this.connections);
        this.connections.clear();
        this.connections.add(new ConnectionParams(connectionParams));
        this.connections.addAll(cpList);
        this.storeSettings(connectionParams, settings);
    }

    private void storeSettings(ConnectionParams connectionParams, ProtocolSettings settings) {
        ProtocolSettings savedSettings = (ProtocolSettings) this.settingsMap.get(connectionParams);
        if (savedSettings != null) {
            savedSettings.copySerializedFieldsFrom(settings);
        } else {
            this.settingsMap.put(new ConnectionParams(connectionParams), new ProtocolSettings(settings));
        }

    }

    public ConnectionParams getMostSuitableConnection(ConnectionParams orig) {
        ConnectionParams res = this.connections.isEmpty() ? orig : (ConnectionParams) this.connections.get(0);
        if (null != orig && null != orig.hostName) {
            Iterator i$ = this.connections.iterator();

            while (true) {
                while (i$.hasNext()) {
                    ConnectionParams cp = (ConnectionParams) i$.next();
                    if (orig.equals(cp)) {
                        return cp;
                    }

                    if (this.compareTextFields(orig.hostName, res.hostName, cp.hostName)) {
                        res = cp;
                    } else if (orig.hostName.equals(cp.hostName) && this.comparePorts(orig.getPortNumber(), res.getPortNumber(), cp.getPortNumber())) {
                        res = cp;
                    } else if (orig.hostName.equals(cp.hostName) && orig.getPortNumber() == cp.getPortNumber() && orig.useSsh() == cp.useSsh() && orig.useSsh() != res.useSsh()) {
                        res = cp;
                    } else if (orig.hostName.equals(cp.hostName) && orig.getPortNumber() == cp.getPortNumber() && orig.useSsh() && cp.useSsh() && this.compareTextFields(orig.sshHostName, res.sshHostName, cp.sshHostName)) {
                        res = cp;
                    } else if (orig.hostName.equals(cp.hostName) && orig.getPortNumber() == cp.getPortNumber() && orig.useSsh() && cp.useSsh() && orig.sshHostName != null && orig.sshHostName.equals(cp.hostName) && this.comparePorts(orig.getSshPortNumber(), res.getSshPortNumber(), cp.getSshPortNumber())) {
                        res = cp;
                    } else if (orig.hostName.equals(cp.hostName) && orig.getPortNumber() == cp.getPortNumber() && orig.useSsh() && cp.useSsh() && orig.sshHostName != null && orig.sshHostName.equals(cp.hostName) && orig.getSshPortNumber() == cp.getSshPortNumber() && this.compareTextFields(orig.sshUserName, res.sshUserName, cp.sshUserName)) {
                        res = cp;
                    }
                }

                return res;
            }
        } else {
            return res;
        }
    }

    private boolean comparePorts(int orig, int res, int test) {
        return orig == test && orig != res;
    }

    private boolean compareTextFields(String orig, String res, String test) {
        return orig != null && test != null && res != null && orig.equals(test) && !orig.equals(res);
    }

    public void settingsChanged(SettingsChangedEvent event) {
        this.storeSettings(this.workingConnectionParams, (ProtocolSettings) event.getSource());
        this.save();
    }
}
