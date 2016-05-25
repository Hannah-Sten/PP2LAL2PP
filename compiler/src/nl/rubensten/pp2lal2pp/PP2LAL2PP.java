package nl.rubensten.pp2lal2pp;

import nl.rubensten.pp2lal2pp.compiler.Compiler;
import nl.rubensten.pp2lal2pp.lang.GlobalVariable;
import nl.rubensten.pp2lal2pp.lang.Program;
import nl.rubensten.pp2lal2pp.parser.FileParser;
import nl.rubensten.pp2lal2pp.parser.Parser;
import nl.rubensten.pp2lal2pp.util.Template;

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

        // Unpack templates.
        if (argList.contains("-u")) {
            int amount = Template.unpack();
            System.out.println("Unpacked " + amount + " template files.");
            return;
        }

        // Parse file name.
        File file = new File(args[args.length - 1]);
        if (!file.exists()) {
            System.out.println("File '" + file.getName() + "' does not exist!");
            return;
        }

        // Destination flag.
        File dest = new File(args[args.length - 1].replaceAll("(\\.[a-zA-Z0-9_\\-]*)$", ".asm"));
        if (argList.contains("-d")) {
            int index = argList.indexOf("-d");

            if (index + 1 >= argList.size()) {
                System.out.println("No destination has been specified.");
                return;
            }

            dest = new File(argList.get(index + 1));
        }

        // Banned global base flag.
        if (argList.contains("-b")) {
            int index = argList.indexOf("-b");

            if (index + 1 >= argList.size()) {
                System.out.println("No number sequence has been specified.");
                return;
            }

            String sequence = argList.get(index + 1);
            String[] numbers = sequence.split(",");
            for (String num : numbers) {
                try {
                    GlobalVariable.banPointer(Integer.parseInt(num));
                }
                catch (NumberFormatException nfe) {
                    System.out.println("Number sequence is not entered properly.");
                    return;
                }
            }
            GlobalVariable.adjustCounter();
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
        System.out.println("Done (" + time + "s). " +
                "Compiled '" + file.getName() + "' to '" + dest.getName() + "'.");
    }

    private static void printHelp() {
        System.out.println("");
        System.out.println("-=-----------------------------------------------------------------=-");
        System.out.println(" Practicum Processor 2 Learn Assembly Language 2 Preserve Prosperity");
        System.out.println("        PP2LAL2PP: 'java -jar JARNAME.jar [-args] <fileName>'");
        System.out.println("-=-----------------------------------------------------------------=-");
        System.out.println("Flags:");
        System.out.println("    -b #,#,#,#,...\t\tSequence of banned global base locations");
        System.out.println("    -d <destination>\tdestination file");
        System.out.println("    -r \t\t\t\t\trefactor file");
        System.out.println("    -u \t\t\t\t\tunpack templates");
        System.out.println("");
    }

}
