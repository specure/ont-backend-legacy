package at.alladin.rmbt.client;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class JTextAreaOutputStream extends OutputStream {

    private final JTextArea textArea;

    public JTextAreaOutputStream(JTextArea textArea) {
        if (textArea == null)
            throw new IllegalArgumentException("Destination is null");

        this.textArea = textArea;
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        final String text = new String(buffer, offset, length);
        if( SwingUtilities.isEventDispatchThread()) {
            textArea.append(text);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textArea.append(text);
                }
            });
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

}