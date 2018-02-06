/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel{

    private BufferedImage image;
    private boolean hasImage = false;

    public ImagePanel() {

    }
    
    public void loadImage(URL url)
    {
        hasImage = false;
        try {                
          image = ImageIO.read(url);
          hasImage = true;
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(hasImage)
        {
            g.drawImage(image, 0, 0, this);
        }
    }

}