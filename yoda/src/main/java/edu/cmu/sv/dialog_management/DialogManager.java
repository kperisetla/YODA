package edu.cmu.sv.dialog_management;

import edu.cmu.sv.dialog_state_tracking.DialogStateHypothesis;
import edu.cmu.sv.dialog_state_tracking.DiscourseUnitHypothesis;
import edu.cmu.sv.natural_language_generation.Grammar;
import edu.cmu.sv.utils.StringDistribution;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import edu.cmu.sv.system_action.SystemAction;
import edu.cmu.sv.system_action.dialog_act.*;

import edu.cmu.sv.utils.HypothesisSetManagement;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

/**
 * Created by David Cohen on 9/2/14.
 *
 * Contains a dialog state tracker and specification of interfaces, etc.
 * Contains functions for assessing potential dialog moves.
 * Contains a main method which is the dialog agent loop.
 *
 */
public class DialogManager implements Runnable {
    private static Logger logger = Logger.getLogger("yoda.dialog_management.DialogManager");
    private static FileHandler fh;
    static {
        try {
            fh = new FileHandler("DialogManager.log");
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        logger.addHandler(fh);
    }

    YodaEnvironment yodaEnvironment;
    StringDistribution dialogStateDistribution = new StringDistribution();
    Map<String, DialogStateHypothesis> dialogStateHypotheses = new HashMap<>();

    public YodaEnvironment getYodaEnvironment() {
        return yodaEnvironment;
    }

    public void setYodaEnvironment(YodaEnvironment yodaEnvironment) {
        this.yodaEnvironment = yodaEnvironment;
    }

    public DialogManager(YodaEnvironment yodaEnvironment) {
        this.yodaEnvironment = yodaEnvironment;
    }

    /*
    * Select the best dialog act given all the possible classes and bindings
    * */
    private List<Pair<SystemAction, Double>> enumerateAndScorePossibleActions() {
        try {

            Map<SystemAction, Double> actionExpectedReward = new HashMap<>();

            //// add the null action
            actionExpectedReward.put(null, 0.0);

            // enumerate and evaluate actions that can be enumerated from a single DU hypothesis
            for (String dialogStateHypothesisId : dialogStateHypotheses.keySet()){
                DialogStateHypothesis currentDialogStateHypothesis = dialogStateHypotheses.get(dialogStateHypothesisId);
                for (String discourseUnitHypothesisId : currentDialogStateHypothesis.getDiscourseUnitHypothesisMap().
                        keySet()) {
                    DiscourseUnitHypothesis currentDiscourseUnitHypothesis = currentDialogStateHypothesis.
                            getDiscourseUnitHypothesisMap().get(discourseUnitHypothesisId);
                    for (Class<? extends DialogAct> dialogActClass : DialogRegistry.argumentationDialogActs) {
                        DialogAct dialogActInstance = dialogActClass.newInstance();
                        // todo: enumerate parameters and perform this loop for each parameterization
                        Double currentReward = dialogActInstance.reward(
                                currentDialogStateHypothesis, currentDiscourseUnitHypothesis) *
                                dialogStateDistribution.get(dialogStateHypothesisId);
                        accumulateReward(actionExpectedReward, dialogActInstance, currentReward);
                    }
                }
            }

            //todo: enumerate and evaluate actions that require multiple DU hypotheses to be enumerated (ex: disambiguation)


            /*
            //// Get expected rewards for executing non-dialog tasks
            for (String hypothesisID : currentDialogState.getHypotheses().keySet()) {
                DiscourseUnitHypothesis.DiscourseUnitHypothesis dsHypothesis = currentDialogState.getHypotheses().get(hypothesisID);
                SemanticsModel hypothesis = dsHypothesis.getSpokenByThem();
                Class<? extends DialogAct> daClass = DialogRegistry.dialogActNameMap.
                        get(hypothesis.getSlotPathFiller("dialogAct"));
                // add contribution from non dialog tasks
                if (DialogRegistry.nonDialogTaskRegistry.containsKey(daClass)) {
                    for (Class<? extends NonDialogTask> taskClass : DialogRegistry.nonDialogTaskRegistry.get(daClass)) {
                        NonDialogTask task = taskClass.getDeclaredConstructor(Database.class).newInstance(yodaEnvironment.db);
                        task.setTaskSpec(hypothesis.deepCopy());
                        Double expectedReward = RewardAndCostCalculator.nonDialogTaskReward(currentDialogState, task);
                        actionExpectedReward.put(task, expectedReward);
                    }
                }
            }
            */

            return HypothesisSetManagement.keepNBestBeam(actionExpectedReward, 10000);
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    private void accumulateReward(Map<SystemAction, Double> actionExpectedReward, DialogAct dialogAct, Double currentReward){
        boolean alreadyFound = false;
        for (SystemAction key : actionExpectedReward.keySet()){
            if (key.evaluationMatch(dialogAct)){
                alreadyFound = true;
                actionExpectedReward.put(key, actionExpectedReward.get(key) + currentReward);
                break;
            }
        }
        if (!alreadyFound){
            actionExpectedReward.put(dialogAct, currentReward);
        }
    }

    @Override
    public void run() {
        while (true){
            try {
                Pair<Map<String, DialogStateHypothesis>, StringDistribution> DmInput = yodaEnvironment.DmInputQueue.poll(100, TimeUnit.MILLISECONDS);
                if (DmInput!=null) {
                    dialogStateHypotheses = DmInput.getLeft();
                    dialogStateDistribution = DmInput.getRight();
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
            List<Pair<SystemAction, Double>> rankedActions = enumerateAndScorePossibleActions();
            logger.info("Ranked actions: " + rankedActions.toString());
            SystemAction selectedAction = rankedActions.get(0).getKey();
            if (selectedAction!=null)
                yodaEnvironment.nlg.speak(((DialogAct)selectedAction).getNlgCommand(), Grammar.DEFAULT_GRAMMAR_PREFERENCES);
        }
    }
}
