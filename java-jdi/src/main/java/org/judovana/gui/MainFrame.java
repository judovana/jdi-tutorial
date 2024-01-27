package org.judovana.gui;

import com.baeldung.jdi.JDIExampleDebuggee;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.util.Optional;

public class MainFrame extends JFrame {

    private final JTextField url = new JTextField("localhost");
    private final JSpinner port = new JSpinner(new SpinnerNumberModel(5005, 999, 999999, 1));
    private final JTextField example = new JTextField(getHelpString());
    private final JSpinner debugeeTimeout = new JSpinner(new SpinnerNumberModel(100, 1, 1000, 10));
    private final JTextField exampleLaunch = new JTextField(getLaunch());
    JButton launch = new JButton("Start above process from this gui (rather start it from terminal :)");

    private String getHelpString() {
        return "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:" + port.getValue().toString();
    }

    private String getLaunch() {
        return getCommandOfCurrentProcess().orElseGet(() -> "java") + " -cp " + System.getProperty("java.class.path") + " " + getHelpString() + " " + JDIExampleDebuggee.class.getName() + " " + debugeeTimeout.getValue().toString();
    }

    public static Optional<String> getCommandOfCurrentProcess() {
        ProcessHandle processHandle = ProcessHandle.current();
        return processHandle.info().command();
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setup();
            }
        });
    }

    private void setup() {
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setLayout(new GridLayout(23, 1));
        this.add(new JLabel("host to connect to"));
        this.add(url);
        this.add(new JLabel("port to connect to"));
        this.add(port);
        this.add(new JLabel("target process have to be launch with debug allowed:"));
        example.setEditable(false);
        this.add(example);
        port.addChangeListener(changeEvent -> {
            example.setText(getHelpString());
            exampleLaunch.setText(getLaunch());
        });
        this.add(new JLabel("eg:"));
        this.add(new JLabel("Duration of exemplar process in seconds:"));
        this.add(debugeeTimeout);
        debugeeTimeout.addChangeListener(changeEvent -> exampleLaunch.setText(getLaunch()));
        exampleLaunch.setEditable(false);
        this.add(exampleLaunch);
        this.add(launch);
        launch.addActionListener(actionEvent -> new ProcessWithOutput().run(getLaunch()));
        this.add(new JLabel("Debugging:"));
        this.add(new JButton("attach"));
        this.add(new JButton("dettach"));
        this.add(new JButton("add breakpoint"));
        this.add(new JLabel("when paused:"));
        this.add(new JButton("step over"));
        this.add(new JButton("step step in"));
        this.add(new JButton("resume"));
        this.add(new JButton("show vars"));
        this.add(new JButton("show stack"));
        this.add(new JButton("show threads"));
        this.setSize(800, 600);
        this.setVisible(true);
    }
}
