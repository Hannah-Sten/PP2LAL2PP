package nl.rubensten.pp2lal2pp;

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
        FileParser parser = new FileParser(file);

        String code = "@#491(034!*****1:==14:=1:=>=123<41=213=<=12#><>5 bambi\n  3+x/4 is " +
                "{kmaeel{}{{{}}{}{}{}{}{}{{}}}}}}  " +
                " " +
                "\n\t\t    de aller " +
                "be\n  ste!";
        LineTokeniser tokeniser = new LineTokeniser(code);
        for (String line : tokeniser) {
            Tokeniser tokeniser1 = new Tokeniser(line);
            for (String token : tokeniser1) {
                System.out.println(token);
            }
        }
    }

}
