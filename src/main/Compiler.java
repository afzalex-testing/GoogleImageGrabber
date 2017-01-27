package main;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import res.Constants;
import res.ResourceHelper;

public class Compiler {

    public boolean debug = false;
    private File googleScreenShot;
    private File googleXML;
    private File compileOutputLocation;
    private File compileOutputCurrentDir;
    private boolean outputDirectoryCreated = false;
    private boolean createImages = false;
    private boolean compiled = false;
    private boolean xmlcompiled = false;
    private boolean screenshotcompiled = false;
    private ArrayList<GrabbedImagePosition> grabbedImagePositions = new ArrayList<>();
    private ArrayList<GrabbedImageData> grabbedImageData = new ArrayList<>();

    private static final DateFormat LAST_OUTPUT_DIRECTORY_NAME_POST_FORMAT
            = new SimpleDateFormat("ddMMMYY_hhmmss_SSS");

    public Compiler() {
        compileOutputLocation = new File(Constants.PATH_DEFAULT_COMPILE_OUTPUT_DIRECTORY);
    }

    public Compiler(File googleScreenShot, File googleXML) {
        this();
        this.googleScreenShot = googleScreenShot;
        this.googleXML = googleXML;
    }

    public Compiler(File googleScreenShot, File googleXML, File compileOutput) {
        this(googleScreenShot, googleXML);
        this.compileOutputLocation = compileOutput;
    }

    private void createLastOutputDirectory() {
        if (!outputDirectoryCreated) {
            compileOutputCurrentDir = new File(compileOutputLocation,
                    ResourceHelper.getGoogleProperty(Constants.KEY_COMPILED_OUTPUT_DIR_NAME_PRE)
                    + LAST_OUTPUT_DIRECTORY_NAME_POST_FORMAT.format(new Date()));
            outputDirectoryCreated = true;
        }
        getCompileOutputCurrentDir().mkdirs();
        if(!getCompileOutputCurrentDir().exists()){
            ResourceHelper.errLog("Compiler > createLastOutputDirectory() > Unable to create directory structure");
        }
    }

    private void createOutputDirStructure() {
        if (!compileOutputLocation.exists()) {
            compileOutputLocation.mkdirs();
        }
        createLastOutputDirectory();
    }

    public void compile() {
        if (!compiled) {
            createOutputDirStructure();
            compileScreenShot();
            compileXML();
        }
    }

    public void compileScreenShot() {
        createOutputDirStructure();
        if (!screenshotcompiled) {
            try {
                int travDiviser = 100;
                BufferedImage shot = ResourceHelper.getImageResource(googleScreenShot);
                int shotWidth = shot.getWidth();
                int shotHeight = shot.getHeight();
                int travXUnit = Math.max(shotWidth / travDiviser, 1);
                int travYUnit = Math.max(shotHeight / travDiviser, 1);
                Color bg = new Color(
                        Integer.parseInt(ResourceHelper.getGoogleProperty(Constants.KEY_GOOGLE_BACKGROUND_COLOR_RED)),
                        Integer.parseInt(ResourceHelper.getGoogleProperty(Constants.KEY_GOOGLE_BACKGROUND_COLOR_GREEN)),
                        Integer.parseInt(ResourceHelper.getGoogleProperty(Constants.KEY_GOOGLE_BACKGROUND_COLOR_BLUE)));
                Color col;
                ArrayList<Integer> topList = new ArrayList<>();
                ArrayList<Integer> btmList = new ArrayList<>();
                boolean lastLineMatched = true;
                for1:
                for (int y = 0; y < shotHeight; y++) {
                    for2:
                    for (int x = 0; x < shotWidth; x += travXUnit) {
                        col = new Color(shot.getRGB(x, y));
                        if (!col.equals(bg)) {
                            if (lastLineMatched) {
                                topList.add(y);
                                lastLineMatched = false;
                            }
                            continue for1;
                        }
                    }
                    if (!lastLineMatched) {
                        btmList.add(y - 1);
                        lastLineMatched = true;
                    }
                }

                Rectangle rect = null;
                boolean newRectInserted = false;
                ArrayList<GrabbedImagePosition> grabbedImagePoss = new ArrayList<>();
                int colnum, sno = 0;
                loop1:
                for (int i = 0; i < topList.size(); i++) {
                    lastLineMatched = true;
                    int maxTop = topList.get(i);
                    int maxBtm = btmList.get(i);
                    travYUnit = Math.max((maxBtm - maxTop) / travDiviser, 1);
                    colnum = 0;
                    loop2:
                    for (int x = 0; x < shotWidth; x++) {
                        loop3:
                        for (int y = maxTop; y <= maxBtm; y += travYUnit) {
                            col = new Color(shot.getRGB(x, y));
                            if (!col.equals(bg)) {
                                if (lastLineMatched) {
                                    newRectInserted = false;
                                    rect = new Rectangle(x, maxTop, 0, maxBtm - maxTop + 1);
                                    lastLineMatched = false;
                                }
                                continue loop2;
                            }
                        }
                        if (!lastLineMatched) {
                            rect.width = x - rect.x;
                            GrabbedImagePosition gip = new GrabbedImagePosition(sno++);
                            gip.setBounds(rect);
                            gip.setRownumber(i);
                            gip.setColnumber(colnum++);
                            grabbedImagePoss.add(gip);
                            lastLineMatched = true;
                            newRectInserted = true;
                        }
                    }
                    if (!newRectInserted) {
                        rect.width = shotWidth - rect.x;
                        GrabbedImagePosition gip = new GrabbedImagePosition(sno++);
                        gip.setBounds(rect);
                        gip.setRownumber(i);
                        gip.setColnumber(colnum++);
                        grabbedImagePoss.add(gip);
                        newRectInserted = true;
                    }
                }

                int maxY, maxX;
                loop1:
                for (GrabbedImagePosition gip : grabbedImagePoss) {
                    rect = gip.getBounds();
                    travXUnit = Math.max(rect.width / travDiviser, 1);
                    lastLineMatched = true;
                    maxY = rect.y + rect.height;
                    maxX = rect.x + rect.width;
                    loop2:
                    for (int y = rect.y; y < maxY; y++) {
                        loop3:
                        for (int x = rect.x; x < maxX; x += travXUnit) {
                            col = new Color(shot.getRGB(x, y));
                            if (!col.equals(bg)) {
                                if (lastLineMatched) {
                                    rect.y = y;
                                    lastLineMatched = false;
                                }
                                continue loop2;
                            }
                        }
                        if (!lastLineMatched) {
                            rect.height = y - rect.y;
                            lastLineMatched = true;
                        }
                    }
                }
                setGrabbedImagePositions(grabbedImagePoss);
                screenshotcompiled = true;
            } catch (Exception ex) {
                ResourceHelper.errLog("Compiler > compileScreenShot() > Error %s", ex);
            }
        }
    }

    public void createImages() {
        compileScreenShot();
        File dir = new File(getCompileOutputCurrentDir(),
                ResourceHelper.getGoogleProperty(Constants.KEY_COMPILED_OUTPUT_IMAGE_DIR_NAME));
        if(dir.mkdirs()){
        BufferedImage shot = ResourceHelper.getImageResource(googleScreenShot);
        Image img;
        Rectangle rect;
        String pre = ResourceHelper.getGoogleProperty(Constants.KEY_COMPILED_OUTPUT_IMAGE_NAME_PRE);
        File file;
        for (GrabbedImagePosition gip : getGrabbedImagePositions()) {
            try {
                rect = gip.getBounds();
                img = shot.getSubimage(rect.x, rect.y, rect.width, rect.height);
                file = new File(dir, String.format("%s_%s_%s.png",
                        pre, gip.getSerialnumber(), gip.getRownumber()));
                ImageIO.write((RenderedImage) img, "png", file);
            } catch (IOException ex) {
                ResourceHelper.errLog("Compiler > createImages() > Error : %s", ex);
            }
        }}else {
            ResourceHelper.errLog("Compiler > createImages() > Unable to create image directory");
        }
    }

    public void createClones() {
        compile();
        File dir = new File(getCompileOutputCurrentDir(),
                ResourceHelper.getGoogleProperty(Constants.KEY_COMPILED_OUTPUT_CLONE_DIR_NAME));
        if (dir.mkdirs()) {
            BufferedImage shot = ResourceHelper.getImageResource(googleScreenShot);
            Image img;
            Rectangle rect;
            File file;
            String fname;
            for (GrabbedImagePosition gip : getGrabbedImagePositions()) {
                GrabbedImageData gid = null;
                for (GrabbedImageData g : grabbedImageData) {
                    if (g.getSerialnumber() == gip.getSerialnumber()) {
                        gid = g;
                    }
                }
                try {
                    rect = gip.getBounds();
                    img = shot.getSubimage(rect.x, rect.y, rect.width, rect.height);
                    fname = null;
                    file = null;
                    if (gid != null) {
                        fname = gid.getFileName();
                        if (fname.matches(".+\\..{3,}")) {
                            file = new File(dir, fname);
                        }
                    }
                    try {
                        if (file == null || !file.createNewFile()) {
                            file = new File(dir, String.format("Clone%d info invalid or not found.png",
                                    gip.getSerialnumber()));
                        }
                    } catch (IOException exx) {
                        file = new File(dir, String.format("Clone%d invalid name found.png",
                                gip.getSerialnumber()));
                    }
                    ImageIO.write((RenderedImage) img, "png", file);
                } catch (Exception ex) {
                    ResourceHelper.errLog("Compiler > createClones() > Error : %s", ex);
                }
            }
        }else {
            ResourceHelper.errLog("Compiler > createClone() > Unable to create clone");
        }
    }

    /**
     * @return the createImages
     */
    public boolean isCreateImages() {
        return createImages;
    }

    /**
     * @param createImages the createImages to set
     */
    public void setCreateImages(boolean createImages) {
        this.createImages = createImages;
    }

    /**
     * @return the grabbedImagePositions
     */
    public ArrayList<GrabbedImagePosition> getGrabbedImagePositions() {
        return grabbedImagePositions;
    }

    /**
     * @param grabbedImagePositions the grabbedImagePositions to set
     */
    public void setGrabbedImagePositions(ArrayList<GrabbedImagePosition> grabbedImagePositions) {
        this.grabbedImagePositions = grabbedImagePositions;
    }

    /**
     * @return the grabbedImageData
     */
    public ArrayList<GrabbedImageData> getGrabbedImageData() {
        return grabbedImageData;
    }

    /**
     * @param grabbedImageData the grabbedImageData to set
     */
    public void setGrabbedImageData(ArrayList<GrabbedImageData> grabbedImageData) {
        this.grabbedImageData = grabbedImageData;
    }

    /**
     * @return the compileOutputCurrentDir
     */
    public File getCompileOutputCurrentDir() {
        return compileOutputCurrentDir;
    }

    /**
     * @return the xmlcompiled
     */
    public boolean isXmlcompiled() {
        return xmlcompiled;
    }

    /**
     * @return the screenshotcompiled
     */
    public boolean isScreenshotcompiled() {
        return screenshotcompiled;
    }

    public void compileXML() {
        createOutputDirStructure();
        if (!xmlcompiled) {
            Pattern pass1 = Pattern.compile("\\<div.*?\\>");
            Pattern pass2 = Pattern.compile("^\\<.*(?=data\\-ri).*\\>$");
            Pattern pass3 = Pattern.compile("\\<a.*?\\>");
            Pattern hrefPat = Pattern.compile("(?<=href\\=\\\").*?(?=\\\".*\\>)");
            Pattern dataRowPat = Pattern.compile("(?<=data\\-row\\=\\\").*?(?=\\\")");
            Pattern dataRiPat = Pattern.compile("(?<=data\\-ri\\=\\\").*?(?=\\\")");
            Pattern imgurlPat = Pattern.compile("(?<=\\?imgurl\\=).*?(?=\\&amp\\;)");
            Pattern imgwPat = Pattern.compile("(?<=\\&amp\\;w=).*?(?=\\&amp\\;)");
            Pattern imghPat = Pattern.compile("(?<=\\&amp\\;h=).*?(?=\\&amp\\;)");
            Pattern tagMeta = Pattern.compile("(?<=\\<div\\s{1,10}class\\=\\\"rg_meta\\\"\\>"
                    + "\\s{0,10})\\{.*\\}(?=\\</div\\>)");
            Pattern metaName = Pattern.compile("(?<=((\\{|\\,)\\\"fn\\\"\\s?:\\s?\\\")).*?(?=\\\")");
            Pattern metaPara = Pattern.compile("(?<=((\\{|\\,)\\\"pt\\\"\\s?:\\s?\\\")).*?(?=\\\")");
            Pattern metaSide = Pattern.compile("(?<=((\\{|\\,)\\\"s\\\"\\s?:\\s?\\\")).*?(?=\\\")");

            Matcher mat1, mat2, mat3;
            String url;
            try (Scanner sc = new Scanner(new FileInputStream(googleXML))) {
                while (sc.hasNextLine()) {
                    ArrayList<Match> lineMatches = new ArrayList<>();
                    String mainline = sc.nextLine();
                    mat1 = pass1.matcher(mainline);
                    while (mat1.find()) {
                        String divline = mat1.group();
                        mat2 = pass2.matcher(divline);
                        if (mat2.find()) {
                            lineMatches.add(new Match(divline, mat1.start(), mat1.end()));
                        }
                    }
                    String tagALineEx, tagALine;
                    int size = lineMatches.size();
                    for (int i = 0; i < size; i++) {
                        Match match = lineMatches.get(i);
                        tagALineEx = mainline.substring(match.divend,
                                i + 1 == size ? mainline.length() : lineMatches.get(i + 1).divstart);
                        mat3 = pass3.matcher(tagALineEx);
                        if (mat3.find()) {
                            tagALine = mat3.group();
                            mat1 = hrefPat.matcher(tagALine);
                            if (mat1.find()) {
                                match.tagahref = mat1.group();
                            }
                        }
                        mat2 = tagMeta.matcher(tagALineEx);
                        if (mat2.find()) {
                            match.tagmeta = mat2.group();
                        }
                    }

                    GrabbedImageData gid;
                    for (Match match : lineMatches) {
                        gid = new GrabbedImageData();
                        mat1 = dataRowPat.matcher(match.tagdiv);
                        if (mat1.find()) {
                            gid.setRownumber(Integer.parseInt(mat1.group()));
                        }
                        mat1 = dataRiPat.matcher(match.tagdiv);
                        if (mat1.find()) {
                            gid.setSerialnumber(Integer.parseInt(mat1.group()));
                        }
                        mat1 = imgurlPat.matcher(match.tagahref);
                        if (mat1.find()) {
                            url = mat1.group();
                            try{
                                gid.setUrl(java.net.URLDecoder.decode(url, "UTF-8"));
                            } catch (UnsupportedEncodingException ex){
                                gid.setUrl(url);
                                ResourceHelper.errLog("Compiler > compileXML() > URL: %s > Error : %s", url, ex);
                            }
                        }
                        mat1 = imgwPat.matcher(match.tagahref);
                        if (mat1.find()) {
                            gid.setWidth(Integer.parseInt(mat1.group()));
                        }
                        mat1 = imghPat.matcher(match.tagahref);
                        if (mat1.find()) {
                            gid.setHeight(Integer.parseInt(mat1.group()));
                        }
                        mat1 = metaName.matcher(match.tagmeta);
                        if (mat1.find()) {
                            gid.setFileName(mat1.group());
                        }
                        mat1 = metaPara.matcher(match.tagmeta);
                        if (mat1.find()) {
                            gid.setHeading(mat1.group());
                        }
                        mat1 = metaSide.matcher(match.tagmeta);
                        if (mat1.find()) {
                            gid.setSideData(mat1.group());
                        }
                        grabbedImageData.add(gid);
                    }
                }
                xmlcompiled = true;
            } catch (FileNotFoundException ex) {
                ResourceHelper.errLog("Compiler > compileXML() > Error : %s", ex);
            }
        }
    }

    public void corrector() {
        compile();
        int max = Math.min(grabbedImageData.size(), grabbedImagePositions.size());
        GrabbedImageData gid = null;
        GrabbedImagePosition gip = null;
        for (int i = 0; i < max; i++) {
            for (GrabbedImageData g : grabbedImageData) {
                if (g != null && g.getSerialnumber() == i) {
                    gid = g;
                    break;
                }
            }
            for (GrabbedImagePosition g : grabbedImagePositions) {
                if (g != null && g.getSerialnumber() == i) {
                    gip = g;
                }
            }
            if (gid == null || gip == null) {
                System.out.printf("Error : i = %d, gid = %s, gip = %s", i, gid, gip);
            } else if (gid.getRownumber() != gip.getRownumber()) {
                grabbedImagePositions.remove(gip);
                resetGipSerial();
            }
        }
    }

    private void resetGidSerial() {
        GrabbedImageData gid;
        for (int i = 0; i < grabbedImageData.size(); i++) {
            gid = grabbedImageData.get(i);
            if (gid != null) {
                gid.setSerialnumber(i);
            }
        }
    }

    public void resetGipSerial() {
        GrabbedImagePosition gip;
        for (int i = 0; i < grabbedImagePositions.size(); i++) {
            gip = grabbedImagePositions.get(i);
            if (gip != null) {
                gip.setSerialnumber(i);
            }
        }
    }

    private class Match {

        String tagdiv;
        String tagahref;
        String tagmeta;
        int divstart;
        int divend;

        Match() {
        }

        Match(String str, int start, int end) {
            this.tagdiv = str;
            this.divstart = start;
            this.divend = end;
        }
    }

    /**
     * @return the googleScreenShot
     */
    public File getGoogleScreenShot() {
        return googleScreenShot;
    }

    /**
     * @param googleScreenShot the googleScreenShot to set
     */
    public void setGoogleScreenShot(File googleScreenShot) {
        this.googleScreenShot = googleScreenShot;
    }

    /**
     * @return the googleXML
     */
    public File getGoogleXML() {
        return googleXML;
    }

    /**
     * @param googleXML the googleXML to set
     */
    public void setGoogleXML(File googleXML) {
        this.googleXML = googleXML;
    }

    /**
     * @return the compileOutputLocation
     */
    public File getCompileOutputLocation() {
        return compileOutputLocation;
    }

    /**
     * @param compileOutputLocation the compileOutputLocation to set
     */
    public void setCompileOutputLocation(File compileOutputLocation) {
        this.compileOutputLocation = compileOutputLocation;
    }
}
