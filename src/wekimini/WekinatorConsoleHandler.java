/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.swing.JTextArea;

/**
 *
 * @author rebecca
 */
public class WekinatorConsoleHandler extends Handler {
//  private Console tmp = null;

    private JTextArea textArea = null;
    private final Formatter formatter = null;
    private Level level = null;

    public WekinatorConsoleHandler() {
        setup();
    }

    public void setTextArea(JTextArea textArea) {
        this.textArea = textArea;
    }

    private void setup() {
        setLevel(Level.INFO);
        setFilter(null);
        setFormatter(new SimpleFormatter());
    }

    @Override
    public void publish(LogRecord record) {
        String message = null;
        if (!isLoggable(record)) {
            return;
        }
        try {
            message = getFormatter().format(record);
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FORMAT_FAILURE);
        }

        try {
            textArea.append(message);
        } catch (Exception ex) {
            reportError(null, ex, ErrorManager.WRITE_FAILURE);
        }

    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }
}
