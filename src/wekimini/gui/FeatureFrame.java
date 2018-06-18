/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import weka.core.Instance;
import wekimini.SupervisedLearningManager;
import wekimini.SupervisedLearningManager.RunningState;
import wekimini.Wekinator;
import wekimini.WekinatorSupervisedLearningController;
import wekimini.kadenze.FeaturnatorLogger;
import wekimini.kadenze.KadenzeLogging;
import wekimini.modifiers.Feature;

/**
 *
 * @author louismccallum
 */
public class FeatureFrame extends JFrame implements FeatureEditorDelegate {
    
    private Wekinator w;
    private int selectedRow = -1;
    private int outputIndex = 0;
    private Feature selectedFeature;
    private PlotTableCellRenderer plotCellRenderer;
    private PlottedFeatureTableModel currentFeaturesTableModel;
    private Timer timer;
    private Feature[] currentFeatures;
    private boolean ignoreSliderUpdate = true;

    private static final int BUTTON_CELL_WIDTH = 30;
    private static final int TITLE_CELL_WIDTH = 135;
    private static final int PLOT_CELL_WIDTH_MIN = 50;
    private static final int ROW_HEIGHT = 35;
    private static final int PLOT_ROW_REFRESH_RATE = 60;
    private static final int PLOT_ROW_POINTS_PER_ROW = 10;

    public FeatureFrame() {
        initComponents();
    }
    
    public FeatureFrame(Wekinator w) {
        initComponents();
        this.w = w;
        timer = new Timer(1,new ActionListener() {public void actionPerformed(ActionEvent evt) {}});
        newFeaturesPanel.update(w, 0);
        featureDetailPanel.update(w);
        evaluateFeaturesPanel.update(w, 0);
        newFeaturesPanel.delegate = this;
        selectedFeature = w.getDataManager().featureManager.getAllFeaturesGroup().getFeatureForKey("AccX");
        currentFeaturesTable.setDefaultRenderer(Feature.class, new FeatureTableRenderer());
        currentFeaturesTable.setRowSelectionAllowed(false);
        currentFeaturesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        currentFeaturesTable.setRowHeight(ROW_HEIGHT);
        currentFeaturesTable.setRowMargin(3);
        currentFeaturesTable.setTableHeader(null);
        updateCurrentFeaturesTable();
                
        MouseListener featuresMouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = currentFeaturesTable.rowAtPoint(e.getPoint());
                int column = currentFeaturesTable.columnAtPoint(e.getPoint());
                Feature ft = (Feature)currentFeaturesTable.getModel().getValueAt(row, outputIndex);
                switch(column)
                {
                    case 0: updateSelectedFeature(ft); selectRow(row); break;
                    case 1: updateSelectedFeature(ft); selectRow(row); break;
                    case 2: removeFeature(ft); deselectRows(true); break;
                }
            }
        };
        currentFeaturesTable.addMouseListener(featuresMouseListener);
        currentFeaturesTable.addComponentListener(new ResizeListener());
        getContentPane().setBackground(Color.WHITE);
//        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//        setAlwaysOnTop(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                w.getSupervisedLearningManager().isPlotting = false;
                evaluateFeaturesPanel.onClose();
            }
        });

        boolean showSliders = true;
        windowLabel.setVisible(showSliders);
        windowSlider.setVisible(showSliders);
        
        ignoreSliderUpdate = true;
        
        windowSlider.setValue(w.getDataManager().featureManager.getFeatureWindowSize());
        windowLabel.setText("Window:" + windowSlider.getValue());
        
        ignoreSliderUpdate = false;
    }
    
    class ResizeListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            updateCurrentFeaturesTable();
        }       
    }
    
    private void removeFeature(Feature ft)
    {
        if(w.getSupervisedLearningManager().getRunningState() == RunningState.NOT_RUNNING)
        {
            if(KadenzeLogging.getLogger() instanceof FeaturnatorLogger)
            {
                ((FeaturnatorLogger)KadenzeLogging.getLogger()).logFeatureRemoved(w);
            }
            w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).removeFeatureForKey(ft.name);
            w.getDataManager().featureListUpdated();
            newFeaturesPanel.featureListUpdated();
            evaluateFeaturesPanel.featuresListUpdated();
            updateCurrentFeaturesTable();
        }
        else
        {
            Object[] options = {"Stop Running","OK"};
            int n = JOptionPane.showOptionDialog(null,
                "Cannot edit features whilst Running",
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     
                options,  
                options[0]); 
            if(n ==0)
            {
                new WekinatorSupervisedLearningController(w.getSupervisedLearningManager(),w).stopRun();
            }
        }
                   
    }
    
    public void startTimer()
    {
        if(timer.isRunning())
        {
            timer.stop();
        }
        
        timer = new Timer(PLOT_ROW_REFRESH_RATE, (ActionEvent evt) -> {
            Instance in = w.getSupervisedLearningManager().getCurrentInputInstance();
            if(in != null)
            {
                for(int i = 0; i < currentFeatures.length; i++)
                {
                    Feature ft  = currentFeatures[i];
                    double val = w.getSupervisedLearningManager().getCurrentValueforFeature(ft, 0);
                    currentFeaturesTableModel.getModel(i).addPoint(val);
                }
            }
            currentFeaturesTable.repaint();    
        });  
        
        timer.start();
    }
    
    public void updateCurrentFeaturesTable()
    {
        int tblWidth = currentFeaturesTable.getWidth();
        int titleCellWidth = TITLE_CELL_WIDTH;
        int plotCellWidth = tblWidth- titleCellWidth - BUTTON_CELL_WIDTH;
        if(plotCellWidth < PLOT_CELL_WIDTH_MIN)
        {
            plotCellWidth = PLOT_CELL_WIDTH_MIN;
            titleCellWidth = tblWidth - plotCellWidth - BUTTON_CELL_WIDTH;
        }
        
        int pointsPerRow = (int)(PLOT_ROW_POINTS_PER_ROW * ((double)plotCellWidth / 50.0f));
        
        currentFeatures = w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).getCurrentFeatures();
        currentFeaturesTableModel = new PlottedFeatureTableModel(currentFeatures, pointsPerRow);
        currentFeaturesTable.setModel(currentFeaturesTableModel);
        
        plotCellRenderer = new PlotTableCellRenderer(plotCellWidth, ROW_HEIGHT);
        plotCellRenderer.reset();
        
        TableColumn column;
        TableColumnModel jTableColumnModel = currentFeaturesTable.getColumnModel();
        int cantCols = jTableColumnModel.getColumnCount();
        for (int i = 0; i < cantCols; i++) {
            column = jTableColumnModel.getColumn(i);
            switch(i)
            {
                case 0:
                    int pWidth = Math.round(titleCellWidth - BUTTON_CELL_WIDTH);
                    column.setPreferredWidth(pWidth);
                    break;
                case 1:
                    column.setPreferredWidth(plotCellWidth);
                    column.setCellRenderer(plotCellRenderer);
                    break;
                case 2:
                    column.setPreferredWidth(BUTTON_CELL_WIDTH);
                    column.setCellRenderer(new ImageTableCellRenderer("delete.png"));
                    break;
            }
        }
        
        deselectRows(false);
        
        startTimer();
    }
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newFeaturesPanel = new wekimini.gui.NewFeaturesPanel();
        featureDetailPanel = new wekimini.gui.FeatureDetailPanel();
        jLabel1 = new javax.swing.JLabel();
        evaluateFeaturesPanel = new wekimini.gui.EvaluateFeaturesPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        currentFeaturesTable = new javax.swing.JTable();
        currentFeaturesLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        windowLabel = new javax.swing.JLabel();
        windowSlider = new javax.swing.JSlider();
        addRemoveToggle = new javax.swing.JCheckBox();
        applyAllButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));

        newFeaturesPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(204, 204, 204), null, new java.awt.Color(204, 204, 204)));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Select Features");

        currentFeaturesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        currentFeaturesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        currentFeaturesTable.setTableHeader(null);
        jScrollPane1.setViewportView(currentFeaturesTable);

        currentFeaturesLabel.setBackground(new java.awt.Color(255, 255, 255));
        currentFeaturesLabel.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        currentFeaturesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        currentFeaturesLabel.setText("Current Features");

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Evaluate");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(204, 204, 204), null, new java.awt.Color(204, 204, 204)));

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
        jLabel3.setText("<html> Parameters (these apply <br> to all features)</hml>");

        windowLabel.setText("Window Size:");

        windowSlider.setMinimum(5);
        windowSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                windowSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(windowLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(windowSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(windowLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(windowSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        addRemoveToggle.setSelected(true);
        addRemoveToggle.setText("Add");
        addRemoveToggle.setToolTipText("");
        addRemoveToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRemoveToggleActionPerformed(evt);
            }
        });

        applyAllButton.setText("Add All");
        applyAllButton.setToolTipText("");
        applyAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(featureDetailPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(addRemoveToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(applyAllButton))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(newFeaturesPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(currentFeaturesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(evaluateFeaturesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE)
                    .addComponent(currentFeaturesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 28, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(evaluateFeaturesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 425, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(40, 40, 40))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(newFeaturesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 423, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addRemoveToggle)
                            .addComponent(applyAllButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(featureDetailPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void windowSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_windowSliderStateChanged
        // TODO add your handling code here:
        if(ignoreSliderUpdate)
        {
            return;
        }
        
        if(w.getSupervisedLearningManager().getRunningState() != SupervisedLearningManager.RunningState.NOT_RUNNING)
        {
            Object[] options = {"Stop Running","OK"};
            int n = JOptionPane.showOptionDialog(null,
                "Cannot edit features whilst Running",
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,     
                options,  
                options[0]); 
            if(n ==0)
            {
                new WekinatorSupervisedLearningController(w.getSupervisedLearningManager(),w).stopRun();
            }
            return;
        }

        
        windowLabel.setText("Window Size:" + windowSlider.getValue());
        boolean isRunning = w.getSupervisedLearningManager().getRunningState() != SupervisedLearningManager.RunningState.NOT_RUNNING;
        boolean isPlotting = w.getSupervisedLearningManager().isPlotting;
        prepareForLibraryUpdate(isRunning, isPlotting);
        w.getDataManager().featureManager.setFeatureWindowSize(windowSlider.getValue(), 100);
        resetFollowingLibraryUpdate(isRunning, isPlotting, false);

    }//GEN-LAST:event_windowSliderStateChanged

    private void addRemoveToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRemoveToggleActionPerformed
        // TODO add your handling code here:
        newFeaturesPanel.searchCurrent = !addRemoveToggle.isSelected();
        applyAllButton.setText(addRemoveToggle.isSelected() ? "Add all":"Remove all");
        newFeaturesPanel.updateFilters();
        
    }//GEN-LAST:event_addRemoveToggleActionPerformed

    private void applyAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyAllButtonActionPerformed
        // TODO add your handling code here:
        for(Feature f:newFeaturesPanel.currentResults)
        {
            if(addRemoveToggle.isSelected())
            {
                w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).addFeatureForKey(f.name);
            }
            else
            {
                w.getDataManager().featureManager.getFeatureGroups().get(outputIndex).removeFeatureForKey(f.name);
            }
        }
        featureListUpdated();
    }//GEN-LAST:event_applyAllButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FeatureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FeatureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FeatureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FeatureFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new FeatureFrame().setVisible(true);
        });
    }
    
    private void updateSelectedFeature(Feature ft)
    {
        selectedFeature = w.getDataManager().featureManager.getAllFeaturesGroup().getFeatureForKey(ft.name);
        if(KadenzeLogging.getLogger() instanceof FeaturnatorLogger)
        {
            ((FeaturnatorLogger)KadenzeLogging.getLogger()).logFeaturePreviewed(w, selectedFeature);
        }
        PlotRowModel model = new PlotRowModel(100);
        w.getSupervisedLearningManager().isPlotting = true;
        model.isStreaming = true;
        model.feature = selectedFeature;
        featureDetailPanel.setModel(model);
    }
    
    //FeatureEditorDelegate methods
    
    @Override
    public void newFeatureSelected(Feature ft)
    {
        updateSelectedFeature(ft);
        deselectRows(false);
    }
   
    @Override
    public void featureListUpdated()
    {
        w.getDataManager().featureListUpdated();
        newFeaturesPanel.featureListUpdated();
        evaluateFeaturesPanel.featuresListUpdated();
        updateCurrentFeaturesTable();
        updateSelectedFeature(selectedFeature);
    }
    
    @Override
    public void featureLibraryUpdated(boolean sizeDidChange)
    {
        w.getDataManager().featureListUpdated();
        newFeaturesPanel.featureListUpdated();
        evaluateFeaturesPanel.featuresListUpdated();
        updateCurrentFeaturesTable();
        if(sizeDidChange)
        {
            deselectRows(true);
        }
    }
    
    
    public void selectRow(int row)
    {
        selectedRow = row;
        ((FeatureTableModel)currentFeaturesTable.getModel()).selectedRow = selectedRow;
        currentFeaturesTable.repaint();
        newFeaturesPanel.deselectRows();
    }
    
    public void deselectRows(boolean deselectPlot)
    {
        selectedRow = -1;
        ((FeatureTableModel)currentFeaturesTable.getModel()).selectedRow = selectedRow;
        currentFeaturesTable.repaint();
        newFeaturesPanel.deselectRows();
        if(deselectPlot)
        {
            featureDetailPanel.showNoFeature();
        }
    }

    private void prepareForLibraryUpdate(boolean isRunning, boolean isPlotting)
    {
        if(isRunning)
        {
            w.getSupervisedLearningManager().stopRunning();
        }
        if(isPlotting)
        {
            w.getSupervisedLearningManager().isPlotting = false;
        }
    }
    
    private void resetFollowingLibraryUpdate(boolean isRunning, boolean isPlotting, boolean buffers)
    {
        if(isRunning)
        {
            w.getSupervisedLearningManager().startRunning();
        }
        if(isPlotting)
        {
            w.getSupervisedLearningManager().isPlotting = isPlotting;
        }
        newFeaturesPanel.refreshResultsTable();
        w.getDataManager().featureListUpdated();
        featureLibraryUpdated(buffers);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addRemoveToggle;
    private javax.swing.JButton applyAllButton;
    private javax.swing.JLabel currentFeaturesLabel;
    private javax.swing.JTable currentFeaturesTable;
    private wekimini.gui.EvaluateFeaturesPanel evaluateFeaturesPanel;
    private wekimini.gui.FeatureDetailPanel featureDetailPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private wekimini.gui.NewFeaturesPanel newFeaturesPanel;
    private javax.swing.JLabel windowLabel;
    private javax.swing.JSlider windowSlider;
    // End of variables declaration//GEN-END:variables
}
