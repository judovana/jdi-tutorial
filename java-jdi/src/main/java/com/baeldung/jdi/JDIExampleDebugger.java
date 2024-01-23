package com.baeldung.jdi;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.StepRequest;

public class JDIExampleDebugger {

    private Class debugClass; 
    private int[] breakPointLines;

    public Class getDebugClass() {
        return debugClass;
    }

    public void setDebugClass(Class debugClass) {
        this.debugClass = debugClass;
    }

    public int[] getBreakPointLines() {
        return breakPointLines;
    }

    public void setBreakPointLines(int[] breakPointLines) {
        this.breakPointLines = breakPointLines;
    }

    /**
     * Sets the debug class as the main argument in the connector and launches the VM
     * @return VirtualMachine
     * @throws IOException
     * @throws IllegalConnectorArgumentsException
     * @throws VMStartException
     */
    public VirtualMachine connectAndLaunchVM() throws IOException, IllegalConnectorArgumentsException, VMStartException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(getDebugClass().getName());
        VirtualMachine vm = launchingConnector.launch(arguments);
        return vm;
    }

    /**
     * thanx to http://wayne-adams.blogspot.com/2011/10/jdi-three-ways-to-attach-to-java.html
     * @param port
     * @return
     * @throws IOException
     * @throws IllegalConnectorArgumentsException
     * @throws VMStartException
     */
    public VirtualMachine connectToLaunchedPort(int port) throws IOException, IllegalConnectorArgumentsException, VMStartException {
        VirtualMachineManager vmMgr = Bootstrap.virtualMachineManager();
        AttachingConnector socketConnector = null;
        List<AttachingConnector> attachingConnectors = vmMgr.attachingConnectors();
        for (AttachingConnector ac: attachingConnectors) {
            if (ac.transport().name().equals("dt_socket")) {
                socketConnector = ac;
                break;
            }
        }
        if (socketConnector != null) {
            Map paramsMap = socketConnector.defaultArguments();
            Connector.IntegerArgument portArg = (Connector.IntegerArgument)paramsMap.get("port");
            portArg.setValue(port);
            VirtualMachine vm = socketConnector.attach(paramsMap);
            System.out.println("Attached to process '" + vm.name() + "'");
            return vm;
        }
        throw new RuntimeException("No vm found");
    }

    /**
     * Creates a request to prepare the debug class, add filter as the debug class and enables it
     * @param vm
     */
    public void enableClassPrepareRequest(VirtualMachine vm) {
        ClassPrepareRequest classPrepareRequest = vm.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debugClass.getName());
        classPrepareRequest.enable();
    }

    /**
     * Sets the break points at the line numbers mentioned in breakPointLines array
     * @param vm
     * @param event
     * @throws AbsentInformationException
     */
    public void setBreakPoints(VirtualMachine vm, ClassPrepareEvent event) throws AbsentInformationException {
        ClassType classType = (ClassType) event.referenceType();
        for(int lineNumber: getBreakPointLines()) {
            Location location = classType.locationsOfLine(lineNumber).get(0);
            BreakpointRequest bpReq = vm.eventRequestManager().createBreakpointRequest(location);
            bpReq.enable();
        }
    }

    /**
     * Displays the visible variables
     * @param event
     * @throws IncompatibleThreadStateException
     * @throws AbsentInformationException
     */
    public void displayVariables(LocatableEvent event) throws IncompatibleThreadStateException, AbsentInformationException {
        StackFrame stackFrame = event.thread().frame(0);
        if(stackFrame.location().toString().contains(debugClass.getName())) {
            Map<LocalVariable, Value> visibleVariables = stackFrame.getValues(stackFrame.visibleVariables());
            System.out.println("Variables at " +stackFrame.location().toString() +  " > ");
            for (Map.Entry<LocalVariable, Value> entry : visibleVariables.entrySet()) {
                System.out.println(entry.getKey().name() + " = " + entry.getValue());
            }
        }
        event.request().disable();
    }

    /**
     * Enables step request for a break point
     * @param vm
     * @param event
     */
    public void enableStepRequest(VirtualMachine vm, BreakpointEvent event) {
        //enable step request for last break point
        if (event.location().toString().contains(debugClass.getName() + ":" + getBreakPointLines()[0])) {
            StepRequest stepRequest = vm.eventRequestManager().createStepRequest(event.thread(), StepRequest.STEP_LINE, StepRequest.STEP_OVER);
            stepRequest.enable();    
        }
    }

    public static void main(String[] args) throws Exception {

        JDIExampleDebugger debuggerInstance = new JDIExampleDebugger();
        debuggerInstance.setDebugClass(JDIExampleDebuggee.class);
        int[] breakPoints = {14, 19};
        debuggerInstance.setBreakPointLines(breakPoints);
        VirtualMachine vm = null;

        //vm = debuggerInstance.connectAndLaunchVM();
        //debuggerInstance.enableClassPrepareRequest(vm);
        //above do not work, the class is not dound:(

        vm = debuggerInstance.connectToLaunchedPort(5005);

        int lineNumber = breakPoints[0];
        List<ReferenceType> refTypes = vm.allClasses();
        Location breakpointLocation = null;
        for (ReferenceType refType : refTypes) {
            if (breakpointLocation != null) {
                break;
            }
            if (JDIExampleDebuggee.class.getName().equals(refType.name())) {
                List<Location> locs = refType.allLineLocations();
                for (Location loc : locs) {
                    if (loc.lineNumber() == lineNumber) {
                        breakpointLocation = loc;
                        break;
                    }
                }
            }
        }
        if (breakpointLocation != null) {
            //thanx to http://wayne-adams.blogspot.com/2011/10/generating-minable-event-stream-with.html
            EventRequestManager evtReqMgr = vm.eventRequestManager();
            BreakpointRequest bReq = evtReqMgr.createBreakpointRequest(breakpointLocation);
            bReq.setSuspendPolicy(BreakpointRequest.SUSPEND_ALL);
            bReq.enable();
            EventQueue evtQueue = vm.eventQueue();
            while (true) {
                EventSet evtSet = evtQueue.remove();
                EventIterator evtIter = evtSet.eventIterator();
                while (evtIter.hasNext()) {
                    try {
                        Event evt = evtIter.next();
                        if (evt instanceof BreakpointEvent) {
                            EventRequest evtReq = evt.request();
                            if (evtReq instanceof BreakpointRequest) {
                                BreakpointRequest bpReq = (BreakpointRequest) evtReq;
                                if (bpReq.location().lineNumber() == lineNumber) {
                                    System.out.println("Breakpoint at line " + lineNumber + ": ");
                                    BreakpointEvent brEvt = (BreakpointEvent) evt;
                                    ThreadReference threadRef = brEvt.thread();
                                    StackFrame stackFrame = threadRef.frame(0);
                                    List<LocalVariable> visVars = stackFrame.visibleVariables();
                                    for (LocalVariable visibleVar : visVars) {
                                        Value val = stackFrame.getValue(visibleVar);
                                        String varNameValue = val.toString();
                                        System.out.println(visibleVar.name() + " = '" + varNameValue + "'");
                                    }
                                    debuggerInstance.enableStepRequest(vm, (BreakpointEvent) evt);
                                }
                            }
                        }
                        if (evt instanceof StepEvent) {
                            EventRequest evtReq = evt.request();
                            debuggerInstance.displayVariables((StepEvent) evt);
                        }

                    } catch (AbsentInformationException aie) {
                        System.out.println("AbsentInformationException: did you compile your target application with -g option?");
                    } catch (Exception exc) {
                        System.out.println(exc.getClass().getName() + ": " + exc.getMessage());
                    } finally {
                        evtSet.resume();
                    }
                }
            }
        }
    }
}
