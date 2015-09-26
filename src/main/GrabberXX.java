package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import res.Constants;
import res.ResourceHelper;

public class GrabberXX {

    public static void main(String... args) {
        if (args.length > 0) {
            try {
                //loop: for (Command cmd : cmds) {
                switch (args[0]) {
                    case "-compile": // compiling option
                    case "-c":
                        compile(args);
                        break;
                    case "-display": // display option
                    case "-d":
                        display(args);
                        break;
                    case "-listurls":
                    case "-lu":
                        listurls();
                        break;
                }
            } catch (Exception ex) {
                ResourceHelper.errLog("Grabber > [command line] > Error : %s", ex);
            }
        } else {
            showHelp();
        }
    }

    public static void listurls(String... args) {
        Compiler compiler = compile(args);
        ArrayList<GrabbedImageData> gids = compiler.getGrabbedImageData();
        ArrayList<GrabbedImagePosition> gips = compiler.getGrabbedImagePositions();
        TreeMap<Integer, Boolean> toggled = new TreeMap<>();
        gids.forEach((GrabbedImageData t) -> {
            toggled.put(t.getSerialnumber(), Boolean.FALSE);
        });
        File file = new File(compiler.getCompileOutputCurrentDir(),
                ResourceHelper.getGoogleProperty(Constants.KEY_SELECTED_IMAGES_URLS_OUTPUT_FILE));
        addUrlsToFile(file, gids, toggled, args);
    }

    public static void display(String... args) {
        Compiler compiler = compile(args);
        ArrayList<GrabbedImageData> gids = compiler.getGrabbedImageData();
        ArrayList<GrabbedImagePosition> gips = compiler.getGrabbedImagePositions();
        TreeMap<Integer, Boolean> toggled = new TreeMap<>();
        gips.forEach((GrabbedImagePosition t) -> {
            toggled.put(t.getSerialnumber(), Boolean.FALSE);
        });
        BufferedImage image = ResourceHelper.getImageResource(compiler.getGoogleScreenShot());
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(700, 500);
        f.setLayout(new BorderLayout());
        GrabbedImageDisplay gid = new GrabbedImageDisplay(image, gids, gips, toggled);
        gid.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        gid.setMaximumSize(new Dimension(700, Integer.MAX_VALUE));
        JScrollPane pane = new JScrollPane(gid);
        javax.swing.JScrollBar sb = pane.getVerticalScrollBar();
        sb.setBlockIncrement(100);
        sb.setUnitIncrement(100);
        pane.updateUI();
        f.add(pane, BorderLayout.CENTER);
        f.add(new JButton(new AbstractAction("done") {
            @Override
            public void actionPerformed(ActionEvent e) {
                f.dispose();
                File file = new File(compiler.getCompileOutputCurrentDir(),
                        ResourceHelper.getGoogleProperty(Constants.KEY_SELECTED_IMAGES_URLS_OUTPUT_FILE));
                addUrlsToFile(file, gids, toggled, args);
            }
        }), BorderLayout.SOUTH);
        f.setVisible(true);
    }

    public static void addUrlsToFile(File output, ArrayList<GrabbedImageData> gids,
            TreeMap<Integer, Boolean> toggled, String... args) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(output))) {
            boolean listurls = false;
            for (String arg : args) {
                switch (arg) {
                    case "-listurls":
                    case "-lu":
                        listurls = true;
                        break;
                }
            }
            ArrayList<String> selectedUrls = new ArrayList<>();
            for (Entry<Integer, Boolean> entry : toggled.entrySet()) {
                if (entry.getValue()) {
                    gids.stream().anyMatch((GrabbedImageData t) -> {
                        if (entry.getKey().equals(t.getSerialnumber())) {
                            selectedUrls.add(t.getUrl());
                            return true;
                        }
                        return false;
                    });
                }
            }
            for (String selectedUrl : selectedUrls) {
                pw.println(selectedUrl);
                if (listurls) {
                    System.out.println(selectedUrl);
                }
            }
        } catch (FileNotFoundException ex) {
            ResourceHelper.errLog("Grabber > addUrlsToFile(...) > Error : %s", ex);
        }
    }

    public static Compiler compile(String... args) {
        int k = 1;
        String gss = null, gxml = null, od = Constants.PATH_DEFAULT_COMPILE_OUTPUT_DIRECTORY;
        boolean addimages = false;
        while (k < args.length) {
            switch (args[k]) {
                case "-googlescreenshot":
                case "-gss":
                    gss = args[++k];
                    break;
                case "-googlexml":
                case "-gxml":
                    gxml = args[++k];
                    break;
                case "-outputdir":
                case "-od":
                    od = args[++k];
                    break;
                case "-addimages":
                case "-ai":
                    addimages = true;
                    break;
            }
            k++;
        }
        if (gss == null || gxml == null) {
            showHelp();
            System.exit(1);
        }
        File gssFile = new File(gss);
        File gxmlFile = new File(gxml);
        File odFile = new File(od);
        Compiler compiler = new Compiler(gssFile, gxmlFile, odFile);
        compiler.setCreateImages(addimages);
        compiler.compile();
        return compiler;
    }

    public static void showHelp() {
        try (Scanner sc = new Scanner(new FileInputStream(
                ResourceHelper.getURLResource(Constants.RESOURCE_GRABBER_HELP_FILE).getFile()))) {
            while (sc.hasNextLine()) {
                System.out.println(sc.nextLine());
            }
        } catch (Exception ex) {
            ResourceHelper.errLog("Grabber > showHelp() > Error : %s", ex);
        }
    }
}
