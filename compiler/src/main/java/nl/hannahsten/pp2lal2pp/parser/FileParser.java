package nl.hannahsten.pp2lal2pp.parser;

import nl.hannahsten.pp2lal2pp.ParseException;
import nl.hannahsten.pp2lal2pp.util.FileWorker;

import java.io.File;
import java.util.*;

/**
 * @author Hannah Schellekens
 */
public class FileParser extends Parser {

    /**
     * The file to parse.
     */
    private File file;

    public FileParser(File file) {
        this((String)null);
        this.file = file;

        readInput();
        includeFiles();
    }

    private FileParser(String input) {
        super(input);
    }

    /**
     * Reads all the text in the file and stores it in {@link Parser#input}.
     */
    private void readInput() throws ParseException {
        this.input = getFileContents(file);
    }

    /**
     * Includes all files that have to be included.
     */
    private void includeFiles() {
        List<String> includedFiles = new ArrayList<>();
        includedFiles.add(file.getAbsolutePath());

        while (hasIncludes()) {
            Map<String, String> toReplace = new HashMap<>();

            for (String line : input.split("\n")) {
                line = line.trim();

                if (line.toLowerCase().startsWith("include")) {
                    String[] parts = line.split("^(include(\\s)+)");
                    String toInclude = parts[1];

                    Optional<File> opFile = getIncludeFile(toInclude);
                    if (!opFile.isPresent()) {
                        throw new ParseException("Could not find a file to include for '" +
                                toInclude + "'.");
                    }

                    File includeFile = opFile.get();
                    if (includedFiles.contains(includeFile.getAbsolutePath())) {
                        throw new ParseException("File " + toInclude + " has already been included.");
                    }

                    toReplace.put("include " + toInclude, getFileContents(includeFile));
                    includedFiles.add(includeFile.getAbsolutePath());
                }
            }

            for (String key : toReplace.keySet()) {
                String replacement = toReplace.get(key);
                input = input.replace(key, replacement.trim());
            }
        }
    }

    /**
     * Checks if the input hsa include statements left.
     */
    private boolean hasIncludes() {
        for (String line : input.split("\n")) {
            if (line.trim().startsWith("include")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reads all the contents of the given file.
     */
    private String getFileContents(File file) {
        return new FileWorker(file).read();
    }

    /**
     * Generates the filename from an include statement's value.
     */
    private Optional<File> getIncludeFile(String include) {
        String base = file.getAbsolutePath().replace(file.getName(), include);

        // First check if the include file is valid.
        File file = new File(base);
        if (file.exists()) {
            return Optional.of(file);
        }

        // Then check .pp2lal2pp
        file = new File(base + ".pp2lal2pp");
        if (file.exists()) {
            return Optional.of(file);
        }

        // Then check .pp2
        file = new File(base + ".pp2");
        if (file.exists()) {
            return Optional.of(file);
        }

        return Optional.empty();
    }

}
