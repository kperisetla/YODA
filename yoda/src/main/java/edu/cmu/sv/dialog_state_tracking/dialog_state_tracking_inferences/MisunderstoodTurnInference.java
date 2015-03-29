package edu.cmu.sv.dialog_state_tracking.dialog_state_tracking_inferences;

import edu.cmu.sv.dialog_state_tracking.DialogState;
import edu.cmu.sv.dialog_state_tracking.DiscourseUnit;
import edu.cmu.sv.dialog_state_tracking.Turn;
import edu.cmu.sv.dialog_state_tracking.dialog_state_tracking_inferences.DialogStateUpdateInference;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.utils.NBestDistribution;
import edu.cmu.sv.utils.StringDistribution;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Cohen on 9/19/14.
 *
 * Infers the dialog state after misunderstanding a user turn
 *
 */
public class MisunderstoodTurnInference extends DialogStateUpdateInference {
    public static final double probabilityUserTurnMisunderstood = .08;
    public static final String duString = "Misunderstood";

    @Override
    public NBestDistribution<DialogState> applyAll(
            YodaEnvironment yodaEnvironment, DialogState currentState, Turn turn, long timeStamp) {

        NBestDistribution<DialogState> resultHypotheses = new NBestDistribution<>();

        if (turn.speaker.equals("user")) {
            DiscourseUnit newDUHypothesis = new DiscourseUnit();
            SemanticsModel newSpokenByThemHypothesis = new SemanticsModel("{\"dialogAct\":\""+duString+"\"}");
            newDUHypothesis.timeOfLastActByThem = timeStamp;
            newDUHypothesis.spokenByThem = newSpokenByThemHypothesis;
            newDUHypothesis.groundInterpretation = newSpokenByThemHypothesis;
            newDUHypothesis.initiator = turn.speaker;
            DialogState newDialogState = currentState.deepCopy();
            newDialogState.discourseUnitCounter += 1;
            newDialogState.getDiscourseUnitHypothesisMap().
                    put("du_" + newDialogState.discourseUnitCounter, newDUHypothesis);

//            newDUHypothesis.actionAnalysis.update(yodaEnvironment, newDUHypothesis);
            newDialogState.misunderstandingCounter ++;
            resultHypotheses.put(newDialogState, probabilityUserTurnMisunderstood);
        }
        return resultHypotheses;
    }
}
