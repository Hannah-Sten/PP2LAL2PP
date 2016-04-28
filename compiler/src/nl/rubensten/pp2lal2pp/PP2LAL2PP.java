package nl.rubensten.pp2lal2pp;

import nl.rubenlib.io.FileWorker;
import nl.rubensten.pp2lal2pp.compiler.LineTokeniser;
import nl.rubensten.pp2lal2pp.compiler.Tokeniser;
import nl.rubensten.pp2lal2pp.parser.FileParser;

import java.io.File;

/**
 * Practicum Processor To Learn Assembly Language To Preserve Prosperity-Language compiler.
 *
 * @author Ruben Schellekens, Sten Wessel
 * @version 0.1
 */
public class PP2LAL2PP {

    public static void main(String[] args) {
        File file = new File("test.pp2");
        //FileParser parser = new FileParser(file);

        String code = new FileWorker(file).readFully();

        LineTokeniser tokeniser = new LineTokeniser(code);
        for (String line : tokeniser) {
            StringBuilder sb = new StringBuilder(line.length());
            Tokeniser tokeniser1 = new Tokeniser(line);
            for (String token : tokeniser1) {
                sb.append("[").append(token).append("]");
            }
            System.out.println(sb.toString());
        }
    }

}
