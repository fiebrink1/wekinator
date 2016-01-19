/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.kadenze;

import java.io.File;
import java.io.IOException;
import wekimini.GlobalSettings;

/**
 *
 * @author rebecca
 */
public class KadenzeLogging {
    private static KadenzeLogger logger = new KadenzeLogger();
    
    public void startLoggingForAssignment(int n) throws IOException {
        String dir = GlobalSettings.getInstance().getKadenzeSaveLocation();
        String myAssignmentDir = dir + File.separator + "assignment1" + File.separator;
        logger.beginLog(myAssignmentDir);
    }
    
    public void noLogging() {
        //ERROR: NOt implemented yet!
    }
    
    public KadenzeLogger getLogger() {
        return logger;
    }
}
