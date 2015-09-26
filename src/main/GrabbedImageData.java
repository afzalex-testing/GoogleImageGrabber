package main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrabbedImageData {

    private int serialnumber;
    private int rownumber;
    private int width;
    private int height;
    private String url;
    private String fileName;
    private String heading;
    private String sideData;
    public boolean error = false;

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
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    private Matcher mat;
    private static final Pattern pat = Pattern.compile("^.*?(?=((\\s*$)|(\\s*\\?)))");
    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        mat = pat.matcher(fileName);
        if (mat.find()) {
            fileName = mat.group();
            if (fileName.length() == 0) {
                fileName = null;
            }
        } else {
            fileName = null;
        }
        this.fileName = fileName;
    }

    /**
     * @return the heading
     */
    public String getHeading() {
        return heading;
    }

    /**
     * @param heading the heading to set
     */
    public void setHeading(String heading) {
        this.heading = heading;
    }

    /**
     * @return the sideData
     */
    public String getSideData() {
        return sideData;
    }

    /**
     * @param sideData the sideData to set
     */
    public void setSideData(String sideData) {
        this.sideData = sideData;
    }
}
