/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author louismccallum
 */
    public class ImageTableCellRenderer extends JLabel implements TableCellRenderer
    {
        private BufferedImage image;
        
        private ImageTableCellRenderer(){}
        
        public ImageTableCellRenderer(String file)
        {
            setOpaque(true);
            loadImage(getClass().getResource("/wekimini/icons/"+ file));
        }
        
        private void loadImage(URL url)
        {
            try {                
              image = ImageIO.read(url);
            } catch (IOException ex) {
               System.out.println(ex.toString());
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, this);
        }
        
        @Override
        public JLabel getTableCellRendererComponent(
                        JTable table, Object value,
                        boolean isSelected, boolean hasFocus,
                        int row, int column) 
        {
            setBackground(Color.WHITE);
            return this;
        }
    }
