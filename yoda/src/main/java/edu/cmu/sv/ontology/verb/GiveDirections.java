package edu.cmu.sv.ontology.verb;

import edu.cmu.sv.natural_language_generation.Lexicon;
import edu.cmu.sv.ontology.role.Destination;
import edu.cmu.sv.ontology.role.Role;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by David Cohen on 12/19/14.
 */
public class GiveDirections extends Verb {
    static Set<Lexicon.LexicalEntry> lexicalEntries = new HashSet<>();
    static Set<Class <? extends Role>> requiredGroundedRoles = new HashSet<>();
    static {
        Lexicon.LexicalEntry entry = new Lexicon.LexicalEntry();
        entry.presentSingularVerbs.add("give directions");
        lexicalEntries.add(entry);
        requiredGroundedRoles.add(Destination.class);
    }

    @Override
    public Set<Lexicon.LexicalEntry> getLexicalEntries() {
        return lexicalEntries;
    }

    @Override
    public Set<Class<? extends Role>> getRequiredGroundedRoles() {
        return requiredGroundedRoles;
    }
}
