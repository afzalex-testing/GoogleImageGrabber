package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.JPanel;

public class GrabbedImageDisplay extends JPanel implements MouseListener, MouseMotionListener {

    public boolean debug = false;
    ArrayList<GrabbedImagePosition> grabbedImagePositions;
    ArrayList<GrabbedImageData> grabbedImageData;
    TreeMap<Integer, Boolean> toggled;
    BufferedImage coverImage;
    private final int extra = 20;
    private int coverImagex;
    private int coverImagey;

    public GrabbedImageDisplay(BufferedImage coverImage, ArrayList<GrabbedImageData> gids,
            ArrayList<GrabbedImagePosition> gips) {
        this(coverImage, gids, gips, null);
        toggled = new TreeMap<>();
        for (GrabbedImagePosition gip : grabbedImagePositions) {
            toggled.put(gip.getSerialnumber(), Boolean.FALSE);
        }

    }

    public GrabbedImageDisplay(BufferedImage coverImage, ArrayList<GrabbedImageData> gids,
            ArrayList<GrabbedImagePosition> gips, TreeMap<Integer, Boolean> toggled) {
        this.grabbedImageData = gids;
        this.grabbedImagePositions = gips;
        this.coverImage = coverImage;
        this.toggled = toggled;
        setPreferredSize(new Dimension(coverImage.getWidth() + extra,
                coverImage.getHeight() + extra));
        coverImagex = extra / 2;
        coverImagey = extra / 2;
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private static final Color selectedColor = new Color(15, 70, 4);
    private static final Color selectedForDetColor = new Color(0, 37, 121);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(coverImage, coverImagex, coverImagey, null);
        Rectangle rect;
        g.setColor(selectedForDetColor);
        if (isSelectedForDetails) {
            rect = gipSelectedForDetails.getBounds();
            g.drawRect(rect.x + coverImagex, rect.y + coverImagey, rect.width, rect.height);
            g.drawRect(rect.x - 1 + coverImagex, rect.y - 1 + coverImagey, rect.width + 2, rect.height + 2);
            g.drawRect(rect.x - 2 + coverImagex, rect.y - 2 + coverImagey, rect.width + 4, rect.height + 4);
        }
        g.setColor(selectedColor);
        for (Entry<Integer, Boolean> entry : toggled.entrySet()) {
            if (entry.getValue()) {
                rect = grabbedImagePositions.get(entry.getKey()).getBounds();
                g.drawRect(rect.x + coverImagex, rect.y + coverImagey, rect.width, rect.height);
                g.drawRect(rect.x - 1 + coverImagex, rect.y - 1 + coverImagey, rect.width + 2, rect.height + 2);
            }
        }
    }
    boolean isSelectedForDetails = false;
    GrabbedImagePosition gipSelectedForDetails = null;
    GrabbedImageData gidSelectedForDetails = null;

    @Override
    public void mouseClicked(MouseEvent e) {
        isSelectedForDetails = false;
        grabbedImagePositions.stream().anyMatch((GrabbedImagePosition gip) -> {
            Rectangle rect1 = gip.getBounds();
            if (rect1.contains(e.getX() - coverImagex, e.getY() - coverImagey)) {
                for (GrabbedImageData gid : grabbedImageData) {
                    if (gid.getSerialnumber() == gip.getSerialnumber()) {
                        if (gid.getRownumber() != gip.getRownumber()) {
                            System.out.println("found error");
                        } else {
                            switch (e.getButton()) {
                                case MouseEvent.BUTTON1:
                                    toggled.put(gip.getSerialnumber(), !toggled.get(gip.getSerialnumber()));
                                    break;
                                case MouseEvent.BUTTON3:
                                    isSelectedForDetails = true;
                                    gipSelectedForDetails = gip;
                                    gidSelectedForDetails = gid;
                                    break;
                            }
                            return true;
                        }
                        break;
                    }
                }
            }
            return false;
        });
        repaint();
    }

    int last = -1;

    @Override
    public void mouseMoved(MouseEvent e) {
        if (isSelectedForDetails) {
            mouseOver(gidSelectedForDetails, gipSelectedForDetails);
        } else {
            grabbedImagePositions.stream().anyMatch((GrabbedImagePosition gip) -> {
                Rectangle rect1 = gip.getBounds();
                if (rect1.contains(e.getX() - coverImagex, e.getY() - coverImagey)) {
                    if (gip.getSerialnumber() != last) {
                        last = gip.getSerialnumber();
                        for (GrabbedImageData gid : grabbedImageData) {
                            if (gid.getSerialnumber() == gip.getSerialnumber()) {
                                mouseOver(gid, gip);
                                if (debug) {
                                    System.out.printf("gid.row : %2d    gip.row : %2d\n", gid.getRownumber(), gip.getRownumber());
                                }
                                repaint();
                                break;
                            }
                        }
                        return true;
                    }
                }
                return false;
            });
        }
    }

    public void mouseOver(GrabbedImageData gid, GrabbedImagePosition gip) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }
}
