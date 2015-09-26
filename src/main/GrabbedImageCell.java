package main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JPanel;

public class GrabbedImageCell extends JPanel {

    GrabbedImageData grabbedImageData;
    GrabbedImagePosition grabbedImagePosition;
    BufferedImage image;
    Dimension size;

    public GrabbedImageCell(BufferedImage fullImage, GrabbedImageData gid, GrabbedImagePosition gip) {
        this.grabbedImageData = gid;
        this.grabbedImagePosition = gip;
        Rectangle rect = gip.getBounds();
        image = fullImage.getSubimage(rect.x, rect.y, rect.width, rect.height);
        size = new Dimension(image.getWidth(), image.getHeight());
        setPreferredSize(size);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
