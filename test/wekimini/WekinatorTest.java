/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import wekimini.gui.MainGUI;
import wekimini.osc.OSCMonitor;
import wekimini.osc.OSCReceiver;
import wekimini.osc.OSCSender;

/**
 *
 * @author louismccallum
 */
public class WekinatorTest {
    
    public WekinatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getID method, of class Wekinator.
     */
    @Test
    public void testGetID() {
        System.out.println("getID");
        Wekinator instance = null;
        int expResult = 0;
        int result = instance.getID();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addCloseListener method, of class Wekinator.
     */
    @Test
    public void testAddCloseListener() {
        System.out.println("addCloseListener");
        ChangeListener l = null;
        Wekinator instance = null;
        instance.addCloseListener(l);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeCloseListener method, of class Wekinator.
     */
    @Test
    public void testRemoveCloseListener() {
        System.out.println("removeCloseListener");
        ChangeListener l = null;
        Wekinator instance = null;
        instance.removeCloseListener(l);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of close method, of class Wekinator.
     */
    @Test
    public void testClose() {
        System.out.println("close");
        Wekinator instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addPropertyChangeListener method, of class Wekinator.
     */
    @Test
    public void testAddPropertyChangeListener() {
        System.out.println("addPropertyChangeListener");
        PropertyChangeListener listener = null;
        Wekinator instance = null;
        instance.addPropertyChangeListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removePropertyChangeListener method, of class Wekinator.
     */
    @Test
    public void testRemovePropertyChangeListener() {
        System.out.println("removePropertyChangeListener");
        PropertyChangeListener listener = null;
        Wekinator instance = null;
        instance.removePropertyChangeListener(listener);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getStatusUpdateCenter method, of class Wekinator.
     */
    @Test
    public void testGetStatusUpdateCenter() {
        System.out.println("getStatusUpdateCenter");
        Wekinator instance = null;
        StatusUpdateCenter expResult = null;
        StatusUpdateCenter result = instance.getStatusUpdateCenter();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getTrainingRunner method, of class Wekinator.
     */
    @Test
    public void testGetTrainingRunner() {
        System.out.println("getTrainingRunner");
        Wekinator instance = null;
        TrainingRunner expResult = null;
        TrainingRunner result = instance.getTrainingRunner();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSupervisedLearningManager method, of class Wekinator.
     */
    @Test
    public void testGetSupervisedLearningManager() {
        System.out.println("getSupervisedLearningManager");
        Wekinator instance = null;
        SupervisedLearningManager expResult = null;
        SupervisedLearningManager result = instance.getSupervisedLearningManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDtwLearningManager method, of class Wekinator.
     */
    @Test
    public void testGetDtwLearningManager() {
        System.out.println("getDtwLearningManager");
        Wekinator instance = null;
        DtwLearningManager expResult = null;
        DtwLearningManager result = instance.getDtwLearningManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDataManager method, of class Wekinator.
     */
    @Test
    public void testGetDataManager() {
        System.out.println("getDataManager");
        Wekinator instance = null;
        DataManager expResult = null;
        DataManager result = instance.getDataManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getInputManager method, of class Wekinator.
     */
    @Test
    public void testGetInputManager() {
        System.out.println("getInputManager");
        Wekinator instance = null;
        InputManager expResult = null;
        InputManager result = instance.getInputManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOutputManager method, of class Wekinator.
     */
    @Test
    public void testGetOutputManager() {
        System.out.println("getOutputManager");
        Wekinator instance = null;
        OutputManager expResult = null;
        OutputManager result = instance.getOutputManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMainGUI method, of class Wekinator.
     */
    @Test
    public void testGetMainGUI() {
        System.out.println("getMainGUI");
        Wekinator instance = null;
        MainGUI expResult = null;
        MainGUI result = instance.getMainGUI();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOSCMonitor method, of class Wekinator.
     */
    @Test
    public void testGetOSCMonitor() {
        System.out.println("getOSCMonitor");
        Wekinator instance = null;
        OSCMonitor expResult = null;
        OSCMonitor result = instance.getOSCMonitor();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOSCReceiver method, of class Wekinator.
     */
    @Test
    public void testGetOSCReceiver() {
        System.out.println("getOSCReceiver");
        Wekinator instance = null;
        OSCReceiver expResult = null;
        OSCReceiver result = instance.getOSCReceiver();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOSCSender method, of class Wekinator.
     */
    @Test
    public void testGetOSCSender() {
        System.out.println("getOSCSender");
        Wekinator instance = null;
        OSCSender expResult = null;
        OSCSender result = instance.getOSCSender();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getWekinatorController method, of class Wekinator.
     */
    @Test
    public void testGetWekinatorController() {
        System.out.println("getWekinatorController");
        Wekinator instance = null;
        WekinatorController expResult = null;
        WekinatorController result = instance.getWekinatorController();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultDirectory method, of class Wekinator.
     */
    @Test
    public void testGetDefaultDirectory() {
        System.out.println("getDefaultDirectory");
        String expResult = "";
        String result = Wekinator.getDefaultDirectory();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDefaultNextProjectName method, of class Wekinator.
     */
    @Test
    public void testGetDefaultNextProjectName() {
        System.out.println("getDefaultNextProjectName");
        String expResult = "";
        String result = Wekinator.getDefaultNextProjectName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of TestingWekinator method, of class Wekinator.
     */
    @Test
    public void testTestingWekinator() throws Exception {
        System.out.println("TestingWekinator");
        Wekinator expResult = null;
        Wekinator result = Wekinator.TestingWekinator();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLearningManager method, of class Wekinator.
     */
    @Test
    public void testGetLearningManager() {
        System.out.println("getLearningManager");
        Wekinator instance = null;
        LearningManager expResult = null;
        LearningManager result = instance.getLearningManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLoggingManager method, of class Wekinator.
     */
    @Test
    public void testGetLoggingManager() {
        System.out.println("getLoggingManager");
        Wekinator instance = null;
        LoggingManager expResult = null;
        LoggingManager result = instance.getLoggingManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of registerForMacOSXEvents method, of class Wekinator.
     */
    @Test
    public void testRegisterForMacOSXEvents() {
        System.out.println("registerForMacOSXEvents");
        Wekinator instance = null;
        instance.registerForMacOSXEvents();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of about method, of class Wekinator.
     */
    @Test
    public void testAbout() {
        System.out.println("about");
        Wekinator instance = null;
        instance.about();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of preferences method, of class Wekinator.
     */
    @Test
    public void testPreferences() {
        System.out.println("preferences");
        Wekinator instance = null;
        instance.preferences();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProjectName method, of class Wekinator.
     */
    @Test
    public void testGetProjectName() {
        System.out.println("getProjectName");
        Wekinator instance = null;
        String expResult = "";
        String result = instance.getProjectName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProjectName method, of class Wekinator.
     */
    @Test
    public void testSetProjectName() {
        System.out.println("setProjectName");
        String projectName = "";
        Wekinator instance = null;
        instance.setProjectName(projectName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProjectLocation method, of class Wekinator.
     */
    @Test
    public void testGetProjectLocation() {
        System.out.println("getProjectLocation");
        Wekinator instance = null;
        String expResult = "";
        String result = instance.getProjectLocation();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProjectLocation method, of class Wekinator.
     */
    @Test
    public void testSetProjectLocation() {
        System.out.println("setProjectLocation");
        String projectLocation = "";
        Wekinator instance = null;
        instance.setProjectLocation(projectLocation);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hasSaveLocation method, of class Wekinator.
     */
    @Test
    public void testHasSaveLocation() {
        System.out.println("hasSaveLocation");
        Wekinator instance = null;
        boolean expResult = false;
        boolean result = instance.hasSaveLocation();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setHasSaveLocation method, of class Wekinator.
     */
    @Test
    public void testSetHasSaveLocation() {
        System.out.println("setHasSaveLocation");
        boolean hasSaveLocation = false;
        Wekinator instance = null;
        instance.setHasSaveLocation(hasSaveLocation);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveAs method, of class Wekinator.
     */
    @Test
    public void testSaveAs() throws Exception {
        System.out.println("saveAs");
        String name = "";
        File projectDir = null;
        Wekinator instance = null;
        instance.saveAs(name, projectDir);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of save method, of class Wekinator.
     */
    @Test
    public void testSave() {
        System.out.println("save");
        Wekinator instance = null;
        instance.save();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of prepareToDie method, of class Wekinator.
     */
    @Test
    public void testPrepareToDie() {
        System.out.println("prepareToDie");
        Wekinator instance = null;
        instance.prepareToDie();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of showConsole method, of class Wekinator.
     */
    @Test
    public void testShowConsole() {
        System.out.println("showConsole");
        Wekinator instance = null;
        instance.showConsole();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
