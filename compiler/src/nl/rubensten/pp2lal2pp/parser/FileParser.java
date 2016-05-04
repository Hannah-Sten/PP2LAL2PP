package nl.rubensten.pp2lal2pp.parser;

import nl.rubensten.pp2lal2pp.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Ruben Schellekens
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

        try {
            handleDefinitions();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            throw new ParseException("The define statements were not correctly setup.");
        }
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
        StringBuilder total = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;
            while ((line = br.readLine()) != null) {
                total.append(line).append("\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new ParseException("Could not read file " + file);
        }

        return total.toString();
    }

    /**
     * Generates the filename from an include statement's value.
     */
    private Optional<File> getIncludeFile(String include) {
        String base = getMainDirectoryPath() + "\\" + include;

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

    private String getMainDirectoryPath() {
        return file.getAbsolutePath().replace("\\" + file.getName(), "");
    }

    /**
     * Replaces all definitions in the code.
     */
    private void handleDefinitions() {
        Map<String, String> toReplace = new LinkedHashMap<>();

        for (String line : input.split("\\n")) {
            if (line.trim().toLowerCase().startsWith("define")) {
                String[] parts = line.split("( )+");
                toReplace.put(line, "");
                toReplace.put(parts[1], parts[2]);
            }
        }

        for (String key : toReplace.keySet()) {
            String replacement = toReplace.get(key);
            input = input.replaceAll(key, replacement.trim());
        }
    }

}
