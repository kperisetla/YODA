package edu.cmu.sv.domain.ontology;

/**
 * Created by David Cohen on 6/16/15.
 *
 * Adjectives and prepositions are implemented as QualityDegree instances
 *
 */
public class QualityDegree {
    // parameterize the triangle function which maps the quality to this quality degree
    public String name;
    public double center;
    public double slope;
    public Quality quality;

    public QualityDegree(String name, double center, double slope, Quality quality) {
        this.name = name;
        this.center = center;
        this.slope = slope;
        this.quality = quality;
        quality.getQualityDegrees().add(this);
    }

    public double getCenter() {
        return center;
    }

    public double getSlope() {
        return slope;
    }

    public Quality getQuality() {
        return quality;
    }
}
