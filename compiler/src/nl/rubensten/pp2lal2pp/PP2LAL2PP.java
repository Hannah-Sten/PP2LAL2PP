package nl.rubensten.pp2lal2pp;

import nl.rubenlib.io.FileWorker;
import nl.rubensten.pp2lal2pp.compiler.LineTokeniser;
import nl.rubensten.pp2lal2pp.compiler.Tokeniser;
import nl.rubensten.pp2lal2pp.lang.Program;
import nl.rubensten.pp2lal2pp.parser.FileParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Practicum Processor To Learn Assembly Language To Preserve Prosperity-Language compiler.
 *
 * @author Ruben Schellekens, Sten Wessel
 * @version 0.1
 */
public class PP2LAL2PP {

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        List<String> argList = new ArrayList<>(Arrays.asList(args));

        // Parse file name.
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("File '" + file.getName() + "' does not exist!");
            return;
        }

        // Destination argument.
        File dest = new File(args[0].replaceAll("(\\.[a-zA-Z0-9_\\-]*)$", ".asm"));
        if (argList.contains("-d")) {
            int index = argList.indexOf("-d");

            if (index + 1 >= argList.size()) {
                System.out.println("No destination has been specified.");
                return;
            }

            dest = new File(argList.get(index + 1));
        }
    }

    private static String readFile(File file) {
        try {
            StringBuilder contents = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                contents.append(line).append("\n");
            }
            return contents.toString();
        }
        catch (IOException e) {
            throw new PP2LAL2PPException(e.getMessage());
        }
    }

    private static void writeFile(File file, String contents) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(contents);
            fw.close();
        }
        catch (IOException e) {
            throw new PP2LAL2PPException(e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("");
        System.out.println("-=---------------------------------------------------------=-");
        System.out.println("Compile PP2LAL2PP: 'java -jar JARNAME.jar <file.pp2> [-args]'");
        System.out.println("-=---------------------------------------------------------=-");
        System.out.println("Arguments:");
        System.out.println("    -d <destination.asm>\tdestination assembly file");
        System.out.println("");
    }

}
