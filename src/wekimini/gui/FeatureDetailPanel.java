/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.NoSuchElementException;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import javax.swing.Timer;
import weka.core.Instance;
import wekimini.Wekinator;
import wekimini.modifiers.Feature.InputDiagram;
import wekimini.modifiers.FeatureMultipleModifierOutput;
import wekimini.modifiers.FeatureSingleModifierOutput;
import wekimini.modifiers.ModifiedInput;

/**
 *
 * @author louismccallum
 */
public class FeatureDetailPanel extends javax.swing.JPanel {

    private PlotPanel plotPanel;
    private final int REFRESH_RATE = 20;
    private PlotRowModel model = new PlotRowModel(100);
    private Wekinator w;
    private static final int PLOT_W = 416;
    private final static int PLOT_H = 75;
    private int featureOutputIndex = 0;
    private Timer timer;
    
    public FeatureDetailPanel() 
    {
        initComponents();
        plotPanel = new PlotPanel(PLOT_W, PLOT_H, 100);        
        plotScrollPane.setViewportView(plotPanel);
        plotScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        outputComboBox.setVisible(false);
        outputLabel.setVisible(false);
        plotScrollPane.addComponentListener(new ResizeListener());
        
    }
    
    class ResizeListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            plotPanel.updateWidth(plotScrollPane.getWidth());
            redrawPlot();
        }       
    }
    
    protected void showNoFeature()
    {
        timer.stop();
        titleLabel.setText("Select a Feature to Explore");
        outputComboBox.setVisible(false);
        outputLabel.setVisible(false);
        model.points.clear();
        plotPanel.updateModel(model);
        diagramView.setVisible(false);
    }
    
    public void update(Wekinator w)
    {
        this.w = w;
        
        timer = new Timer(REFRESH_RATE, (ActionEvent evt) -> {
            Instance in = w.getSupervisedLearningManager().getCurrentInputInstance();
            if(in != null && model != null)
            {
                double val = w.getSupervisedLearningManager().getCurrentValueforFeature(model.feature, featureOutputIndex);
                model.addPoint(val);
                model.windowSize = w.getDataManager().featureManager.getFeatureWindowSize();
                plotPanel.renderWindow = true;
                plotPanel.updateModel(model);
            }    
        });  
        timer.start();
    }
    
    public URL urlForDiagram(InputDiagram diagram) throws NoSuchElementException
    {
        switch(diagram)
        {
            case ACCX : return getClass().getResource("/wekimini/icons/AccX.png");
            case ACCY : return getClass().getResource("/wekimini/icons/AccY.png");
            case ACCZ : return getClass().getResource("/wekimini/icons/AccZ.png");
            case GYROX : return getClass().getResource("/wekimini/icons/GyroX.png");
            case GYROY : return getClass().getResource("/wekimini/icons/GyroY.png");
            case GYROZ : return getClass().getResource("/wekimini/icons/GyroZ.png");
            case MULTIPLE : return getClass().getResource("/wekimini/icons/Multi.png");
            case UNKNOWN : return getClass().getResource("/wekimini/icons/Unknown.png");
        }
        throw new NoSuchElementException();
    }
    
    public void redrawPlot()
    {
        plotPanel.updateModel(model);
        plotPanel.reset();
        plotPanel.updateWidth(model.isStreaming);
        repaint();
        plotScrollPane.revalidate();
        validate();
        plotScrollPane.setViewportView(plotPanel);
        plotScrollPane.revalidate();
        validate();
    }
    public void setModel(PlotRowModel model)
    {
        this.model = model;
        featureOutputIndex = 0;
        titleLabel.setText(model.feature.name);

        redrawPlot();
        
        try
        {
            diagramView.setVisible(true);
            diagramView.loadImage(urlForDiagram(model.feature.diagram));
        } 
        catch (NoSuchElementException e)
        {
            System.out.println(e.getLocalizedMessage());
        }
        
        descriptionLabel.setText(model.feature.description);
              
        updateComboBox();
        
        if(!timer.isRunning())
        {
            timer.start();
        }
    }
    
    private void addItemsForModifier(int iD, String name)
    {
        ModifiedInput outputModifier = w.getDataManager().featureManager.getAllFeaturesGroup().getModifiers().getModifierForID(iD);
        for(int i = 0; i < outputModifier.getSize(); i++)
        {
            outputComboBox.addItem(name + ":" + i);
        }
    }
    
    private void updateComboBox()
    {
        outputComboBox.removeAllItems();
        if(model.feature instanceof FeatureSingleModifierOutput)
        {
            FeatureSingleModifierOutput ft = (FeatureSingleModifierOutput)model.feature;
            addItemsForModifier(ft.getOutputModifierID(), ft.name);
        }
        else
        {
            FeatureMultipleModifierOutput ft = (FeatureMultipleModifierOutput)model.feature;
            for(int i = 0; i < ft.getOutputModifierIDs().length; i++)
            {
                addItemsForModifier(ft.getOutputModifierIDs()[i], ft.name + ":" + i);
            }
        }
        boolean multiOutput = outputComboBox.getItemCount() > 1;
        outputComboBox.setVisible(multiOutput);
        outputLabel.setVisible(multiOutput);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea3 = new javax.swing.JTextArea();
        plotScrollPane = new javax.swing.JScrollPane();
        titleLabel = new javax.swing.JLabel();
        diagramView = new wekimini.gui.ImagePanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        descriptionLabel = new javax.swing.JTextArea();
        outputComboBox = new javax.swing.JComboBox<>();
        outputLabel = new javax.swing.JLabel();

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane2.setViewportView(jTextArea2);

        jTextArea3.setColumns(20);
        jTextArea3.setRows(5);
        jScrollPane3.setViewportView(jTextArea3);

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED, null, new java.awt.Color(204, 204, 204), null, new java.awt.Color(204, 204, 204)));

        plotScrollPane.setBorder(null);
        plotScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        plotScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        titleLabel.setBackground(new java.awt.Color(238, 0, 0));
        titleLabel.setFont(new java.awt.Font("Lucida Grande", 1, 15)); // NOI18N
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setText("Select a Feature To Explore");

        diagramView.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout diagramViewLayout = new javax.swing.GroupLayout(diagramView);
        diagramView.setLayout(diagramViewLayout);
        diagramViewLayout.setHorizontalGroup(
            diagramViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 94, Short.MAX_VALUE)
        );
        diagramViewLayout.setVerticalGroup(
            diagramViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jScrollPane4.setBorder(null);
        jScrollPane4.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        descriptionLabel.setEditable(false);
        descriptionLabel.setColumns(20);
        descriptionLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        descriptionLabel.setLineWrap(true);
        descriptionLabel.setRows(5);
        descriptionLabel.setWrapStyleWord(true);
        descriptionLabel.setBorder(null);
        jScrollPane4.setViewportView(descriptionLabel);

        outputComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        outputComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputComboBoxActionPerformed(evt);
            }
        });

        outputLabel.setText("Select Output:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(outputLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(outputComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(diagramView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(plotScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                    .addComponent(outputComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(outputLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(diagramView, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
                    .addComponent(plotScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void outputComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputComboBoxActionPerformed
        //WARNING!! THIS DOESNT NECESSARILY WORK FOR FEATURES WITH MULTIPLE MODIFERS OUTPUTTING
        featureOutputIndex = outputComboBox.getSelectedIndex();
        
    }//GEN-LAST:event_outputComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descriptionLabel;
    private wekimini.gui.ImagePanel diagramView;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextArea jTextArea3;
    private javax.swing.JComboBox<String> outputComboBox;
    private javax.swing.JLabel outputLabel;
    private javax.swing.JScrollPane plotScrollPane;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
}
