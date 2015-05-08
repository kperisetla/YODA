package edu.cmu.sv.natural_language_generation.top_level_templates;

import edu.cmu.sv.natural_language_generation.Template;
import edu.cmu.sv.semantics.SemanticsModel;
import edu.cmu.sv.system_action.dialog_act.core_dialog_acts.Acknowledge;
import edu.cmu.sv.utils.Assert;
import edu.cmu.sv.yoda_environment.YodaEnvironment;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Cohen on 10/29/14.
 */
public class AcknowledgeTemplate implements Template {
    @Override
    public Map<String, JSONObject> generateAll(JSONObject constraints, YodaEnvironment yodaEnvironment, int remainingDepth) {
        // ensure that the constraints match this template
        try {
            Assert.verify(constraints.get("dialogAct").equals(Acknowledge.class.getSimpleName()));
            Assert.verify(constraints.keySet().size()==1);
        } catch (Assert.AssertException e){
            return new HashMap<>();
        }

        Map<String, JSONObject> ans = new HashMap<>();
        ans.put("Ok", SemanticsModel.parseJSON(constraints.toJSONString()));
        return ans;
    }
}