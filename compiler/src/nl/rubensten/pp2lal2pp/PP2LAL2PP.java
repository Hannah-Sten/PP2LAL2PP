package nl.rubensten.pp2lal2pp;

import nl.rubensten.pp2lal2pp.compiler.Compiler;
import nl.rubensten.pp2lal2pp.lang.Program;
import nl.rubensten.pp2lal2pp.parser.FileParser;
import nl.rubensten.pp2lal2pp.parser.Parser;

import java.io.File;
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
        long start = System.currentTimeMillis();

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

        // Parse file
        Parser parser = new FileParser(file);
        Program program = parser.parse();

        // Compile file
        Compiler compiler = new Compiler(dest, program);
        compiler.compile();

        // Finish
        long delta = System.currentTimeMillis() - start;
        float time = (float)delta / 1000f;
        System.out.println("Done (" + time + "s). Compiled to " + dest.getName() + ".");
    }

    private static void printHelp() {
        System.out.println("");
        System.out.println("-=---------------------------------------------------------=-");
        System.out.println("Compile PP2LAL2PP: 'java -jar JARNAME.jar [-args] <fileName>'");
        System.out.println("-=---------------------------------------------------------=-");
        System.out.println("Arguments:");
        System.out.println("    -d <destination.asm>\tdestination assembly file");
        System.out.println("");
    }

}
