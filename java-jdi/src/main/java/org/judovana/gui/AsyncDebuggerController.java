package org.judovana.gui;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AsyncDebuggerController {

    private final String host;
    private final int port;
    private VirtualMachine vm;

    public AsyncDebuggerController(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IllegalConnectorArgumentsException, IOException {
        if (vm != null) {
            throw new IOException("This connector is/was already connected.");
        }
        vm = connectToLaunchedPortImpl(host, port);
    }


    private static VirtualMachine connectToLaunchedPortImpl(String host, int port) throws IOException, IllegalConnectorArgumentsException {
        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector socketConnector = null;
        List<AttachingConnector> attachingConnectors = vmMgr.attachingConnectors();
        for (AttachingConnector ac : attachingConnectors) {
            if (ac.transport().name().equals("dt_socket")) {
                socketConnector = ac;
                break;
            }
        }
        if (socketConnector != null) {
            Map paramsMap = socketConnector.defaultArguments();
            Connector.IntegerArgument portArg = (Connector.IntegerArgument) paramsMap.get("port");
            Connector.StringArgument hostArg = (Connector.StringArgument) paramsMap.get("hostname");
            portArg.setValue(port);
            hostArg.setValue(host);
            VirtualMachine vm = socketConnector.attach(paramsMap);
            System.out.println("Attached to process '" + vm.name() + "'");
            return vm;
        }
        throw new IOException("No vm found");
    }
}
