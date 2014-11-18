package edu.cmu.sv.database;

import edu.cmu.sv.YodaEnvironment;
import edu.cmu.sv.ontology.OntologyRegistry;
import edu.cmu.sv.ontology.Thing;
import edu.cmu.sv.ontology.adjective.Adjective;
import edu.cmu.sv.ontology.misc.WebResource;
import edu.cmu.sv.ontology.noun.Noun;
import edu.cmu.sv.ontology.preposition.Preposition;
import edu.cmu.sv.ontology.quality.TransientQuality;
import edu.cmu.sv.ontology.role.HasURI;
import edu.cmu.sv.ontology.role.InRelationTo;
import edu.cmu.sv.ontology.role.has_quality_subroles.HasQualityRole;
import edu.cmu.sv.utils.StringDistribution;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONObject;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;

import java.util.*;

/**
 * Created by David Cohen on 11/17/14.
 */
public class ReferenceResolution {

    /*
    * return a distribution over URI's that this JSONObject may refer to
    * */
    public static StringDistribution resolveReference(YodaEnvironment yodaEnvironment, JSONObject reference){
        String queryString = yodaEnvironment.db.prefixes + "SELECT ?x0 ?score0 WHERE {\n";
        queryString += referenceResolutionHelper(reference, 0).getKey();
        queryString += "} \nORDER BY DESC(?score0) \nLIMIT 10";


        yodaEnvironment.db.log(queryString);

        System.out.println(queryString);
//        System.exit(0);

        StringDistribution ans = new StringDistribution();
        try {
            TupleQuery query = yodaEnvironment.db.connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            TupleQueryResult result = query.evaluate();

            while (result.hasNext()){
                BindingSet bindings = result.next();
                ans.put(bindings.getValue("x0").stringValue(),
                        Double.parseDouble(bindings.getValue("score0").stringValue()));
            }
            result.close();
        } catch (RepositoryException | QueryEvaluationException | MalformedQueryException e) {
            e.printStackTrace();
        }

        System.out.println("result:\n"+ans);
        return ans;
    }


    /*
    * Return a partial query string and an updated tmpVarIndex for the reference JSONObject
    * tmpVarIndex is used so that temporary variables within the query don't have naming conflicts
    * */
    private static Pair<String, Integer> referenceResolutionHelper(JSONObject reference, Integer tmpVarIndex){
        try {
            int referenceIndex = tmpVarIndex;
            String ans = "{\n";
            String classString = (String) reference.get("class");
            if (classString.equals(WebResource.class.getSimpleName())) {
                ans += "?x" + referenceIndex + " rdfs:label ?tmp" + tmpVarIndex + " . \n" +
                        "base:" + reference.get(HasURI.class.getSimpleName()) + " rdf:value ?tmpV" + tmpVarIndex + " . \n" +
                        "BIND( base:" + StringSimilarity.class.getSimpleName() +
                        "(?tmp" + tmpVarIndex + ", ?tmpV" + tmpVarIndex + ") AS ?score" + referenceIndex + ")\n";
                tmpVarIndex++;
            } else {
                List<String> scoresToAccumulate = new LinkedList<>();
                if (Noun.class.isAssignableFrom(OntologyRegistry.thingNameMap.get(classString))) {
                    ans += "?x" + referenceIndex + " rdf:type base:" + classString + " .\n";
//                    tmpVarIndex++;
                }

                for (Object key : reference.keySet()) {
                    if (key.equals("class"))
                        continue;

                    double center;
                    double slope;
                    Class<? extends TransientQuality> qualityClass;

                    if (HasQualityRole.class.isAssignableFrom(OntologyRegistry.roleNameMap.get((String) key))) {
                        Class<? extends Thing> qualityDegreeClass = OntologyRegistry.thingNameMap.
                                get((String) ((JSONObject) reference.get(key)).get("class"));
                        List<String> entityURIs = new LinkedList<>();
                        entityURIs.add("?x"+referenceIndex);
                        if (Preposition.class.isAssignableFrom(qualityDegreeClass)) {
                            Preposition preposition = (Preposition) qualityDegreeClass.newInstance();
                            center = preposition.getCenter();
                            slope = preposition.getSlope();
                            qualityClass = preposition.getQuality();
                            //recursively resolve the child to this PP, add the child's variable to entityURIs
                            tmpVarIndex ++;
                            scoresToAccumulate.add("?score"+tmpVarIndex);
                            Pair<String, Integer> updates = referenceResolutionHelper(
                                    (JSONObject)((JSONObject)reference.get(key)).get(InRelationTo.class.getSimpleName()),
                                    tmpVarIndex);
                            ans += updates.getKey();
                            tmpVarIndex = updates.getRight();
                            entityURIs.add("?x"+tmpVarIndex);
                        } else if (Adjective.class.isAssignableFrom(qualityDegreeClass)) {
                            Adjective adjective = (Adjective) qualityDegreeClass.newInstance();
                            center = adjective.getCenter();
                            slope = adjective.getSlope();
                            qualityClass = adjective.getQuality();
                        } else {
                            throw new Error("degreeClass is neither an Adjective nor a Preposition class");
                        }
                        scoresToAccumulate.add("?score"+tmpVarIndex);
                        entityURIs.add("?transient_quality"+tmpVarIndex);
                        ans += qualityClass.newInstance().getQualityCalculatorSPARQLQuery().apply(entityURIs) +
                                "BIND(base:LinearFuzzyMap("+center+", "+slope+", ?transient_quality"+tmpVarIndex+") AS ?score"+tmpVarIndex+")\n";
                    } else {
                        throw new Error("this role isn't handled:" + key);
                    }
                    tmpVarIndex++;
                }
                ans += "BIND(base:"+Product.class.getSimpleName()+"(";
                ans += String.join(", ", scoresToAccumulate);
                ans += ") AS ?score"+referenceIndex+")\n";
            }
            return new ImmutablePair<>(ans+"}\n", tmpVarIndex);

        } catch (InstantiationException | IllegalAccessException e){
            e.printStackTrace();
            throw new Error();
        }
    }

}
