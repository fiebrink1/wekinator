/*
 * Adapted from vertical label solution code at http://stackoverflow.com/questions/620929/rotate-a-swing-jlabel
 */
package wekimini.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import javax.swing.JLabel;
import static wekimini.gui.VerticalLabel.Direction.*;

/**
 *
 * @author rebecca
 */
public class VerticalLabel extends JLabel {

    private boolean needsRotate;

    public enum Direction {

        HORIZONTAL,
        VERTICAL_UP,
        VERTICAL_DOWN
    }

    private Direction direction = VERTICAL_UP;

    @Override
    public Dimension getSize() {
        if (!needsRotate) {
            return super.getSize();
        }

        Dimension size = super.getSize();
        Direction d = VERTICAL_DOWN;
        switch (getDirection()) {
            case VERTICAL_DOWN:
            case VERTICAL_UP:
                return new Dimension(size.height, size.width);
            default:
                return super.getSize();
        }
    }

    @Override
    public int getHeight() {
        return getSize().height;
    }

    @Override
    public int getWidth() {
        return getSize().width;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D gr = (Graphics2D) g.create();

        switch (getDirection()) {
            case VERTICAL_UP:
                gr.translate(0, getSize().getHeight());
                gr.transform(AffineTransform.getQuadrantRotateInstance(-1));
                break;
            case VERTICAL_DOWN:
                gr.transform(AffineTransform.getQuadrantRotateInstance(1));
                gr.translate(0, -getSize().getWidth());
                break;
            default:
        }

        needsRotate = true;
        super.paintComponent(gr);
        needsRotate = false;
    }

    private Direction getDirection() {
        return direction;
    }

    private void setDirection(Direction d) {
        direction = d;
    }
}
