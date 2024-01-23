/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rebecca
 */
public class StatusUpdateCenter {
    Wekinator w;
    private static final Logger logger = Logger.getLogger(StatusUpdateCenter.class.getName());
    
    
    public StatusUpdateCenter(Wekinator w) {
        this.w = w;
    }
    
    private StatusUpdate update = null;

    public static final String PROP_UPDATE = "update";

    /**
     * Get the value of update
     *
     * @return the value of update
     */
    public StatusUpdate getLastUpdate() {
        return update;
    }

    /**
     * Set the value of update
     *
     * @param caller the caller of this function
     * @param updateString the update string
     */
    public void update(Object caller, String updateString) {
        StatusUpdate newUpdate = new StatusUpdate(caller, updateString);
        StatusUpdate oldUpdate = this.update;
        this.update = newUpdate;
        logger.log(LoggingManager.USER_INFO, updateString);
        propertyChangeSupport.firePropertyChange(PROP_UPDATE, oldUpdate, update);
    }
    
    public void warn(Object caller, String updateString) {
        StatusUpdate newUpdate = new StatusUpdate(caller, updateString);
        StatusUpdate oldUpdate = this.update;
        this.update = newUpdate;
        logger.log(Level.WARNING, updateString);
        propertyChangeSupport.firePropertyChange(PROP_UPDATE, oldUpdate, update);
    }

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public class StatusUpdate {
        private final String callerClassName;
        private final String update;
        
        public StatusUpdate(Object caller, String update) {
            if (caller == null) {
                this.callerClassName = "";
            } else {
                this.callerClassName = caller.getClass().getName();
            }
            this.update = update;         
        }
        
        @Override
        public String toString() {
            return update;
        }
    };
    
}


