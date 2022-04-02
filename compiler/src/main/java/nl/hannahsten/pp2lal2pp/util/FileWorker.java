package nl.hannahsten.pp2lal2pp.util;

import nl.hannahsten.pp2lal2pp.PP2LAL2PPException;

import java.io.*;

/**
 * @author Hannah Schellekens
 */
public class FileWorker {

    /**
     * The file to work with.
     */
    private final File file;

    public FileWorker(File file) {
        this.file = file;
    }

    /**
     * Writes the contents of the given string to the file.
     *
     * @param string
     *         The string to write to the file.
     * @param append
     *         <code>true</code> if you want to add to the file's contents, <code>false</code> to
     *         overwrite.
     * @throws PP2LAL2PPException
     *         If an IOException occured when writing to the file.
     */
    public void write(String string, boolean append) throws PP2LAL2PPException {
        try {
            FileWriter fw = new FileWriter(file, append);
            fw.write(string);
            fw.close();
        }
        catch (IOException e) {
            throw new PP2LAL2PPException("Could not write to file: " + file.getAbsolutePath());
        }
    }

    /**
     * Reads all the contents of a file and puts it into a single string.
     *
     * @return The contents of the file.
     * @throws PP2LAL2PPException
     *         If an IOException occured when reading from the file.
     */
    public String read() throws PP2LAL2PPException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            StringBuilder sb = new StringBuilder();
            boolean firstDone = false;

            String line;
            while ((line = br.readLine()) != null) {
                if (firstDone) {
                    sb.append("\n");
                }

                firstDone = true;
                sb.append(line);
            }

            br.close();

            return sb.toString();
        }
        catch (IOException e) {
            throw new PP2LAL2PPException("Could not read from file: " + file.getAbsolutePath());
        }
    }

}
