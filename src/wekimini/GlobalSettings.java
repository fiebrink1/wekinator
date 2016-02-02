/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author rebecca
 */
public class GlobalSettings {
    private static GlobalSettings instance = null;
    private final Preferences prefs;
    
    /*private KadenzeSettings kadenzeSettings = new KadenzeSettings();
    public static final String PROP_KADENZESETTINGS = "kadenzeSettings"; */

    //In future, probably also want to add things like last save location, last Input/output configs, etc.
    
    protected GlobalSettings() {
        // Exists only to defeat instantiation.
        prefs = Preferences.userRoot().node(this.getClass().getName());
    }
    
    public static GlobalSettings getInstance() {
      if(instance == null) {
            instance = new GlobalSettings(); 
      }
      return instance;
   }
   
    public boolean hasKadenzeSaveLocation() {
        return prefs.getBoolean("hasKadenzeSaveLocation", false);
    }
    
    public void setKadenzeSaveLocation(String location) {
        prefs.putBoolean("hasKadenzeSaveLocation", true);
        prefs.put("kadenzeSaveLocation", location);
    }
    
    public void setStringValue(String key, String value) {
        prefs.put(key, value);
    }
    
    public String getStringValue(String key, String def) {
        return prefs.get(key, def);
    }
    
    public String getKadenzeSaveLocation() {
        return prefs.get("kadenzeSaveLocation", "");
    }
    
    public int getIntValue(String key, int def) {
        return prefs.getInt(key, def);
    }
    
    public void setIntValue(String key, int value) {
        prefs.putInt(key, value);
    }
    
    
    public void clearPreferences() throws BackingStoreException {
        prefs.clear();
    }
    
    public static void main(String[] args) {
        GlobalSettings s = GlobalSettings.getInstance();
        if (s.hasKadenzeSaveLocation()) {
            System.out.println("Has save location: " + s.getKadenzeSaveLocation());
        } else {
            System.out.println("Set location");
            s.setKadenzeSaveLocation("/Users/rebecca/kadenze");
        }
    }
    
}
