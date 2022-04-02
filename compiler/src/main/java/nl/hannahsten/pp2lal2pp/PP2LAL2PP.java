package nl.hannahsten.pp2lal2pp;

import nl.hannahsten.pp2lal2pp.compiler.Compiler;
import nl.hannahsten.pp2lal2pp.lang.GlobalVariable;
import nl.hannahsten.pp2lal2pp.lang.Program;
import nl.hannahsten.pp2lal2pp.parser.FileParser;
import nl.hannahsten.pp2lal2pp.parser.Parser;
import nl.hannahsten.pp2lal2pp.util.Template;
import nl.hannahsten.pp2lal2pp.util.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Practicum Processor To Learn Assembly Language To Preserve Prosperity-Language compiler.
 *
 * @author Hannah Schellekens, Sten Wessel
 */
public class PP2LAL2PP {

    public static String VERSION = "Version 2.0";

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        if (args.length == 0) {
            printHelp();
            return;
        }

        List<String> argList = new ArrayList<>(Arrays.asList(args));

        // Check things for auto-assemble.
        if (argList.contains("-a")) {
            int index = argList.indexOf("-a");
            String jar = argList.get(index + 1);
            File file = new File(jar);

            if (!file.exists()) {
                System.out.println("There is no assembler JAR called '" + jar + "'.");
                System.exit(1);
            }
        }

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
        System.out.println("PP2LAL2PP Compiler " + VERSION + " by Hannah-Sten");
        System.out.println("Done (" + time + "s). " +
                "Compiled '" + file.getName() + "' to '" + dest.getName() + ".");
        System.out.println();

        // Auto assemble
        if (argList.contains("-a")) {
            int index = argList.indexOf("-a");
            String jar = argList.get(index + 1);
            String hex = argList.get(index + 2);

            try {
                compileToHex(jar, dest.getAbsolutePath(), hex);
            }
            catch (IOException ioe) {
                System.out.println("Couldn't auto-assemble " + dest.getName());
                ioe.printStackTrace();
            }

            delta = System.currentTimeMillis() - start;
            time = (float)delta / 1000f;
            System.out.println("Done (" + time + "s). " +
                    "Assembled '" + dest.getName() + "' to '" + hex + ".");
        }
    }

    private static void printHelp() {
        System.out.println();
        System.out.println
                ("-=-----------------------------------------------------------------------------=-");
        System.out.println("       Practicum Processor 2 Learn Assembly Language 2 Preserve " +
                "Prosperity");
        System.out.println(fillTo("", 39) + VERSION);
        System.out.println("              PP2LAL2PP: 'java -jar JARNAME.jar [-args] <fileName>'");
        System.out.println
                ("-=-----------------------------------------------------------------------------=-");
        System.out.println("Flags:");
        System.out.println(fillTo("    -a <assembler.jar> <output.hex>", 40) +
                "compiles it to a PP2hex-file");
        System.out.println(fillTo("    -b #,#,#,#,...", 40) +
                "sequence of banned global base locations");
        System.out.println(fillTo("    -d <destination>", 40) + "destination file");
        System.out.println(fillTo("    -r ", 40) + "refactor file");
        System.out.println(fillTo("    -u ", 40) + "unpack templates");
    }

    /**
     * Fills the string up to a certain amount of characters.
     */
    private static String fillTo(String string, int amount) {
        String filler = Util.makeString(" ", amount - string.length());
        return string + filler;
    }

    /**
     * Compiles an asm result.
     *
     * @param assembler
     *         The JAR-file of the assembler.
     * @param input
     *         The file name of the input ASM-file.
     * @param hex
     *         The file name of the output HEX-file.
     */
    private static void compileToHex(String assembler, String input, String hex) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", "" + assembler + "",
                "" + input + "", hex + "\"");
        final Process process = builder.start();

        new Thread(() -> readStream(process.getInputStream())).start();
        new Thread(() -> readStream(process.getErrorStream())).start();

        try {
            process.waitFor();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println();
    }

    /**
     * Prints everything received in the given stream to the standard output.
     *
     * @param stream
     *         The stream to output the data of.
     */
    private static void readStream(InputStream stream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
