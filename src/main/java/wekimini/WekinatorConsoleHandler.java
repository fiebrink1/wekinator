/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import wekimini.gui.Console;

/**
 *
 * @author rebecca
 */
public class WekinatorConsoleHandler extends Handler {
//  private Console tmp = null;

    private Console console = null;
    //private final Formatter formatter = null;
    //private Level level = null;

    public WekinatorConsoleHandler() {
        setup();
    }

    public void setConsole(Console c) {
        this.console = c;
    }

    private void setup() {
        setLevel(Level.INFO);
        setFilter(null);
        //setFormatter(simpleformatter);
    }

    @Override
    public void publish(LogRecord record) {
        String message;
        if (!isLoggable(record)) {
            return;
        }
        try {
            //message = getFormatter().format(record);
            message = record.getMessage();
            if (record.getLevel() == Level.WARNING || record.getLevel() == Level.SEVERE ) {
                console.logWarning(message);
            } else {
                console.log(message);
            }
        } catch (Exception e) {
            //reportError(null, e, ErrorManager.FORMAT_FAILURE);
            reportError(null, e, ErrorManager.WRITE_FAILURE);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }
}
