package edu.cmu.sv.ontology.noun;

import edu.cmu.sv.natural_language_generation.LexicalEntry;
import edu.cmu.sv.ontology.ThingWithRoles;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by David Cohen on 9/20/14.
 */
public abstract class Noun extends ThingWithRoles{

    static Set<LexicalEntry> lexicalEntries = new HashSet<>();
    static {
        LexicalEntry entry = new LexicalEntry();
        entry.whPronouns = new HashSet<>();
        entry.whPronouns.add("what");
    }

    @Override
    public Set<LexicalEntry> getLexicalEntries(){return lexicalEntries;}
}
