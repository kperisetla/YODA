package edu.cmu.sv.spoken_language_understanding;

import java.util.Arrays;
import java.util.List;

/**
 * Created by David Cohen on 12/29/14.
 */
public class Tokenizer {
    public static List<String> tokenize(String inputString){
        inputString = inputString.toLowerCase();

        // get rid of <noise>, <unk>, etc.
        inputString = inputString.replaceAll("<.*?>", "");

        // split up contractions
        inputString = inputString.replaceAll("n't", " not");
        inputString = inputString.replaceAll("'re", " are");
        inputString = inputString.replaceAll("'ll", " will");
        inputString = inputString.replaceAll("i'm", "i am");
        inputString = inputString.replaceAll("'s", " 's");
        inputString = inputString.replaceAll("s' |s'$", "s 's");
        inputString = inputString.replaceAll("'d", " 'd");

        inputString = inputString.trim();
        // split at white space, between alpha and num, between num and alpha
        return Arrays.asList(inputString.split("\\s+|(?<=\\p{Alpha})(?=\\p{Digit})|(?<=\\p{Digit})(?=\\p{Alpha})"));
    }
}
