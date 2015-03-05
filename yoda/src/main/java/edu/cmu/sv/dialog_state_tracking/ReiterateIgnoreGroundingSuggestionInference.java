package edu.cmu.sv.dialog_state_tracking;

import edu.cmu.sv.database.dialog_task.ReferenceResolution;
import edu.cmu.sv.dialog_management.DialogRegistry;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.system_action.dialog_act.core_dialog_acts.Fragment;
import edu.cmu.sv.system_action.dialog_act.grounding_dialog_acts.RequestConfirmValue;
import edu.cmu.sv.utils.Assert;
import edu.cmu.sv.utils.StringDistribution;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Cohen on 10/18/14.
 *
 * This can either be a correction or a confirmation /  back-channel agreement
 *
 */
public class ReiterateIgnoreGroundingSuggestionInference extends DialogStateUpdateInference {
    static double penaltyForThisInference = .2; // this inference is not likely
    static double penaltyForNonGroundedMatch = .1;

    @Override
    public Pair<Map<String, DialogState>, StringDistribution> applyAll(
            YodaEnvironment yodaEnvironment, DialogState currentState, Turn turn, long timeStamp) {
        StringDistribution resultDistribution = new StringDistribution();
        Map<String, DialogState> resultHypotheses = new HashMap<>();

        int newHypothesisCounter = 0;
        if (turn.speaker.equals("user")) {
            for (String sluHypothesisID : turn.hypothesisDistribution.keySet()) {
                SemanticsModel hypModel = turn.hypotheses.get(sluHypothesisID);
                Double sluScore = turn.hypothesisDistribution.get(sluHypothesisID);
                String dialogAct = hypModel.getSlotPathFiller("dialogAct");
                if (DialogRegistry.dialogActNameMap.get(dialogAct).equals(Fragment.class)) {
                    for (String predecessorId : currentState.discourseUnitHypothesisMap.keySet()) {
                        DiscourseUnit predecessor = currentState.discourseUnitHypothesisMap.get(predecessorId).deepCopy();

                        JSONObject correctionContent;
                        DiscourseAnalysis duAnalysis = new DiscourseAnalysis(predecessor, yodaEnvironment);
                        try {
                            Assert.verify(predecessor.initiator.equals("user"));
                            Assert.verify(duAnalysis.ungroundedByAct(RequestConfirmValue.class));
                            duAnalysis.analyseSuggestions();
                            duAnalysis.analyseCommonGround();
                            correctionContent = (JSONObject) hypModel.newGetSlotPathFiller("topic");
                        } catch (Assert.AssertException e){
                            continue;
                        }

                        // copy suggestion, re-resolveDiscourseUnit the discourse unit
                        SemanticsModel newSpokenByThemHypothesis = predecessor.getSpokenByThem().deepCopy();
                        if (newSpokenByThemHypothesis.newGetSlotPathFiller(duAnalysis.suggestionPath)==null){
                            SemanticsModel.putAtPath(newSpokenByThemHypothesis.getInternalRepresentation(),
                                    duAnalysis.suggestionPath, correctionContent);
                        } else {
                            SemanticsModel.overwrite(((JSONObject) newSpokenByThemHypothesis.newGetSlotPathFiller(duAnalysis.suggestionPath)),
                                    correctionContent);
                        }
                        SemanticsModel.putAtPath(predecessor.groundInterpretation.getInternalRepresentation(),
                                duAnalysis.suggestionPath, null);
                        Utils.returnToGround(predecessor, newSpokenByThemHypothesis, timeStamp);

                        Pair<Map<String, DiscourseUnit>, StringDistribution> groundedHypotheses =
                                ReferenceResolution.resolveDiscourseUnit(predecessor, yodaEnvironment);
                        for (String groundedDuKey: groundedHypotheses.getRight().keySet()) {
                            String newDialogStateHypothesisID = "dialog_state_hyp_" + newHypothesisCounter++;
                            DiscourseUnit currentDu = groundedHypotheses.getLeft().get(groundedDuKey);
                            DialogState newDialogState = currentState.deepCopy();
                            newDialogState.getDiscourseUnitHypothesisMap().put(predecessorId, currentDu);

                            currentDu.actionAnalysis.update(yodaEnvironment, currentDu);
                            resultHypotheses.put(newDialogStateHypothesisID, newDialogState);
                            Double score = groundedHypotheses.getRight().get(groundedDuKey) *
                                    penaltyForThisInference * sluScore *
                                    Utils.discourseUnitContextProbability(newDialogState, currentDu);
                            resultDistribution.put(newDialogStateHypothesisID, score);
                        }
                    }
                }
            }
        } else { // if turn.speaker.equals("system")
        }
        return new ImmutablePair<>(resultHypotheses, resultDistribution);
    }

}
