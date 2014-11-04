package edu.cmu.sv.natural_language_generation.Templates;

import edu.cmu.sv.YodaEnvironment;
import edu.cmu.sv.database.Database;
import edu.cmu.sv.natural_language_generation.GenerationUtils;
import edu.cmu.sv.natural_language_generation.Template;
import edu.cmu.sv.ontology.OntologyRegistry;
import edu.cmu.sv.ontology.ThingWithRoles;
import edu.cmu.sv.ontology.absolute_quality_degree.AbsoluteQualityDegree;
import edu.cmu.sv.ontology.misc.UnknownThingWithRoles;
import edu.cmu.sv.ontology.misc.WebResource;
import edu.cmu.sv.ontology.role.HasURI;
import edu.cmu.sv.ontology.role.has_quality_subroles.HasAbsoluteQualityDegree;
import edu.cmu.sv.semantics.SemanticsModel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Cohen on 11/3/14.
 *
 * Try all adjectives that this individual is in the range for.
 * Generate the + adj + cls if > .5 truth to the description.
 * Generates {cls:UnknownThingWithRoles, X:HasAQD: {cls: Y:aqd}}
 *
 */
public class DefiniteReferenceWithAdjectiveAndClassTemplate0 implements Template {
    static JSONObject applicabilityConstraint;
    static {
        try {
            applicabilityConstraint= (JSONObject) SemanticsModel.parser.
                    parse("{\"class\":\"" + WebResource.class.getSimpleName() + "\"}");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, JSONObject> generateAll(JSONObject constraints, YodaEnvironment yodaEnvironment) {
        if (SemanticsModel.anySenseConflicts(applicabilityConstraint, constraints))
            return new HashMap<>();

        String entityURI = (String) new SemanticsModel(constraints).
                newGetSlotPathFiller(HasURI.class.getSimpleName());
        String queryString = yodaEnvironment.db.prefixes +
                "SELECT ?x WHERE { <"+entityURI+"> rdf:type ?x .}";
        Set<String> classNames = yodaEnvironment.db.runQuerySelectX(queryString);

        // define the chunks that this template composes
        Map<String, JSONObject> detChunks = new HashMap<>();
        Map<String, JSONObject> adjChunks = new HashMap<>();
        Map<String, JSONObject> clsChunks = new HashMap<>();

        // compose det chunks
        detChunks.put("the", new SemanticsModel("{}").getInternalRepresentation());

        // collect adj chunks
        for (String clsName : classNames.stream().map(Database::getLocalName).collect(Collectors.toSet())) {
            for (Pair<Class<? extends HasAbsoluteQualityDegree>, Class<? extends AbsoluteQualityDegree>> adjPair : GenerationUtils.
                    applicableAdjectives((Class <? extends ThingWithRoles>) OntologyRegistry.thingNameMap.get(clsName))){
                double degreeOfMatch = yodaEnvironment.db.
                        evaluateAbsoluteQualityDegree(entityURI, adjPair.getLeft(), adjPair.getRight());
                if (degreeOfMatch > 0.5){
                    Set<String> adjStrings = GenerationUtils.getPOSForClass(adjPair.getRight(), "adjectives");
                    for (String adjString : adjStrings){
                        JSONObject tmp = SemanticsModel.parseJSON("{\"class\":\"" + adjPair.getRight().getSimpleName() + "\"}");
                        SemanticsModel.wrap(tmp, UnknownThingWithRoles.class.getSimpleName(), adjPair.getLeft().getSimpleName());
                        adjChunks.put(adjString, tmp);
                    }

                }
            }
        }
        // include the empty adjective chunk
        adjChunks.put("", SemanticsModel.parseJSON("{\"class\":\""+UnknownThingWithRoles.class.getSimpleName()+"\"}"));

        // collect class name chunks
        for (String clsName : classNames.stream().map(Database::getLocalName).
                collect(Collectors.toList())) {
            if (!OntologyRegistry.thingNameMap.containsKey(clsName))
                continue;
            Set<String> singularNounForms = GenerationUtils.getPOSForClass(
                    OntologyRegistry.thingNameMap.get(clsName), "singularNounForms");
            for (String singularNounForm : singularNounForms) {
                clsChunks.put(singularNounForm, SemanticsModel.parseJSON("{\"class\":\"" + clsName + "\"}"));
            }
        }

        List<Map<String, JSONObject>> chunks = Arrays.asList(detChunks, adjChunks, clsChunks);
        return GenerationUtils.simpleOrderedCombinations(chunks,
                DefiniteReferenceWithAdjectiveAndClassTemplate0::compositionFunction, new HashMap<>());
    }

    private static JSONObject compositionFunction(List<JSONObject> children){
        JSONObject det = children.get(0);
        JSONObject adj = children.get(1);
        JSONObject cls = children.get(2);
        SemanticsModel ans = new SemanticsModel(cls.toJSONString());
        ans.extendAndOverwrite(new SemanticsModel(adj.toJSONString()));
        return ans.getInternalRepresentation();
    }

}