/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import wekimini.kadenze.KadenzeAssn3SubmissionPrompt;
import wekimini.kadenze.KadenzeAssignmentSummaryFrame;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import wekimini.DataManager;
import wekimini.DtwLearningManager;
import wekimini.GlobalSettings;
import wekimini.LearningManager;
import wekimini.LearningManager.LearningType;
import wekimini.Path;
import wekimini.SupervisedLearningManager;
import wekimini.SupervisedLearningManager.RunningState;
import wekimini.WekiMiniRunner.Closeable;
import wekimini.gui.path.PathEditorFrame;
import wekimini.util.Util;
import wekimini.WekiMiniRunner;
import wekimini.Wekinator;
import wekimini.WekinatorFileData;
import wekimini.WekinatorSaver;
import wekimini.dtw.gui.DtwEditorFrame;
import wekimini.dtw.gui.DtwLearningPanel;
import wekimini.dtw.gui.DtwOutputEditor;
import wekimini.kadenze.KadenzeAssignment;
import wekimini.kadenze.KadenzeAssignment.KadenzeAssignmentType;
import wekimini.kadenze.KadenzeAssn1SubmissionPrompt;
import wekimini.kadenze.KadenzeAssn2SubmissionPrompt;
import wekimini.kadenze.KadenzeAssn6SubmissionPrompt;
import wekimini.kadenze.KadenzeAssn7SubmissionPrompt;
import wekimini.kadenze.KadenzeAssn4SubmissionPrompt1;
import wekimini.kadenze.KadenzeAssn4SubmissionPrompt2;
import wekimini.kadenze.KadenzeLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.learning.dtw.DtwModel;

/**
 *
 * @author rebecca
 */
public class MainGUI extends javax.swing.JFrame implements Closeable {

    private OSCInputStatusFrame oscInputStatusFrame = null;
    private InputMonitor inputMonitorFrame = null;
    private WekiArffLoader arffLoader = null;
    private OutputViewerTable outputTableWindow = null;
    private DtwOutputEditor dtwOutputEditor = null;
    private ModelEvaluationFrame modelEvaluationFrame = null;
    private InputOutputConnectionsEditor inputOutputConnectionsWindow = null;
    private final Wekinator w;
    private boolean closeable = true; //flaseif this is the last window open
    private static final Logger logger = Logger.getLogger(MainGUI.class.getName());
    private JMenuItem[] kadenzeMenuItems = new JMenuItem[0];
    private DtwLearningPanel dtwLearningPanel1;

    /**
     * Creates new form MainGUI
     */
    public MainGUI(Wekinator w, LearningManager.LearningType type) {
        initComponents();
        if (type == LearningManager.LearningType.INITIALIZATION) {
            throw new IllegalStateException("GUI can only be created for Wekinator whose learning type is known");
        }

        this.w = w;
        setGUIForWekinator(type);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                //int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to quit?", "Quit?", JOptionPane.YES_NO_OPTION);
                int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to close this project?", "Close project?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, WekiMiniRunner.getIcon());

                if (option == JOptionPane.YES_OPTION) {
                    finishUp();
                }

            }
        });
        //menuTemp.setVisible(false); 
    }

    private void finishUp() {
        KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.PROJECT_CLOSED);
        KadenzeLogging.getLogger().flush(); //Imperfect: Log won't be closed if this is last GUI and we haven't submitted yet...
        w.close();
        this.dispose();
    }

    private void setGUIForWekinator(LearningManager.LearningType type) {
        this.setTitle(w.getProjectName());
        menuItemSave.setEnabled(w.hasSaveLocation());
        w.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                wekinatorPropertyChanged(evt);
            }
        });

        if (type == LearningManager.LearningType.SUPERVISED_LEARNING) {
            initializeForSupervisedLearning();
        } else if (type == LearningManager.LearningType.TEMPORAL_MODELING) {
            initializeForTemporalModeling();
        }

        initKadenzeMenu();

    }

    private void initKadenzeMenu() {
        menuKadenze.setVisible(WekiMiniRunner.isKadenze());
        if (!WekiMiniRunner.isKadenze()) {
            return;
        }

        //Add sub-menus here
        addKadenzeMenus();
        updateKadenzeMenus();

        //Add listeners
        KadenzeLogging.addListener(new KadenzeLogging.KadenzeListener() {

            //This is called when part changes as well
            @Override
            public void assignmentChanged(KadenzeAssignmentType ka) {
                updateKadenzeMenus();
            }

            @Override
            public void assignmentStarted(KadenzeAssignmentType ka) {
            }

            @Override
            public void assignmentStopped() {
                updateKadenzeMenus();
            }
        });
    }

    private void addKadenzeMenus() {
        KadenzeAssignmentType ka = KadenzeLogging.getCurrentAssignmentType();
        int whichMainAssignment = KadenzeAssignment.getAssignmentNumber(ka);

        if (whichMainAssignment == 1) {
            makeKadenzeAssignment1Menu(ka);
        } else if (whichMainAssignment == 2) {
            makeKadenzeAssignment2Menu(ka);
        } else if (whichMainAssignment == 3) {
            makeKadenzeAssignment3Menu(ka);
        } else if (whichMainAssignment == 4) {
            makeKadenzeAssignment4Menu(ka);
        } else if (whichMainAssignment == 6) {
            makeKadenzeAssignment6Menu(ka);
        } else if (whichMainAssignment == 7) {
            makeKadenzeAssignment7Menu(ka);
        } else {
            logger.log(Level.WARNING, "Unknown assignment :" + ka);
        }
    }

    private void makeKadenzeAssignment1Menu(final KadenzeAssignmentType ka) {
        //Don't need any sub-menus
        kadenzeMenuItems = new JMenuItem[2];
        JMenuItem k1 = new JMenuItem("Doing Assignment 1, Part 1A");
        k1.setEnabled(false);
        menuKadenze.add(k1);
        kadenzeMenuItems[0] = k1;

        JMenuItem k2 = new JMenuItem("Create Kadenze Assignment 1 submission");
        k2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAssignmentSubmission();
            }
        });
        menuKadenze.add(k2);
        kadenzeMenuItems[1] = k2;
    }

    private void makeKadenzeAssignment6Menu(final KadenzeAssignmentType ka) {
        kadenzeMenuItems = new JMenuItem[4];
        int subPart = KadenzeAssignment.getAssignmentSubPart(ka); //1 through 6
        for (int i = 0; i < 2; i++) {
            String s;
            if (i == (subPart - 1)) {
                s = "Doing ";
            } else {
                s = "Start ";
            }
            s = s + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(6, i + 1));
            kadenzeMenuItems[i] = new JMenuItem(s);
            if (i == (subPart - 1)) {
                kadenzeMenuItems[i].setEnabled(false);
            }
            menuKadenze.add(kadenzeMenuItems[i]);
        }

        addKadenzeListener(kadenzeMenuItems[0], KadenzeAssignmentType.ASSIGNMENT6_PART1);
        addKadenzeListener(kadenzeMenuItems[1], KadenzeAssignmentType.ASSIGNMENT6_PART2);

        kadenzeMenuItems[2] = new JMenuItem();
        addKadenzeSummaryItem(menuKadenze, kadenzeMenuItems[2]);

        kadenzeMenuItems[3] = new JMenuItem("Create Kadenze Assignment 6 submission");
        kadenzeMenuItems[3].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAssignmentSubmission();
            }
        });
        menuKadenze.add(kadenzeMenuItems[3]);
    }

    private void makeKadenzeAssignment4Menu(final KadenzeAssignmentType ka) {
        kadenzeMenuItems = new JMenuItem[6];
        int subPart = KadenzeAssignment.getAssignmentSubPart(ka); //1 through 6
        for (int i = 0; i < 4; i++) {
            String s;
            if (i == (subPart - 1)) {
                s = "Doing ";
            } else {
                s = "Start ";
            }
            s = s + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(4, i + 1));
            kadenzeMenuItems[i] = new JMenuItem(s);
            if (i == (subPart - 1)) {
                kadenzeMenuItems[i].setEnabled(false);
            }
            /* kadenzeMenuItems[i].addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
             switchToAssignment(KadenzeAssignment.getAssignmentNumber(ka), i);
             }
             }); */
            menuKadenze.add(kadenzeMenuItems[i]);
        }

        addKadenzeListener(kadenzeMenuItems[0], KadenzeAssignmentType.ASSIGNMENT4_PART1A);
        addKadenzeListener(kadenzeMenuItems[1], KadenzeAssignmentType.ASSIGNMENT4_PART1B);
        addKadenzeListener(kadenzeMenuItems[2], KadenzeAssignmentType.ASSIGNMENT4_PART1C);
        addKadenzeListener(kadenzeMenuItems[3], KadenzeAssignmentType.ASSIGNMENT4_PART2);

        kadenzeMenuItems[4] = new JMenuItem();
        addKadenzeSummaryItem(menuKadenze, kadenzeMenuItems[4]);

        kadenzeMenuItems[5] = new JMenuItem("Create Kadenze Assignment 4 submission");
        kadenzeMenuItems[5].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAssignmentSubmission();
            }
        });
        menuKadenze.add(kadenzeMenuItems[5]);
    }

    private void makeKadenzeAssignment3Menu(final KadenzeAssignmentType ka) {
        kadenzeMenuItems = new JMenuItem[8];
        int subPart = KadenzeAssignment.getAssignmentSubPart(ka); //1 through 6
        for (int i = 0; i < 6; i++) {
            String s;
            if (i == (subPart - 1)) {
                s = "Doing ";
            } else {
                s = "Start ";
            }
            s = s + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(3, i + 1));
            kadenzeMenuItems[i] = new JMenuItem(s);
            if (i == (subPart - 1)) {
                kadenzeMenuItems[i].setEnabled(false);
            }
            /* kadenzeMenuItems[i].addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
             switchToAssignment(KadenzeAssignment.getAssignmentNumber(ka), i);
             }
             }); */
            menuKadenze.add(kadenzeMenuItems[i]);
        }

        addKadenzeListener(kadenzeMenuItems[0], KadenzeAssignmentType.ASSIGNMENT3_PART1A);
        addKadenzeListener(kadenzeMenuItems[1], KadenzeAssignmentType.ASSIGNMENT3_PART1B);
        addKadenzeListener(kadenzeMenuItems[2], KadenzeAssignmentType.ASSIGNMENT3_PART1C);
        addKadenzeListener(kadenzeMenuItems[3], KadenzeAssignmentType.ASSIGNMENT3_PART2);
        addKadenzeListener(kadenzeMenuItems[4], KadenzeAssignmentType.ASSIGNMENT3_PART3A);
        addKadenzeListener(kadenzeMenuItems[5], KadenzeAssignmentType.ASSIGNMENT3_PART3B);

        kadenzeMenuItems[6] = new JMenuItem();
        addKadenzeSummaryItem(menuKadenze, kadenzeMenuItems[6]);

        kadenzeMenuItems[7] = new JMenuItem("Create Kadenze Assignment 3 submission");
        kadenzeMenuItems[7].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAssignmentSubmission();
            }
        });
        menuKadenze.add(kadenzeMenuItems[7]);
    }

    private void makeKadenzeAssignment2Menu(final KadenzeAssignmentType ka) {
        kadenzeMenuItems = new JMenuItem[9];
        int subPart = KadenzeAssignment.getAssignmentSubPart(ka); //1 through 7
        for (int i = 0; i < 7; i++) {
            String s;
            if (i == (subPart - 1)) {
                s = "Doing ";
            } else {
                s = "Start ";
            }
            s = s + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(2, i + 1));
            kadenzeMenuItems[i] = new JMenuItem(s);
            if (i == (subPart - 1)) {
                kadenzeMenuItems[i].setEnabled(false);
            }
            /* kadenzeMenuItems[i].addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
             switchToAssignment(KadenzeAssignment.getAssignmentNumber(ka), i);
             }
             }); */
            menuKadenze.add(kadenzeMenuItems[i]);
        }

        addKadenzeListener(kadenzeMenuItems[0], KadenzeAssignmentType.ASSIGNMENT2_PART1A);
        addKadenzeListener(kadenzeMenuItems[1], KadenzeAssignmentType.ASSIGNMENT2_PART1B);
        addKadenzeListener(kadenzeMenuItems[2], KadenzeAssignmentType.ASSIGNMENT2_PART1C);
        addKadenzeListener(kadenzeMenuItems[3], KadenzeAssignmentType.ASSIGNMENT2_PART1D);
        addKadenzeListener(kadenzeMenuItems[4], KadenzeAssignmentType.ASSIGNMENT2_PART2);
        addKadenzeListener(kadenzeMenuItems[5], KadenzeAssignmentType.ASSIGNMENT2_PART3A);
        addKadenzeListener(kadenzeMenuItems[6], KadenzeAssignmentType.ASSIGNMENT2_PART3B);

        kadenzeMenuItems[7] = new JMenuItem();
        addKadenzeSummaryItem(menuKadenze, kadenzeMenuItems[7]);

        kadenzeMenuItems[8] = new JMenuItem("Create Kadenze Assignment 2 submission");
        kadenzeMenuItems[8].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAssignmentSubmission();
            }
        });
        menuKadenze.add(kadenzeMenuItems[8]);
    }

    private void addKadenzeListener(final JMenuItem kadenzeMenuItem, final KadenzeAssignmentType kadenzeAssignmentType) {
        kadenzeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchToAssignment(kadenzeAssignmentType);
            }
        });
    }

    private void switchToAssignment(KadenzeAssignmentType t) {
        try {
            KadenzeLogging.startLoggingForAssignment(t);
        } catch (IOException ex) {
            //Can we give a better error?
            Util.showPrettyErrorPane(this, "Error encountered in creating log file! Please ensure that your Kadenze log location is writable, and that no other copies of Wekinator are also trying to log there.");
        }
    }

    //Called when part changed or assignment stopped
    //TODO:
    private void updateKadenzeMenus() {
        KadenzeAssignmentType ka = KadenzeLogging.getCurrentAssignmentType();
        int which = KadenzeAssignment.getAssignmentNumber(ka);
        if (which == 1) {
            if (KadenzeLogging.isCurrentlyLogging()) {
                kadenzeMenuItems[1].setEnabled(true);
            } else {
                kadenzeMenuItems[1].setEnabled(false);
            }
        } else if (which == 2) {
            int subpart = KadenzeAssignment.getAssignmentSubPart(ka);
            for (int i = 0; i < 7; i++) {
                if (subpart == (i + 1)) {
                    kadenzeMenuItems[i].setEnabled(false);
                    kadenzeMenuItems[i].setText("Doing " + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(2, i + 1)));
                } else {
                    kadenzeMenuItems[i].setEnabled(true);
                    kadenzeMenuItems[i].setText("Start " + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(2, i + 1)));
                }
            }
        } else if (which == 3) {
            int subpart = KadenzeAssignment.getAssignmentSubPart(ka);
            for (int i = 0; i < 6; i++) {
                if (subpart == (i + 1)) {
                    kadenzeMenuItems[i].setEnabled(false);
                    kadenzeMenuItems[i].setText("Doing " + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(3, i + 1)));

                } else {
                    kadenzeMenuItems[i].setEnabled(true);
                    kadenzeMenuItems[i].setText("Start " + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(3, i + 1)));
                }
            }
        } else if (which == 4) {
            int subpart = KadenzeAssignment.getAssignmentSubPart(ka);
            for (int i = 0; i < 4; i++) {
                if (subpart == (i + 1)) {
                    kadenzeMenuItems[i].setEnabled(false);
                    kadenzeMenuItems[i].setText("Doing " + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(4, i + 1)));

                } else {
                    kadenzeMenuItems[i].setEnabled(true);
                    kadenzeMenuItems[i].setText("Start " + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(4, i + 1)));
                }
            }
        } else if (which == 6) {
            int subpart = KadenzeAssignment.getAssignmentSubPart(ka);
            for (int i = 0; i < 2; i++) {
                if (subpart == (i + 1)) {
                    kadenzeMenuItems[i].setEnabled(false);
                    kadenzeMenuItems[i].setText("Doing " + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(6, i + 1)));

                } else {
                    kadenzeMenuItems[i].setEnabled(true);
                    kadenzeMenuItems[i].setText("Start " + KadenzeAssignment.getReadableName(KadenzeAssignment.getAssignment(6, i + 1)));
                }
            }
        } else if (which == 7) {
            // System.out.println("NOT IMPLEMENTED YET");
        } else {
            System.out.println("ERROR: no assignment for " + which);
        }
    }

    private void createAssignmentSubmission() {
        try {
            //If running, should stop first! Otherwise, danger of not knowing 
            // how long running happened before student submits logs (run start 
            // will be last logged line)
            if (w.getLearningManager().getLearningType() == LearningType.SUPERVISED_LEARNING) {
                if (w.getSupervisedLearningManager().getRunningState() == RunningState.RUNNING) {
                    KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.RUN_STOP);
                    w.getSupervisedLearningManager().setRunningState(RunningState.NOT_RUNNING);
                    w.getStatusUpdateCenter().update(this, "Running stopped");
                }
            } else if (w.getLearningManager().getLearningType() == LearningType.TEMPORAL_MODELING) {
                if (w.getDtwLearningManager().getRunningState() == DtwLearningManager.RunningState.RUNNING) {
                    KadenzeLogging.getLogger().logEvent(w, KadenzeLogger.KEvent.RUN_STOP); //TODO: DTW need to test this!
                    w.getDtwLearningManager().stopRunning();
                    w.getStatusUpdateCenter().update(this, "Running stopped");
                }
            }

            //Show prompt for information about input device & difficulty
            KadenzeAssignmentType ka = KadenzeLogging.getCurrentAssignmentType();
            int which = KadenzeAssignment.getAssignmentNumber(ka);
            if (which == 1) {
                //Show prompt
                final MainGUI mg = this;
                KadenzeAssn1SubmissionPrompt kipf = new KadenzeAssn1SubmissionPrompt(w, new KadenzeAssn1SubmissionPrompt.KadenzeAssn1InputInfoReceiver() {
                    @Override
                    public void infoLogged() {
                        String zipped;
                        try {
                            zipped = KadenzeLogging.createZipForAssignment();
                            Util.showPrettyInfoPane(mg, "Your assignment is done! Please submit file " + zipped, "Success!");
                        } catch (IOException ex) {
                            String dir = KadenzeLogging.getLogger().getZipDirectoryNameForAssignment();
                            Util.showPrettyErrorPane(mg, "Could not zip file. Please zip your " + dir + " directory manually.");
                        }
                    }
                });
                kipf.setVisible(true);
            } else if (which == 2) {
                //Show prompt
                final MainGUI mg = this;
                KadenzeAssn2SubmissionPrompt kipf = new KadenzeAssn2SubmissionPrompt(w, new KadenzeAssn2SubmissionPrompt.KadenzeAssn2InputInfoReceiver() {
                    @Override
                    public void infoLogged() {
                        String zipped;
                        try {
                            zipped = KadenzeLogging.createZipForAssignment();
                            Util.showPrettyInfoPane(mg, "Your assignment is done! Please submit file " + zipped, "Success!");
                        } catch (IOException ex) {
                            String dir = KadenzeLogging.getLogger().getZipDirectoryNameForAssignment();
                            Util.showPrettyErrorPane(mg, "Could not zip file. Please zip your " + dir + " directory manually.");
                        }
                    }
                });
                kipf.setVisible(true);
            } else if (which == 3) {
                //Show prompt
                final MainGUI mg = this;
                KadenzeAssn3SubmissionPrompt kipf = new KadenzeAssn3SubmissionPrompt(w, new KadenzeAssn3SubmissionPrompt.KadenzeAssn3InputInfoReceiver() {
                    @Override
                    public void infoLogged() {
                        String zipped;
                        try {
                            zipped = KadenzeLogging.createZipForAssignment();
                            Util.showPrettyInfoPane(mg, "Your assignment is done! Please submit file " + zipped, "Success!");
                        } catch (IOException ex) {
                            String dir = KadenzeLogging.getLogger().getZipDirectoryNameForAssignment();
                            Util.showPrettyErrorPane(mg, "Could not zip file. Please zip your " + dir + " directory manually.");
                        }
                    }
                });
                kipf.setVisible(true);
            } else if (which == 4) {
                final MainGUI mg = this;
                KadenzeAssn4SubmissionPrompt1.KadenzeAssn4Part1InputInfoReceiver ki1 = new KadenzeAssn4SubmissionPrompt1.KadenzeAssn4Part1InputInfoReceiver() {
                    @Override
                    public void infoLogged() {
                        KadenzeAssn4SubmissionPrompt2.KadenzeAssn4Part2InputInfoReceiver ki2 = new KadenzeAssn4SubmissionPrompt2.KadenzeAssn4Part2InputInfoReceiver() {
                            @Override
                            public void infoLogged() {
                                String zipped;
                                try {
                                    zipped = KadenzeLogging.createZipForAssignment();
                                    Util.showPrettyInfoPane(mg, "Your assignment is done! Please submit file " + zipped, "Success!");
                                } catch (IOException ex) {
                                    String dir = KadenzeLogging.getLogger().getZipDirectoryNameForAssignment();
                                    Util.showPrettyErrorPane(mg, "Could not zip file. Please zip your " + dir + " directory manually.");
                                }

                            }
                        };
                        KadenzeAssn4SubmissionPrompt2 prompt2 = new KadenzeAssn4SubmissionPrompt2(w, ki2);
                        prompt2.setVisible(true);
                    }
                };
                KadenzeAssn4SubmissionPrompt1 prompt = new KadenzeAssn4SubmissionPrompt1(w, ki1);
                prompt.setVisible(true);

            } else if (which == 6) {
                final MainGUI mg = this;
                KadenzeAssn6SubmissionPrompt.KadenzeAssn6Part2InputInfoReceiver ki2 = new KadenzeAssn6SubmissionPrompt.KadenzeAssn6Part2InputInfoReceiver() {
                    @Override
                    public void infoLogged() {
                        String zipped;
                        try {
                            zipped = KadenzeLogging.createZipForAssignment();
                            Util.showPrettyInfoPane(mg, "Your assignment is done! Please submit file " + zipped, "Success!");
                        } catch (IOException ex) {
                            String dir = KadenzeLogging.getLogger().getZipDirectoryNameForAssignment();
                            Util.showPrettyErrorPane(mg, "Could not zip file. Please zip your " + dir + " directory manually.");
                        }
                    }
                };

                KadenzeAssn6SubmissionPrompt prompt = new KadenzeAssn6SubmissionPrompt(w, ki2);
                prompt.setVisible(true);

            } else if (which == 7) {
                final MainGUI mg = this;
                KadenzeAssn7SubmissionPrompt.KadenzeAssn7InputInfoReceiver ki2 = new KadenzeAssn7SubmissionPrompt.KadenzeAssn7InputInfoReceiver() {
                    @Override
                    public void infoLogged() {
                        String zipped;
                        try {
                            zipped = KadenzeLogging.createZipForAssignment();
                            Util.showPrettyInfoPane(mg, "Your assignment is done! Please submit file " + zipped, "Success!");
                        } catch (IOException ex) {
                            String dir = KadenzeLogging.getLogger().getZipDirectoryNameForAssignment();
                            Util.showPrettyErrorPane(mg, "Could not zip file. Please zip your " + dir + " directory manually.");
                        }
                    }
                };

                KadenzeAssn7SubmissionPrompt prompt = new KadenzeAssn7SubmissionPrompt(w, ki2);
                prompt.setVisible(true);

            } else {
                String zipped = KadenzeLogging.createZipForAssignment();
                Util.showPrettyInfoPane(this, "Your assignment is done! Please submit file " + zipped, "Success!");
            }
        } catch (Exception ex) {
            //String dir = KadenzeLogging.getLogger().getCurrentLoggingDirectory();
            String dir = KadenzeLogging.getLogger().getZipDirectoryNameForAssignment();
            Util.showPrettyErrorPane(this, "Could not zip file. Please zip your " + dir + " directory manually.");
        }
    }

    private void wekinatorPropertyChanged(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == Wekinator.PROP_PROJECT_NAME) {
            this.setTitle(w.getProjectName());
        } else if (evt.getPropertyName() == Wekinator.PROP_HAS_SAVE_LOCATION) {
            menuItemSave.setEnabled(w.hasSaveLocation());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        learningPanel1 = new wekimini.gui.SupervisedLearningPanel();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        panelParent = new javax.swing.JPanel();
        supervisedLearningPanel1 = new wekimini.gui.SupervisedLearningPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        menuItemSave = new javax.swing.JMenuItem();
        menuItemSaveAs = new javax.swing.JMenuItem();
        menuItemSaveModels = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuLoadFromARFF = new javax.swing.JMenuItem();
        menuItemSaveArff = new javax.swing.JMenuItem();
        menuItemExportCpp = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        menuItemEvaluation = new javax.swing.JMenuItem();
        menuPerformanceCheck = new javax.swing.JCheckBoxMenuItem();
        menuConsole = new javax.swing.JMenuItem();
        menuActions = new javax.swing.JMenu();
        checkEnableOSCControl = new javax.swing.JCheckBoxMenuItem();
        menuKadenze = new javax.swing.JMenu();

        jMenu1.setText("Temp");

        jMenuItem3.setText("flush");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("New project");
        setMaximumSize(new java.awt.Dimension(851, 2147483647));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        panelParent.setLayout(new javax.swing.BoxLayout(panelParent, javax.swing.BoxLayout.LINE_AXIS));

        supervisedLearningPanel1.setMinimumSize(new java.awt.Dimension(840, 313));
        supervisedLearningPanel1.setPreferredSize(new java.awt.Dimension(840, 313));
        panelParent.add(supervisedLearningPanel1);

        menuFile.setMnemonic('F');
        menuFile.setText("File");

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.META_MASK));
        jMenuItem6.setText("New project");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem6);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.META_MASK));
        jMenuItem4.setText("Open project...");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        menuFile.add(jMenuItem4);

        menuItemSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.META_MASK));
        menuItemSave.setText("Save");
        menuItemSave.setToolTipText("");
        menuItemSave.setEnabled(false);
        menuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveActionPerformed(evt);
            }
        });
        menuFile.add(menuItemSave);

        menuItemSaveAs.setText("Save project as...");
        menuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(menuItemSaveAs);

        menuItemSaveModels.setText("Save models as...");
        menuFile.add(menuItemSaveModels);
        menuFile.add(jSeparator1);

        menuLoadFromARFF.setText("Import training data from ARFF...");
        menuLoadFromARFF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLoadFromARFFActionPerformed(evt);
            }
        });
        menuFile.add(menuLoadFromARFF);

        menuItemSaveArff.setText("Save data as ARFF...");
        menuItemSaveArff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveArffActionPerformed(evt);
            }
        });
        menuFile.add(menuItemSaveArff);

        menuItemExportCpp.setText("Export models as C++...");
        menuItemExportCpp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExportCppActionPerformed(evt);
            }
        });
        menuFile.add(menuItemExportCpp);

        jMenuBar1.add(menuFile);

        jMenu2.setText("View");

        jMenuItem5.setText("OSC receiver status");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuItem1.setText("Inputs");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem2.setText("Outputs");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem7.setText("Input/output connection editor");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem7);

        menuItemEvaluation.setText("Model evaluation");
        menuItemEvaluation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemEvaluationActionPerformed(evt);
            }
        });
        jMenu2.add(menuItemEvaluation);

        menuPerformanceCheck.setText("Performance mode view");
        menuPerformanceCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPerformanceCheckActionPerformed(evt);
            }
        });
        jMenu2.add(menuPerformanceCheck);

        menuConsole.setText("Console");
        menuConsole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuConsoleActionPerformed(evt);
            }
        });
        jMenu2.add(menuConsole);

        jMenuBar1.add(jMenu2);

        menuActions.setText("Actions");

        checkEnableOSCControl.setSelected(true);
        checkEnableOSCControl.setText("Enable OSC control of GUI");
        checkEnableOSCControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkEnableOSCControlActionPerformed(evt);
            }
        });
        menuActions.add(checkEnableOSCControl);

        jMenuBar1.add(menuActions);

        menuKadenze.setText("Kadenze");
        jMenuBar1.add(menuKadenze);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelParent, javax.swing.GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelParent, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveAsActionPerformed
        new NewProjectSettingsFrame(w).setVisible(true);
    }//GEN-LAST:event_menuItemSaveAsActionPerformed

    private void menuItemSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveActionPerformed
        w.save();
    }//GEN-LAST:event_menuItemSaveActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        //String homeDir = System.getProperty("user.home");
        String lastLocation = GlobalSettings.getInstance().getStringValue("wekinatorProjectLoadLocation", "");
        if (lastLocation.equals("")) {
            lastLocation = System.getProperty("user.home");
        }

        File f = Util.findLoadFile(WekinatorFileData.FILENAME_EXTENSION, "Wekinator file", lastLocation, this);
        if (f != null) {
            try {
                //TODO: Check this isn't same wekinator as mine! (don't load from my same place, or from something already open...)
                WekiMiniRunner.getInstance().runFromFile(f.getAbsolutePath(), true);
            } catch (Exception ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        showOSCReceiverWindow();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        showInputMonitorWindow();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        showOutputTable();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed

        WekiMiniRunner.getInstance().runNewProject();
        //TODO: this or main?

    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        showInputOutputConnectionWindow();
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosed

    public void setPerformanceMode(boolean performanceMode) {
        learningPanel1.setPerfomanceMode(performanceMode);
        menuPerformanceCheck.setSelected(performanceMode);
        if (performanceMode) {
            this.setSize(225, 225);
            setMaximumSize(new Dimension(255, 255));
        } else {
            this.setSize(getPreferredSize());
            this.setMaximumSize(new Dimension(817, 2147483647));
        }
    }

    private void menuPerformanceCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuPerformanceCheckActionPerformed
        setPerformanceMode(menuPerformanceCheck.isSelected());
        //pack();
        //repaint();
    }//GEN-LAST:event_menuPerformanceCheckActionPerformed

    private void checkEnableOSCControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkEnableOSCControlActionPerformed
        w.getWekinatorController().setOscControlEnabled(checkEnableOSCControl.isSelected());
    }//GEN-LAST:event_checkEnableOSCControlActionPerformed

    private void menuConsoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuConsoleActionPerformed
        w.showConsole();
    }//GEN-LAST:event_menuConsoleActionPerformed

    private void menuItemEvaluationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemEvaluationActionPerformed
        showEvaluationWindow();
    }//GEN-LAST:event_menuItemEvaluationActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        flushLogs();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void menuLoadFromARFFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLoadFromARFFActionPerformed
        showArffLoader();
    }//GEN-LAST:event_menuLoadFromARFFActionPerformed

    private void menuItemSaveArffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveArffActionPerformed
        //TODO unshow this menu if we're doing dtw; same for loading from arff
        DataManager dataManager = w.getDataManager();
        if (dataManager != null) {

            File file = Util.findSaveFile("arff",
                    "data",
                    "arff file",
                    this);
            if (file != null) {
                try {
                    dataManager.writeInstancesToArff(file);
                    /* if (WekinatorRunner.isLogging()) {
                     Plog.log(Msg.DATA_VIEWER_SAVE_ARFF_BUTTON, file.getAbsolutePath() + "/" + file.getName());
                     } */
                    // Util.setLastFile(SimpleDataset.getFileExtension(), file);
                } catch (Exception ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.WARNING, null, ex);
                    Util.showPrettyErrorPane(this, "Could not save to file: " + ex.getMessage());
                }
            }
        } 
    }//GEN-LAST:event_menuItemSaveArffActionPerformed

    private void menuItemExportCppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExportCppActionPerformed
        // TODO: Check that we have only neural nets or kNN
        
        String lastLocation = GlobalSettings.getInstance().getStringValue("CppSaveLocation", "");
        if (lastLocation.equals("")) {
            lastLocation = System.getProperty("user.home");
        }
        
        File f = Util.findSaveDirectory("Select directory to save c++ files", lastLocation, this);

        if (f != null) {
            try {
                WekinatorSaver.saveCppSource(f.getAbsolutePath() + File.separator, w);
                GlobalSettings.getInstance().setStringValue("CppSaveLocation", f.getAbsolutePath());
            } catch (IOException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                Util.showPrettyErrorPane(this, "Could not save to C++ files: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_menuItemExportCppActionPerformed

    private void flushLogs() {
        KadenzeLogging.getLogger().flush();
    }

    private void showEvaluationWindow() {
        if (modelEvaluationFrame == null) {
            modelEvaluationFrame = new ModelEvaluationFrame(w.getOutputManager().getOutputGroup().getOutputNames(), w);
            modelEvaluationFrame.setVisible(true);

            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    System.out.println("Setting to null");
                    modelEvaluationFrame = null;
                }
            };
            Util.callOnClosed(modelEvaluationFrame, callMe);
        } else {
            modelEvaluationFrame.toFront();
        }
    }

    public void showOutputTable() {
        if (w.getLearningManager().getLearningType() == LearningManager.LearningType.TEMPORAL_MODELING) {
            if (dtwOutputEditor == null) {
                dtwOutputEditor = new DtwOutputEditor(w);
                dtwOutputEditor.setVisible(true);

                /* Util.callOnClosed(outputTableWindow, (Callable) () -> {
                 outputTableWindow = null;
                 return null;
                 }); */
                Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                    @Override
                    public void callMe() {
                        dtwOutputEditor = null;
                    }
                };
                Util.callOnClosed(dtwOutputEditor, callMe);

            } else {
                dtwOutputEditor.toFront();
            }
        } else {
            if (outputTableWindow == null) {
                outputTableWindow = new OutputViewerTable(w);
                outputTableWindow.setVisible(true);

                /* Util.callOnClosed(outputTableWindow, (Callable) () -> {
                 outputTableWindow = null;
                 return null;
                 }); */
                Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                    @Override
                    public void callMe() {
                        outputTableWindow = null;
                    }
                };
                Util.callOnClosed(outputTableWindow, callMe);

            } else {
                outputTableWindow.toFront();
            }
        }
    }

    public void showOSCReceiverWindow() {
        if (oscInputStatusFrame == null) {
            oscInputStatusFrame = new OSCInputStatusFrame(w);
            oscInputStatusFrame.setVisible(true);

            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    oscInputStatusFrame = null;
                }
            };
            Util.callOnClosed(oscInputStatusFrame, callMe);
        } else {
            oscInputStatusFrame.toFront();
        }
    }

    private void showInputMonitorWindow() {
        if (inputMonitorFrame == null) {
            inputMonitorFrame = new InputMonitor(w);
            inputMonitorFrame.setVisible(true);

            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    inputMonitorFrame = null;
                }
            };
            Util.callOnClosed(inputMonitorFrame, callMe);
        } else {
            inputMonitorFrame.toFront();
        }
    }

    private void showInputOutputConnectionWindow() {
        if (inputOutputConnectionsWindow == null) {
            inputOutputConnectionsWindow = new InputOutputConnectionsEditor(w);
            inputOutputConnectionsWindow.setVisible(true);

            //Problem: Won't call on button-triggered dispose...
            Util.CallableOnClosed callMe = new Util.CallableOnClosed() {
                @Override
                public void callMe() {
                    inputOutputConnectionsWindow = null;
                }
            };

            Util.callOnClosed(inputOutputConnectionsWindow, callMe);
        } else {
            inputOutputConnectionsWindow.toFront();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /*try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
         if ("Nimbus".equals(info.getName())) {
         javax.swing.UIManager.setLookAndFeel(info.getClassName());
         break;
         }
         }
         } catch (ClassNotFoundException ex) {
         java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
         java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
         java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
         java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         */

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

                try {
                    Wekinator w = Wekinator.TestingWekinator();
                    new MainGUI(w, LearningManager.LearningType.SUPERVISED_LEARNING).setVisible(true);
                } catch (IOException ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem checkEnableOSCControl;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private wekimini.gui.SupervisedLearningPanel learningPanel1;
    private javax.swing.JMenu menuActions;
    private javax.swing.JMenuItem menuConsole;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuItemEvaluation;
    private javax.swing.JMenuItem menuItemExportCpp;
    private javax.swing.JMenuItem menuItemSave;
    private javax.swing.JMenuItem menuItemSaveArff;
    private javax.swing.JMenuItem menuItemSaveAs;
    private javax.swing.JMenuItem menuItemSaveModels;
    private javax.swing.JMenu menuKadenze;
    private javax.swing.JMenuItem menuLoadFromARFF;
    private javax.swing.JCheckBoxMenuItem menuPerformanceCheck;
    private javax.swing.JPanel panelParent;
    private wekimini.gui.SupervisedLearningPanel supervisedLearningPanel1;
    // End of variables declaration//GEN-END:variables

    void displayEditOutput(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void showExamplesViewer() {
        //String s = w.getDataManager().toString();
        //System.out.println(s);
        //if (w.getLearningManager().getLearningType() == LearningType.SUPERVISED_LEARNING) {
        w.getDataManager().showViewer();
        //} else {
        //}
    }

    private void initializeForSupervisedLearning() {
        learningPanel1 = new SupervisedLearningPanel();
        Path[] paths = w.getSupervisedLearningManager().getPaths().toArray(new Path[0]);
        String[] modelNames = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            modelNames[i] = paths[i].getCurrentModelName();
        }
        learningPanel1.setup(w, paths, modelNames);
        panelParent.removeAll();
        panelParent.add(learningPanel1);
    }

    private void initializeForTemporalModeling() {
        panelParent.removeAll();
        dtwLearningPanel1 = new DtwLearningPanel(w);
        panelParent.add(dtwLearningPanel1);
        menuItemEvaluation.setEnabled(false);
        //dtwLearningPanel1.setup(w);
        revalidate();
        repaint();
    }

    public void showPathEditor(Path p) {
        PathEditorFrame f = PathEditorFrame.getEditorForPath(p, w.getInputManager().getInputNames(), w);
        f.setVisible(true);
        f.toFront();
    }

    @Override
    public void setCloseable(boolean b) {
        this.closeable = b;
    }

    @Override
    public Wekinator getWekinator() {
        return w;
    }

    public void showDtwData(int gestureNum) {
        w.getDtwLearningManager().getData().showViewer(gestureNum);
        /* System.out.println("XXXXXXXXXXXXXXX\n\n");

         w.getDtwLearningManager().getModel().dumpToConsole();
         w.getDtwLearningManager().getModel().getData().dumpExamplesForGesture(gestureNum); */
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void showDtwExamplesViewer() {
        w.getDtwLearningManager().getData().showViewer();

        /* System.out.println("XXXXXXXXXXXXXXX\n\n");
         w.getDtwLearningManager().getModel().dumpToConsole();
         w.getDtwLearningManager().getModel().getData().dumpAllExamples(); */
    }

    public void showDtwEditor(DtwModel model) {
        DtwEditorFrame f = DtwEditorFrame.getEditorForModel(model, w.getInputManager().getInputNames(), w);
        f.setVisible(true);
        f.toFront();
    }

    private void makeKadenzeAssignment7Menu(KadenzeAssignmentType ka) {
        kadenzeMenuItems = new JMenuItem[2];
        String s = "Doing Assignment 7";
        kadenzeMenuItems[0] = new JMenuItem(s);
        kadenzeMenuItems[0].setEnabled(false);
        menuKadenze.add(kadenzeMenuItems[0]);

        kadenzeMenuItems[1] = new JMenuItem("Create Kadenze Assignment 7 submission");
        kadenzeMenuItems[1].addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAssignmentSubmission();
            }
        });
        menuKadenze.add(kadenzeMenuItems[1]);
    }

    private void addKadenzeSummaryItem(JMenu menuKadenze, JMenuItem kadenzeMenuItem) {
        kadenzeMenuItem.setText("View assignment summary...");
        kadenzeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewAssignmentSummary();
            }
        });
        menuKadenze.add(kadenzeMenuItem);
    }

    private void viewAssignmentSummary() {
        KadenzeLogging.getLogger().flush();
        KadenzeAssignmentSummaryFrame kasf = new KadenzeAssignmentSummaryFrame(w);
        kasf.setVisible(true);
    }

    public void showArffLoader() {
        if (arffLoader == null) {
            
            File f = WekiArffLoader.getArffFile();
            if (f == null) {
                return;
            }
            
            arffLoader = new WekiArffLoader(w, new WekiArffLoader.ArffLoaderNotificationReceiver() {

                @Override
                public void completed() {
                    arffLoader = null;
                }
            });
            arffLoader.loadFile(f);
        }
    }
}
