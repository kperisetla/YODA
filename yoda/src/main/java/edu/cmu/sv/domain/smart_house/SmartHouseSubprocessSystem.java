package edu.cmu.sv.domain.smart_house;

import edu.cmu.sv.domain.DatabaseRegistry;
import edu.cmu.sv.domain.DomainSpec;
import edu.cmu.sv.domain.NonDialogTaskRegistry;
import edu.cmu.sv.domain.yoda_skeleton.YODASkeletonOntologyRegistry;
import edu.cmu.sv.domain.yoda_skeleton.YodaSkeletonLexicon;
import edu.cmu.sv.yoda_environment.SubprocessYodaSystem;

/**
 * Created by David Cohen on 3/4/15.
 */
public class SmartHouseSubprocessSystem extends SubprocessYodaSystem {
    static {
        // skeleton domain
        domainSpecs.add(new DomainSpec(
                "YODA skeleton domain",
                new YodaSkeletonLexicon(),
                new YODASkeletonOntologyRegistry(),
                new NonDialogTaskRegistry(),
                new DatabaseRegistry()));
        // yelp phoenix domain
        domainSpecs.add(new DomainSpec(
                "Smart house domain",
                new SmartHouseLexicon(),
                new SmartHouseOntologyRegistry(),
                new SmartHouseNonDialogTaskRegistry(),
                new SmartHouseDatabaseRegistry()));
    }
}


