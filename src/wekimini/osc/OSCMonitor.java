/*
 * Monitors OSC sending and receiving
 */
package wekimini.osc;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import wekimini.InputManager;

/**
 *
 * @author rebecca
 */
public class OSCMonitor {

    public static enum OSCReceiveState {

        NOT_CONNECTED, CONNECTED_NODATA, RECEIVING, RECEIVING_WRONG_NUMBER
    };

    private OSCReceiveState receiveState = OSCReceiveState.NOT_CONNECTED;
    public static final String PROP_RECEIVE_STATE = "receiveState";

    private boolean isSending = false;
    public static final String PROP_ISSENDING = "isSending";

    private final OSCReceiver recv;
    private final InputManager im;
    private final OSCSender os;

    private boolean hasReceivedRecently = false;
    private boolean hasSentRecently = false;

    private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture scheduledFuture;

    private boolean isMonitoring = false;

    public static final String PROP_ISMONITORING = "isMonitoring";

    public OSCMonitor(OSCReceiver recv, InputManager im, OSCSender os) {
        this.recv = recv;
        this.im = im;
        this.os = os;

        recv.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                receiverPropertyChanged(evt);
            }
        });

        im.addInputValueListener(new InputManager.InputListener() {

            @Override
            public void update(double[] vals) {
                inputReceived();
            }

            @Override
            public void notifyInputError() {
                inputError();
            }

            @Override
            public void updateBundle(List<List<Double>> values) {
                inputReceived();
            }
        });

        os.addSendEventListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                outputSent();
            }
        });

        if (recv.getConnectionState() == OSCReceiver.ConnectionState.CONNECTED) {
            setReceiveState(OSCReceiveState.CONNECTED_NODATA);
        } else {
           setReceiveState(OSCReceiveState.NOT_CONNECTED);
        }
    }

    /**
     * Get the value of isMonitoring
     *
     * @return the value of isMonitoring
     */
    public boolean isMonitoring() {
        return isMonitoring;
    }

    /**
     * Set the value of isMonitoring
     *
     * @param isMonitoring new value of isMonitoring
     */
    public void startMonitoring() {
        if (!isMonitoring) {
            scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if ((receiveState == OSCReceiveState.CONNECTED_NODATA ||
                            receiveState == OSCReceiveState.RECEIVING_WRONG_NUMBER)
                            && hasReceivedRecently) {
                        setReceiveState(OSCReceiveState.RECEIVING);
                    } else if ((receiveState == OSCReceiveState.RECEIVING 
                            || receiveState == OSCReceiveState.CONNECTED_NODATA
                            || receiveState == OSCReceiveState.RECEIVING_WRONG_NUMBER)
                        && !hasReceivedRecently) {
                        setReceiveState(OSCReceiveState.CONNECTED_NODATA);
                    }

                    if (isSending != hasSentRecently) {
                        setIsSending(hasSentRecently);
                    }

                    hasReceivedRecently = false;
                    hasSentRecently = false;
                }
            }, 500, 500, TimeUnit.MILLISECONDS);

            boolean oldIsMonitoring = false;
            isMonitoring = true;
            propertyChangeSupport.firePropertyChange(PROP_ISMONITORING, oldIsMonitoring, isMonitoring);
        }

    }

    public void stopMonitoring() {
        if (isMonitoring) {
            scheduledFuture.cancel(true);

            boolean oldIsMonitoring = true;
            isMonitoring = false;
            propertyChangeSupport.firePropertyChange(PROP_ISMONITORING, oldIsMonitoring, isMonitoring);
        }
    }

    private void receiverPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == OSCReceiver.PROP_CONNECTIONSTATE) {
            if ((OSCReceiver.ConnectionState)evt.getNewValue() != OSCReceiver.ConnectionState.CONNECTED) {
                setReceiveState(OSCReceiveState.NOT_CONNECTED);
            } else if (hasReceivedRecently) {
                setReceiveState(OSCReceiveState.RECEIVING);
            } else {
                setReceiveState(OSCReceiveState.CONNECTED_NODATA);
            }
        }
    }

    private void inputReceived() {
        hasReceivedRecently = true;
    }

    private void outputSent() {
        hasSentRecently = true;
    }

    /**
     * Get the value of isSending
     *
     * @return the value of isSending
     */
    public boolean isSending() {
        return isSending;
    }

    /**
     * Set the value of isSending
     *
     * @param isSending new value of isSending
     */
    private void setIsSending(boolean isSending) {
        boolean oldIsSending = this.isSending;
        this.isSending = isSending;
        propertyChangeSupport.firePropertyChange(PROP_ISSENDING, oldIsSending, isSending);
    }
    
    private void inputError() {
        setReceiveState(OSCReceiveState.RECEIVING_WRONG_NUMBER);
    }

    /**
     * Get the value of isReceiverConnected
     *
     * @return the value of isReceiverConnected
     */
    public OSCReceiveState getReceiveState() {
        return receiveState;
    }

    /**
     * Set the value of isReceiverConnected
     *
     * @param isReceiverConnected new value of isReceiverConnected
     */
    private void setReceiveState(OSCReceiveState newState) {
        OSCReceiveState oldState = this.receiveState;
        receiveState = newState;
        propertyChangeSupport.firePropertyChange(PROP_RECEIVE_STATE, oldState, newState);
    }

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

}
