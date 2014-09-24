package edu.cmu.sv.system_action.dialog_task;

import edu.cmu.sv.database.Database;
import edu.cmu.sv.semantics.SemanticsModel;

/**
 * Created by David Cohen on 9/3/14.
 */
public class RespondToWHQuestionTask extends DialogTask {
    private static DialogTaskPreferences preferences = new DialogTaskPreferences(.5,1,2);
    private SemanticsModel taskSpec = null;

    private Database db;

    public RespondToWHQuestionTask(Database db) {
        this.db = db;
    }

    @Override
    public void execute() {
        System.out.println("executing 'RespondToWHQuestionTask'");
    }

    @Override
    public DialogTaskPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void setTaskSpec(SemanticsModel taskSpec) {
        this.taskSpec = taskSpec;
    }

    @Override
    public SemanticsModel getTaskSpec() {
        return taskSpec;
    }

}
