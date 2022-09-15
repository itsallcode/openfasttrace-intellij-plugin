package org.itallcode.openfasttrace.intelijplugin.remoterobot;

public final class RemoteRobotProperties {
    private RemoteRobotProperties () {
        // prevent class instantiation.
    }

    final public static int ROBOT_PORT = Integer.parseInt(System.getProperty("robot-server.port"));
    final public static String ROBOT_HOST = System.getProperty("robot-server.host.public", "localhost");
    final public static String ROBOT_BASE_URL = "http://" + ROBOT_HOST + ":" + ROBOT_PORT;
}
