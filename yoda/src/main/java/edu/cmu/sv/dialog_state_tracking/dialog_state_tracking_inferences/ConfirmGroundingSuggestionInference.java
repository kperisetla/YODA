package edu.cmu.sv.dialog_state_tracking.dialog_state_tracking_inferences;

import edu.cmu.sv.dialog_management.DialogRegistry;
import edu.cmu.sv.dialog_state_tracking.*;
import edu.cmu.sv.domain.yoda_skeleton.YodaSkeletonOntologyRegistry;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.system_action.dialog_act.core_dialog_acts.Accept;
import edu.cmu.sv.system_action.dialog_act.core_dialog_acts.Fragment;
import edu.cmu.sv.system_action.dialog_act.grounding_dialog_acts.RequestConfirmValue;
import edu.cmu.sv.utils.Assert;
import edu.cmu.sv.utils.NBestDistribution;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.json.simple.JSONObject;

/**
 * Created by David Cohen on 10/18/14.
 */
public class ConfirmGroundingSuggestionInference extends DialogStateUpdateInference {
    static double penaltyForNonGroundedMatch = .1;
    @Override
    public NBestDistribution<DialogState> applyAll(
            YodaEnvironment yodaEnvironment, DialogState currentState, Turn turn, long timeStamp) {
        NBestDistribution<DialogState> ans = new NBestDistribution<>();

        if (turn.speaker.equals("user")) {
            for (String sluHypothesisID : turn.hypothesisDistribution.keySet()) {
                SemanticsModel hypModel = turn.hypotheses.get(sluHypothesisID);
                Double sluScore = turn.hypothesisDistribution.get(sluHypothesisID);
                String dialogAct = hypModel.getSlotPathFiller("dialogAct");
                if (DialogRegistry.dialogActNameMap.get(dialogAct).equals(Accept.class)) {
                    for (String predecessorId : currentState.discourseUnitHypothesisMap.keySet()) {
                        DialogState newDialogState = currentState.deepCopy();
                        DiscourseUnit predecessor = newDialogState.discourseUnitHypothesisMap.get(predecessorId);
                        newDialogState.misunderstandingCounter = 0;

                        DiscourseAnalysis duAnalysis = new DiscourseAnalysis(predecessor, yodaEnvironment);
                        try {
                            Assert.verify(predecessor.initiator.equals("user"));
                            Assert.verify(duAnalysis.ungroundedByAct(RequestConfirmValue.class));
                            duAnalysis.analyseSuggestions();
                            duAnalysis.analyseCommonGround();
                        } catch (Assert.AssertException e){
                            continue;
                        }

                        // copy suggestion and ground the discourse unit
                        SemanticsModel newSpokenByThemHypothesis = predecessor.getSpokenByMe().deepCopy();
                        SemanticsModel.unwrap((JSONObject) newSpokenByThemHypothesis.newGetSlotPathFiller(duAnalysis.suggestionPath),
                                YodaSkeletonOntologyRegistry.hasValue.name);
                        Utils.returnToGround(predecessor, newSpokenByThemHypothesis, timeStamp);

                        // collect the result
                        Double score = (duAnalysis.groundMatch ? 1.0 : penaltyForNonGroundedMatch) * sluScore *
                                Utils.discourseUnitContextProbability(newDialogState, predecessor);
//                        System.out.println("ConfirmGroundingSuggestion: groundmatch:"+ duAnalysis.groundMatch);
                        ans.put(newDialogState, score);
                    }

                } else if (DialogRegistry.dialogActNameMap.get(dialogAct).equals(Fragment.class)) {
                    // todo: interpret the fragment as a confirmation if it has an attachment point
                }
            }
        }
        return ans;
    }

}
