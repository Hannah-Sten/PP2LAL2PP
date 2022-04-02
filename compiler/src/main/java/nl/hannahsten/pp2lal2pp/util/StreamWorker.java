package nl.hannahsten.pp2lal2pp.util;

import nl.hannahsten.pp2lal2pp.PP2LAL2PPException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Hannah Schellekens
 */
public class StreamWorker {

    /**
     * The stream to work with.
     */
    private final InputStream stream;

    public StreamWorker(InputStream stream) {
        this.stream = stream;
    }

    /**
     * Reads all the contents of a file and puts it into a single string.
     *
     * @return The contents of the file.
     * @throws PP2LAL2PPException
     *         if an error occurs when reading the stream.
     */
    public String read() throws PP2LAL2PPException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));

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
            throw new PP2LAL2PPException("Could not read from stream.");
        }
    }

}
