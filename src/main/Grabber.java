package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import res.Constants;
import res.ResourceHelper;

public class Grabber {

    public static void main(String... args) {
        try {
            TreeSet<Command> set = Command.readCommands(args);
            if (set.isEmpty() || set.contains(Command.invalid)) {
                showHelp();
            } else {
                Compiler compiler = new Compiler();
                for (Command cmd : set) {
                    switch (cmd) {
                        case googlescreenshot:
                            compiler.setGoogleScreenShot(new File(cmd.value.toString()));
                            break;
                        case googlexml:
                            compiler.setGoogleXML(new File(cmd.value.toString()));
                            break;
                        case outputdir:
                            compiler.setCompileOutputLocation(new File(cmd.value.toString()));
                            break;
                        case debug:
                            compiler.debug = true;
                            break;
                    }
                }
                for (Command cmd : set) {
                    switch (cmd) {
                        case listallurls:
                            listUrls(compiler, set);
                            break;
                        case listallfilenames:
                            listFileNames(compiler, set);
                            break;
                        case listalldata:
                            listData(compiler, set);
                            break;
                        case display:
                            display(compiler, set);
                            break;
                        case printsize:
                            printsize(compiler, set);
                            break;
                        case addimages:
                            compiler.createImages();
                            break;
                        case addclones:
                            compiler.createClones();
                            break;
                        case corrector:
                            compiler.compile();
                            compiler.corrector();
                            break;
                    }
                }
                if (compiler.getCompileOutputCurrentDir() != null
                        && compiler.getCompileOutputCurrentDir().exists()) {
                    System.out.println("Output dir : " + compiler.getCompileOutputCurrentDir().getPath());
                }
            }
        } catch (Exception ex) {
            System.out.println("Error : " + ex);
            showHelp();
        }
    }

    public static void printsize(Compiler compiler, TreeSet<Command> set) {
        compiler.compile();
        System.out.println("\nNumber of images found : " + compiler.getGrabbedImagePositions().size());
        System.out.println("Number of URLs found : " + compiler.getGrabbedImageData().size());
    }

    public static void listUrls(Compiler compiler, TreeSet<Command> set) {
        compiler.compileXML();
        ArrayList<GrabbedImageData> gids = compiler.getGrabbedImageData();
        TreeMap<Integer, Boolean> urlMarked = new TreeMap<>();
        gids.forEach((GrabbedImageData t) -> {
            urlMarked.put(t.getSerialnumber(), Boolean.TRUE);
        });
        File file = new File(compiler.getCompileOutputCurrentDir(),
                ResourceHelper.getGoogleProperty(Constants.KEY_LISTALLURLS_OUTPUT_FILE));
        if (set.contains(Command.printmarkeddata)) {
            System.out.println("\n-- Printing listed urls --");
        }
        addUrlsToFile(file, gids, urlMarked, set);
    }

    public static void listData(Compiler compiler, TreeSet<Command> set) {
        compiler.compileXML();
        ArrayList<GrabbedImageData> gids = compiler.getGrabbedImageData();
        TreeMap<Integer, Boolean> urlMarked = new TreeMap<>();
        gids.forEach((GrabbedImageData t) -> {
            urlMarked.put(t.getSerialnumber(), Boolean.TRUE);
        });
        File file = new File(compiler.getCompileOutputCurrentDir(),
                ResourceHelper.getGoogleProperty(Constants.KEY_LISTALLDATA_OUTPUT_FILE));
        if (set.contains(Command.printmarkeddata)) {
            System.out.println("\n-- Printing listed data --");
        }
        addDataToFile(file, gids, urlMarked, set);
    }

    public static void listFileNames(Compiler compiler, TreeSet<Command> set) {
        compiler.compileXML();
        File file = new File(compiler.getCompileOutputCurrentDir(),
                ResourceHelper.getGoogleProperty(Constants.KEY_LISTALLFILENAMES_OUTPUT_FILE));
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(file))) {
            if (set.contains(Command.printmarkeddata)) {
                System.out.println("\n-- Printing listed filenames --");
            }
            boolean printop = set.contains(Command.printmarkeddata);
            for (GrabbedImageData gid : compiler.getGrabbedImageData()) {
                if (gid != null) {
                    pw.println(gid.getFileName());
                    if (printop) {
                        System.out.println(gid.getFileName());
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            ResourceHelper.errLog("Grabber > listFileNames(...) > Error : %s", ex);
        }
    }

    public static void display(Compiler compiler, TreeSet<Command> set) {
        compiler.compile();
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
        GrabbedImageDataDisp gidd = new GrabbedImageDataDisp();
        f.add(gidd, BorderLayout.NORTH);
        GrabbedImageDisplay gid = new GrabbedImageDisplay(image, gids, gips, toggled) {
            @Override
            public void mouseOver(GrabbedImageData gid, GrabbedImagePosition gip) {
                super.mouseOver(gid, gip);
                gidd.summary.setText(gid.getHeading());
                gidd.data.setText(gid.getSideData());
                gidd.fname.setText(gid.getFileName());
                gidd.url.setText(gid.getUrl());
                gidd.size.setText(gid.getWidth() + "x" + gid.getHeight());
                gidd.repaint();
            }
        };
        gid.debug = set.contains(Command.debug);
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
                addUrlsToFile(file, gids, toggled, set);
                if (set.contains(Command.showselectedimagedata)) {
                    file = new File(compiler.getCompileOutputCurrentDir(),
                            ResourceHelper.getGoogleProperty(Constants.KEY_SELECTED_IMAGES_DATA_OUTPUT_FILE));
                    addDataToFile(file, gids, toggled, set);
                }
            }
        }), BorderLayout.SOUTH);
        f.setVisible(true);
    }

    public static void addUrlsToFile(File output, ArrayList<GrabbedImageData> gids,
            TreeMap<Integer, Boolean> urlMarkedToAdd, TreeSet<Command> set) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(output))) {
            boolean printMarkedUrls = false;
            if (set.contains(Command.printmarkeddata)) {
                printMarkedUrls = true;
            }
            ArrayList<String> selectedUrls = new ArrayList<>();
            for (Map.Entry<Integer, Boolean> entry : urlMarkedToAdd.entrySet()) {
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
                if (printMarkedUrls) {
                    System.out.println(selectedUrl);
                }
            }
        } catch (FileNotFoundException ex) {
            ResourceHelper.errLog("Grabber > addUrlsToFile(...) > Error : %s", ex);
        }
    }

    public static void addDataToFile(File output, ArrayList<GrabbedImageData> gids,
            TreeMap<Integer, Boolean> urlMarkedToAdd, TreeSet<Command> set) {
        try (PrintWriter pw = new PrintWriter(new FileOutputStream(output))) {
            boolean printoutput = false;
            if (set.contains(Command.printmarkeddata)) {
                printoutput = true;
            }
            ArrayList<GrabbedImageData> list = new ArrayList<>();
            for (Map.Entry<Integer, Boolean> entry : urlMarkedToAdd.entrySet()) {
                if (entry.getValue()) {
                    gids.stream().anyMatch((GrabbedImageData t) -> {
                        if (entry.getKey().equals(t.getSerialnumber())) {
                            list.add(t);
                            return true;
                        }
                        return false;
                    });
                }
            }
            for (GrabbedImageData gid : list) {
                pw.printf("\n%-10s : %s\n", "Heading", gid.getHeading());
                pw.printf("%-10s : %s\n", "FileName", gid.getFileName());
                pw.printf("%-10s : %d\\x%d\n", "Size", gid.getWidth(), gid.getHeight());
                pw.printf("%-10s : %s\n", "URL", gid.getUrl());
                pw.printf("%-10s : %s\n", "Data", gid.getSideData());
            }
            if (printoutput) {
                for (GrabbedImageData gid : list) {
                    System.out.printf("\n%-10s : %s\n", "Heading", gid.getHeading());
                    System.out.printf("%-10s : %s\n", "FileName", gid.getFileName());
                    System.out.printf("%-10s : %dx%d\n", "Size", gid.getWidth(), gid.getHeight());
                    System.out.printf("%-10s : %s\n", "URL", gid.getUrl());
                    System.out.printf("%-10s : %s\n", "Data", gid.getSideData());
                }
            }
        } catch (FileNotFoundException ex) {
            ResourceHelper.errLog("Grabber > addDataToFile(...) > Error : %s", ex);
        }
    }

    public static void showHelp() {
        try (Scanner sc = new Scanner(ResourceHelper
                .getURLResource(Constants.RESOURCE_GRABBER_HELP_FILE).openStream())) {

            while (sc.hasNextLine()) {
                System.out.println(sc.nextLine());
            }
        } catch (Exception ex) {
            ResourceHelper.errLog("Grabber > showHelp() > Error : %s", ex);
        }
    }
}
