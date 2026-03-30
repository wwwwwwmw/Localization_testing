package org.example.core;

/**
 * OciMetrics chua du lieu dau vao va logic tinh OCI.
 */
public class OciMetrics {

    public static final double WL = 0.20;
    public static final double WF = 0.20;
    public static final double WM = 0.15;
    public static final double WR = 0.20;
    public static final double WB = 0.10;
    public static final double WA = 0.15;

    public final double localeTarget;
    public final double localeCovered;
    public final double featureTarget;
    public final double featureCovered;
    public final double methodTarget;
    public final double methodCovered;
    public final double riskWeightTotal;
    public final double riskWeightCovered;
    public final double boundaryTarget;
    public final double boundaryCovered;
    public final double autoTarget;
    public final double autoCovered;

    public OciMetrics(
            double localeTarget,
            double localeCovered,
            double featureTarget,
            double featureCovered,
            double methodTarget,
            double methodCovered,
            double riskWeightTotal,
            double riskWeightCovered,
            double boundaryTarget,
            double boundaryCovered,
            double autoTarget,
            double autoCovered) {
        this.localeTarget = localeTarget;
        this.localeCovered = localeCovered;
        this.featureTarget = featureTarget;
        this.featureCovered = featureCovered;
        this.methodTarget = methodTarget;
        this.methodCovered = methodCovered;
        this.riskWeightTotal = riskWeightTotal;
        this.riskWeightCovered = riskWeightCovered;
        this.boundaryTarget = boundaryTarget;
        this.boundaryCovered = boundaryCovered;
        this.autoTarget = autoTarget;
        this.autoCovered = autoCovered;
    }

    public double l() {
        return ratio(localeCovered, localeTarget);
    }

    public double f() {
        return ratio(featureCovered, featureTarget);
    }

    public double m() {
        return ratio(methodCovered, methodTarget);
    }

    public double r() {
        return ratio(riskWeightCovered, riskWeightTotal);
    }

    public double b() {
        return ratio(boundaryCovered, boundaryTarget);
    }

    public double a() {
        return ratio(autoCovered, autoTarget);
    }

    public double oci() {
        double raw = WL * l() + WF * f() + WM * m() + WR * r() + WB * b() + WA * a();
        return clamp(raw);
    }

    public String rating() {
        double score = oci();
        if (score < 0.70) {
            return "CHUA_DAT";
        }
        if (score < 0.85) {
            return "DAT_CO_BAN";
        }
        if (score < 0.95) {
            return "TOT";
        }
        return "RAT_TOT";
    }

    private static double ratio(double covered, double target) {
        if (target <= 0) {
            return 0.0;
        }
        return clamp(covered / target);
    }

    private static double clamp(double value) {
        if (value < 0.0) {
            return 0.0;
        }
        if (value > 1.0) {
            return 1.0;
        }
        return value;
    }
}
