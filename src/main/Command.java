package main;

import java.util.TreeSet;

public enum Command {

    display("-d", "-display", null),
    listallurls("-lu", "-listallurls", null),
    listallfilenames("-lf", "-listallfilenames", null),
    listalldata("-ld", "-listalldata", null),
    showselectedimagedata("-ssid", "-showselectedimagedata", null),
    googlescreenshot("-gss", "-googlescreenshot", new StringBuffer()),
    googlexml("-gxml", "-googlexml", new StringBuffer()),
    outputdir("-od", "-outputdir", new StringBuffer()),
    addimages("-ai", "-addimages", null),
    addclones("-ac", "-addclones", null),
    printmarkeddata("-pmd", "printmarkeddata", null),
    printsize("-ps", "-printsize", null),
    corrector("-c", "-corrector", null),
    debug("-db", "-debug", null),
    invalid("-invalid", "-invalid", null);

    public final String optionMin;
    public final String optionLarge;
    public final StringBuffer value;
    public final boolean hasVal;

    Command(String opmin, String oplarge, StringBuffer val) {
        this.optionMin = opmin;
        this.optionLarge = oplarge;
        this.value = val;
        this.hasVal = val != null;
    }

    public static Command parseFromOption(String op) {
        Command cmd = null;
        for (Command c : Command.values()) {
            if (c.doOptionMatched(op)) {
                cmd = c;
                break;
            }
        }
        if(cmd == null){
            cmd = Command.invalid;
        }
        return cmd;
    }

    public boolean doOptionMatched(String op) {
        return optionMin.equalsIgnoreCase(op) || optionLarge.equalsIgnoreCase(op);
    }

    public static TreeSet<Command> readCommands(String... args) {
        TreeSet<Command> set = new TreeSet<>();
        try {
            for (int i = 0; i < args.length; i++) {
                Command cmd = parseFromOption(args[i]);
                if (cmd.hasVal) {
                    i++;
                    cmd.value.setLength(args[i].length());
                    cmd.value.replace(0, args[i].length(), args[i]);
                }
                set.add(cmd);
            }
        } catch (Exception ex) {
            System.err.println("Error in parsing command ...");
        }
        return set;
    }
}
