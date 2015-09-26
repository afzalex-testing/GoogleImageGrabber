package res;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;

public class ResourceHelper {

    private static final String UNIQUE_RESPOOL_ADDER = (char) (0) + (char) (1) + (char) (2) + (char) (3) + "";
    private static final ResourceBundle projectResources = ResourceBundle.getBundle(Constants.PATH_PROJECT_PROPERTIES_FILE);
    private static final File DATA_DIRECTORY = new File(getProjectProperty(Constants.KEY_DATA_DIRECTORY));
    private static final File LOG_FILE = new File(DATA_DIRECTORY + "/" + getProjectProperty(Constants.KEY_LOG_FILE));
    private static final Properties googleResources = new Properties();
    private static final DateFormat LOG_DATE_FORMAT = new SimpleDateFormat("dd/MM/YY hh:mm:ss.SSS");
    private static final Hashtable<String, Object> RES_POOS = new Hashtable<>();

    static {
        try {
            if (!DATA_DIRECTORY.exists()) {
                DATA_DIRECTORY.mkdirs();
            }
            if (!LOG_FILE.exists()) {
                LOG_FILE.createNewFile();
            }
            File googleResourceFile = new File(DATA_DIRECTORY + "/" + getProjectProperty(Constants.KEY_GOOGLE_PORPERTIES_FILE));
            if (googleResourceFile.exists()) {
                try (FileInputStream fis = new FileInputStream(googleResourceFile)) {
                    googleResources.load(fis);
                }
            }
        } catch (IOException | NullPointerException ex) {
            errLog("ResourceHelper > [static_block:1] > Error : %s", ex);
            System.exit(1);
        }
    }

    public static String getProjectProperty(String key) {
        String val = null;
        try {
            val = projectResources.getString(key);
        } catch (NullPointerException | MissingResourceException ex) {
            errLog("ResourceHelper > getProjectResource(String \"%s\") > Error : %s", key, ex);
        }
        return val;
    }

    public static String getGoogleProperty(String key) {
        String val = null;
        try {
            val = googleResources.getProperty(key);
        } catch (NullPointerException | MissingResourceException ex) {
            errLog("ResourceHelper > getGoogleProperty(String \"%s\") > Error : %s", key, ex);
        }
        return val;
    }

    private static String getUniqueKey(URL url) {
        return getUniqueKey("URL", url);
    }

    private static String getUniqueKey(File file) {
        return getUniqueKey("FILE", file);
    }

    private static String getUniqueKey(String pre, Object obj) {
        return String.format("%s_%s_%s", pre, UNIQUE_RESPOOL_ADDER, obj);
    }

    public static URL getURLResource(String key) {
        URL url = null;
        try {
            url = ResourceHelper.class.getResource(key);
        } catch (NullPointerException ex) {
            errLog("ResourceHelper > getURLResource(String \"%s\") > Error : %s", key, ex);
        }
        return url;
    }

    public static BufferedImage getImageResource(URL imageUrl) {
        BufferedImage img = null;
        try {
            String key = getUniqueKey("IMAGE", imageUrl);
            if (RES_POOS.containsKey(key)) {
                img = (BufferedImage) RES_POOS.get(key);
            } else {
                img = ImageIO.read(imageUrl);
                RES_POOS.put(key, img);
            }
        } catch (IOException ex) {
            errLog("ResourceHelper > getImageResource(URL \"%s\") > Error : %s", imageUrl, ex);
        }
        return img;
    }

    public static BufferedImage getImageResource(String key) {
        BufferedImage img = null;
        try {
            URL url = getURLResource(key);
            key = getUniqueKey("IMAGE", url);
            if (RES_POOS.containsKey(key)) {
                img = (BufferedImage) RES_POOS.get(key);
            } else {
                img = getImageResource(url);
                RES_POOS.put(key, img);
            }
        } catch (Exception ex) {
            errLog("ResourceHelper > getImageResource(String \"%s\" > Error : %s", key, ex);
        }
        return img;
    }

    public static BufferedImage getImageResource(File file) {
        BufferedImage img = null;
        try {
            String key = getUniqueKey("IMAGE", file);
            if (RES_POOS.containsKey(key)) {
                img = (BufferedImage) RES_POOS.get(key);
            } else {
                img = ImageIO.read(file);
                RES_POOS.put(key, img);
            }
        } catch (Exception ex) {
            errLog("ResourceHelper > getImageResource(File \"%s\") > Error : %s", file, ex);
        }
        return img;
    }

    public static void setImageResource(File file, BufferedImage img) {
        try {
            String key = getUniqueKey("IMAGE", file);
            RES_POOS.put(key, img);
        } catch (Exception ex) {
            errLog("ResourceHelper > setImageResource(File \"%s\", BufferedImage) > Error : %s", file, ex);
        }
    }

    public static void errLog(String format, Object... args) {
        errLog(String.format(format, args));
    }

    public static void errLog(String str) {
        logIntoSystem(str);
        logIntoFile(str);
    }

    public static void logIntoSystem(String str) {
        System.err.println(str);
    }

    public static void logIntoFile(String str) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(LOG_FILE, true))) {
            pw.printf("%s > %s\n", LOG_DATE_FORMAT.format(new Date()), str);
        } catch (FileNotFoundException ex) {
            System.err.println("ResourceHelper > log(String) > Error : " + ex);
        }
    }
}
