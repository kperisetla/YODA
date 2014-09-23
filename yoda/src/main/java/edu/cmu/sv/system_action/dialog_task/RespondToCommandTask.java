package edu.cmu.sv.system_action.dialog_task;

import edu.cmu.sv.database.Database;
import edu.cmu.sv.semantics.SemanticsModel;

/**
 * Created by David Cohen on 9/11/14.
 */
public class RespondToCommandTask implements DialogTask {
    private Database db;

    public RespondToCommandTask(Database db) {
        this.db = db;
    }

    private static DialogTaskPreferences preferences = new DialogTaskPreferences(.5,2,4);
    private SemanticsModel taskSpec = null;

    @Override
    public void execute() {
        System.out.println("executing 'RespondToCommandTask'");
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