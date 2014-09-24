package edu.cmu.sv.system_action.dialog_act.clarification_dialog_acts;

import edu.cmu.sv.dialog_state_tracking.DiscourseUnit;
import edu.cmu.sv.dialog_management.RewardAndCostCalculator;
import edu.cmu.sv.system_action.dialog_act.DialogAct;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Cohen on 9/8/14.
 */
public class RequestConfirmValue extends DialogAct {
    private Map<String, String> boundVariables = null;
    static Map<String, String> parameters = new HashMap<>();
    static {
        parameters.put("v1", "value");
    }

    // template "<v1> ?"

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public Map<String, String> getBindings() {
        return boundVariables;
    }

    @Override
    public DialogAct bindVariables(Map<String, String> bindings) {
        boundVariables = bindings;
        return this;
    }

    @Override
    public Double reward(DiscourseUnit DU) {
        try {
            return RewardAndCostCalculator.clarificationDialogActReward(db, DU,
                    RewardAndCostCalculator.predictConfidenceGainFromValueConfirmation(DU,
                            boundVariables.get("v1")));
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Double cost(DiscourseUnit DU) {
        // we oblige the user to a simple yes/no, which is < one phrase
        return RewardAndCostCalculator.penaltyForObligingUserPhrase*.75 +
                RewardAndCostCalculator.penaltyForSpeakingPhrase *1;
    }
}
