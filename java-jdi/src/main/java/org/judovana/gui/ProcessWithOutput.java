package org.judovana.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class ProcessWithOutput extends JFrame {

    private final JTextArea output = new JTextArea();
    private Process cmd;

    public ProcessWithOutput() throws HeadlessException {
        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(output));
        this.setSize(300, 300);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JButton kill = new JButton("kill");
        this.add(kill, BorderLayout.SOUTH);
        kill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (cmd != null) {
                    cmd.destroy();
                    kill.setText("killed");
                }
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ProcessWithOutput.this.setVisible(true);
            }
        });
    }

    private void consume(InputStream in) {
        new Thread(new Runnable() {
            public void run() {
                InputStreamReader reader = new InputStreamReader(in);
                Scanner scan = new Scanner(reader);
                while (scan.hasNextLine()) {
                    String s = scan.nextLine();
                    synchronized (ProcessWithOutput.this) {
                        output.setText(output.getText() + "\n" + s);
                    }
                }
            }
        }).start();
    }

    public void run(String args) {
        new Thread(() -> runImpl(args)).start();
    }

    public void runImpl(String args) {
        try {
            cmdImpl(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.toString());
        }
    }

    private void cmdImpl(String args) throws IOException {
        cmd = Runtime.getRuntime().exec(args);
        final InputStream inStream = cmd.getInputStream();
        final InputStream errStream = cmd.getErrorStream();
        consume(inStream);
        consume(errStream);

    }
}

