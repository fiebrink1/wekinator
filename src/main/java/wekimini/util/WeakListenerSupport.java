/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;

/**
 * Hilfsklasse fÃ¼r das Handling von anonymen Listener-Objekten verschiedener
 * Art, die trotz ihrer Referenz zum Elternobjekt entfernbar sein sollen.
 * 
 * Problem: Es gibt ein langlebiges Model-Objekt. Eine GUI, die dieses Model
 * bearbeiten soll, hÃ¤ngt ein PropertyChangeEvent in das Model ein, welches ein
 * dazugehÃ¶riges Eingabefeld aktualisieren soll.
 * 
 * Da dieser, von Model nun stark referenzierte Listener eine (wenn auch u.U.
 * implizite) Referenz zur GUI-Komponente besitzt, kann diese nicht vom
 * Garbage-Collector entfernt werden, solange das Model existiert.
 * 
 * Auf diese Weise kann es in einer langlebigen Anwendung dazu kommen, dass sich
 * die Anzahl der unsichtbaren GUI-Komponenten und Property- ChangeListener
 * bestÃ¤ndig erhÃ¶ht und eine Menge Performance kostet.
 * 
 * Eine LÃ¶sung ist es, eine schwache (weak) Referenz zwischen Modelobjekt und
 * Listener zu schaffen. Anonyme Listener, die sonst niemandem gehÃ¶ren, wÃ¼rden
 * dadurch jedoch sofort weggerÃ¤umt werden. Sie mÃ¼ssen also durch jemanden
 * referenziert werden, bis das Elternobjekt freigegeben werden kann.
 * 
 * Genau das leistet diese Klasse. Ihre Anwendung ist analog zur Klasse
 * PropertyChangeSupport: Das Elternobjekt definiert eine Instanzvariable mit
 * dem WeakListenerSupport. Alle in das Model eingehÃ¤ngten Listener werden durch
 * die Methoden dieses Objekts gekappselt (und vom Objekt in einer Liste
 * abgelegt, so dass sie vor Ã„nderungen geschÃ¼tzt sind).
 * 
 * Die Listener werden durch eine weitere Listener-Objekt gekappselt, welches
 * sie mittels WeakReference verknÃ¼pft. Sobald ein Event ausgelÃ¶st wird, schaut
 * der WeakListener nach, ob der Ziellistener noch existiert. Falls ja, wird das
 * Event weitergereicht. Falls nein wird durch den Aufruf der jeweiligen
 * Standardmethode einmalig versucht, den WeakListener aus dem Model zu
 * entfernen. Selbst wenn das nicht gelingt reduziert sich der dadurch
 * entstehende Memory-Leak jedoch gewaltig!
 * 
 * Ein weiteres nettes Feature, was man auf diese Weise enthÃ¤lt, ist die
 * MÃ¶glichkeit, alle Listener gleichzeitig durch den Aufruf von
 * "removeAll(Object)" aus einem Modelobjekt zu entfernen.
 * 
 * Anwendungsbeispiel:
 * 
 * <code>
 * public class MyPanel extends JPanel {
 *     private final WeakListenerSupport wls;
 *     private final JTextField textfield;
 *     private final Model model;
 *     
 *     public MyPanel(Model model) {
 *         this.model = model;
 *         this.wls = new WeakListenerSupport();
 *         this.textfield = new JTextField(model.getValue());
 *         this.model.addPropertyChangeListener("value", wls.propertyChange(new PropertyChangeListener() {
 * 
 * @Override public void propertyChange(PropertyChangeEvent event) {
 *           textfield.setText(model.getValue()); } }, this.model));
 *           add(this.textfield); } } </code>
 * 
 * @author Roland Tapken (java@rt.tasmiro.de)
 * @license Public Domain
 */
public class WeakListenerSupport {

    /**
     * SchÃ¼tzt die Listener solange vor dem GarbageCollector, bis der Besitzter
     * dieses Objekts ebenfalls weggerÃ¤umt werden kann. Jeder Listener, fÃ¼r den
     * ein WeakListener erzeugt wird, muss in diese Liste eingefÃ¼gt werden.
     */
    private ArrayList<Object> listeners = new ArrayList<Object>();

    /**
     * ErmÃ¶glicht die Entfernung aller Listener (eines bestimmten Model-Objekts)
     * auf einen Schlag. Es werden nur die Keys verwendet, im Objekt steht immer
     * null.
     */
    private WeakHashMap<WeakListener<?>, Object> weakListeners = new WeakHashMap<WeakListener<?>, Object>();

    /**
     * Entfernt alle Listeners und versetzt dieses Objekt quasi in den
     * Ausgangszustand zurÃ¼ck.
     */
    public void removeAll() {
        for (WeakListener<?> l : weakListeners.keySet()) {
            if (l != null) {
                l.unlink();
            }
        }
        listeners.clear();
        weakListeners.clear();
    }

    /**
     * Entfernt alle Listeners, die in einem bestimmten Objekt eingehÃ¤ngt sind.
     * Hinweis: Es wird nach exakt dem selben Model gesucht, es wird nicht
     * equals() verwendet.
     */
    public void removeAll(Object model) {
        for (WeakListener<?> l : new ArrayList<WeakListener<?>>(weakListeners.keySet())) {
            if (l != null && l.getModel() == model) {
                listeners.remove(l.getListener());
                weakListeners.remove(l);
                l.unlink();
            }
        }
    }

    public void remove(Object model, Object listener) {
        for (WeakListener<?> l : new ArrayList<WeakListener<?>>(weakListeners.keySet())) {
            if (l != null && l.getModel() == model && l.getListener() == listener) {
                listeners.remove(l.getListener());
                weakListeners.remove(l);
                l.unlink();
            }
        }
    }

    public WeakPropertyChangeListener propertyChange(PropertyChangeListener listener, Object model,
            String removeMethodName) {
        return new WeakPropertyChangeListener(this, listener, model, removeMethodName);
    }

    public WeakPropertyChangeListener propertyChange(PropertyChangeListener listener, Object model) {
        return propertyChange(listener, model, "removePropertyChangeListener");
    }

    public WeakPropertyChangeListener propertyChange(PropertyChangeListener listener) {
        return propertyChange(listener, null);
    }

    public WeakVetoableChangeListener vetoableChange(VetoableChangeListener listener, Object model,
            String removeMethodName) {
        return new WeakVetoableChangeListener(this, listener, model, removeMethodName);
    }

    public WeakVetoableChangeListener vetoableChange(VetoableChangeListener listener, Object model) {
        return vetoableChange(listener, model, "removePropertyChangeListener");
    }

    public WeakVetoableChangeListener vetoableChange(VetoableChangeListener listener) {
        return vetoableChange(listener, null);
    }

    /**
     * Container-Klasse fÃ¼r WeakListeners. Sie stellt einige Grundfunktionen
     * bereit. Das Template trÃ¤gt den Namen der Klasse des gekappselten
     * Listener-Objekts.
     */
    private abstract static class WeakListener<T> {
        private WeakReference<Object> model;
        private WeakReference<T> listener;
        private Class<?> listenerClass;
        private String removeListenerMethod;

        private WeakListener(WeakListenerSupport wls, T listener, Object model, String removeListenerMethod) {
            this.model = new WeakReference<Object>(model);
            this.listener = new WeakReference<T>(listener);
            this.listenerClass = listener == null ? null : listener.getClass();
            this.removeListenerMethod = removeListenerMethod == null ? "" : removeListenerMethod;

            // Register this at the support object
            wls.listeners.add(listener);
            wls.weakListeners.put(this, null);
        }

        public final Object getModel() {
            Object model = this.model == null ? null : this.model.get();
            return model == null ? null : model;
        }

        public final T getListener() {
            T listener = this.listener == null ? null : this.listener.get();
            return listener == null ? null : listener;
        }

        /**
         * Diese Methode versucht, den aktuellen Listener aus dem Zielobjekt zu
         * entfernen.
         */
        protected void unlink() {
            if (this.model != null) {
                try {
                    Object model = getModel();
                    if (model != null && !removeListenerMethod.isEmpty()) {
                        try {
                            Method m = model.getClass().getMethod(removeListenerMethod, listenerClass);
                            m.invoke(model, this);
                        } catch (Exception e) {
                        }
                    }
                } finally {
                    // Free as much as we have
                    this.model = null;
                    this.listener = null;
                    this.listenerClass = null;
                    this.removeListenerMethod = null;
                }
            }
        }
    }

    private static class WeakPropertyChangeListener extends WeakListener<PropertyChangeListener> implements
            PropertyChangeListener {
        private WeakPropertyChangeListener(WeakListenerSupport wls, PropertyChangeListener listener, Object model,
                String removeMethodName) {
            super(wls, listener, model, removeMethodName);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener listener = getListener();
            if (listener != null) {
                listener.propertyChange(evt);
            } else {
                unlink();
            }
        }
    }

    private static class WeakVetoableChangeListener extends WeakListener<VetoableChangeListener> implements
            VetoableChangeListener {
        private WeakVetoableChangeListener(WeakListenerSupport wls, VetoableChangeListener listener, Object model,
                String removeMethodName) {
            super(wls, listener, model, removeMethodName);
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            VetoableChangeListener listener = getListener();
            if (listener != null) {
                listener.vetoableChange(evt);
            } else {
                unlink();
            }
        }
    }

}
