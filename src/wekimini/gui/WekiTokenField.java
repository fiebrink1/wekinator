/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author louismccallum
 */
public class WekiTokenField extends JScrollPane 
{
    private ArrayList<String> tokens = new ArrayList();
    private JPanel panel = new JPanel();
    private FlowLayout layout = new FlowLayout();
    private JButton[] btns;
    private WekiTokenFieldDelegate delegate;
    
    public WekiTokenField()
    {
        panel.setLayout(layout);
        panel.removeAll();
        getViewport().setBorder(null);
        setViewportBorder(null);
        setBorder(null);
    }
    
    public void setDelegate(WekiTokenFieldDelegate delegate)
    {
        this.delegate = delegate;
    }
    
    public void setTokens(ArrayList<String> tokens)
    {
        this.tokens = tokens;
        redraw();
    }
    
    public void addToken(String token)
    {
        tokens.add(token);
        redraw();
    }
    
    public void removeToken(String token)
    {
        tokens.remove(token);
        redraw();
    }
            
    public void redraw()
    {
        panel.removeAll();
        revalidate();
        layout.setHgap(2);
        btns = new JButton[tokens.size()];
        int i = 0;
        getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
        for(String token:tokens)
        {
            btns[i] = new JButton(token);
            btns[i].addActionListener(new ActionListener() { 
                public void actionPerformed(ActionEvent e) { 
                  delegate.onTokenPressed(token);
                } 
            });
            panel.add(btns[i]);
            i++;
        }
        panel.validate();
        
        int w = 0;
        for(JButton btn:btns)
        {
            w += btn.getPreferredSize().width;
        }
        panel.setPreferredSize(new Dimension(w + 20, getPreferredSize().height));
        revalidate();
        setViewportView(panel);
        revalidate();
        getViewport().setBorder(null);
        setViewportBorder(null);
        setBorder(null);
    }
}
