package edu.cmu.sv.natural_language_generation.top_level_templates;

import edu.cmu.sv.natural_language_generation.TopLevelNLGTemplate;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Created by David Cohen on 10/29/14.
 */
public class RequestFixMisunderstandingNLGTemplate implements TopLevelNLGTemplate {
    @Override
    public ImmutablePair<String, SemanticsModel> generate(SemanticsModel constraints, YodaEnvironment yodaEnvironment) {
        return new ImmutablePair<>("I'm sorry could you rephrase that", constraints.deepCopy());
    }
}
