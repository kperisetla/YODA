package edu.cmu.sv.system_action.dialog_act.core_dialog_acts;

import edu.cmu.sv.dialog_state_tracking.DialogState;
import edu.cmu.sv.dialog_state_tracking.DiscourseUnit;
import edu.cmu.sv.domain.yoda_skeleton.ontology.Thing;
import edu.cmu.sv.domain.yoda_skeleton.ontology.noun.Noun;
import edu.cmu.sv.system_action.dialog_act.DialogAct;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Cohen on 9/8/14.
 */
public class WHQuestion extends DialogAct {
    static Map<String, Object> individualParameters = new HashMap<>();
    static Map<String, Object> classParameters = new HashMap<>();
    static Map<String, Object> descriptionParameters = new HashMap<>();
    static Map<String, Object> pathParameters = new HashMap<>();
    @Override
    public Map<String, Object> getPathParameters() {
        return pathParameters;
    }
    @Override
    public Map<String, Object> getDescriptionParameters() {
        return descriptionParameters;
    }
    static{
        individualParameters.put("topic_individual", Noun.class);
    }
    @Override
    public Map<String, Object> getClassParameters() {
        return classParameters;
    }
    @Override
    public Map<String, Object> getIndividualParameters() {
        return individualParameters;
    }
    @Override
    public Double reward(DialogState dialogState, DiscourseUnit discourseUnit) {
        return null;
    }
}
