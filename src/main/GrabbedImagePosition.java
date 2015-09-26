package main;

import java.awt.Rectangle;

public class GrabbedImagePosition {

    private Rectangle bounds;
    private int rownumber;
    private int colnumber;
    private int serialnumber;

    public GrabbedImagePosition(){}
    public GrabbedImagePosition(int serialNumber){
        this.serialnumber = serialNumber;
    }
    
    public GrabbedImagePosition(Rectangle bounds) {
        this.bounds = bounds;
    }

    /**
     * @return the bounds
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * @param bounds the bounds to set
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    /**
     * @return the rownumber
     */
    public int getRownumber() {
        return rownumber;
    }

    /**
     * @param rownumber the rownumber to set
     */
    public void setRownumber(int rownumber) {
        this.rownumber = rownumber;
    }

    /**
     * @return the colnumber
     */
    public int getColnumber() {
        return colnumber;
    }

    /**
     * @param colnumber the colnumber to set
     */
    public void setColnumber(int colnumber) {
        this.colnumber = colnumber;
    }

    /**
     * @return the serialnumber
     */
    public int getSerialnumber() {
        return serialnumber;
    }

    /**
     * @param serialnumber the serialnumber to set
     */
    public void setSerialnumber(int serialnumber) {
        this.serialnumber = serialnumber;
    }
}
