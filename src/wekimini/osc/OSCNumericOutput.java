/*
 * Information about an output, not the value of the output itself (that is handled by manager)
 */
package wekimini.osc;

import java.util.Random;
import wekimini.LearningModelBuilder;
import wekimini.learning.NeuralNetModelBuilder;
import wekimini.util.Util;

/**
 *
 * @author rebecca
 */
public class OSCNumericOutput implements OSCSupervisedLearningOutput {

    private final String name;

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NUMERIC,NAME=").append(name);
        sb.append(",MIN=").append(min).append(",MAX=").append(max);
        sb.append(",TYPE=").append(outputType);
        sb.append(",LIMIT_TYPE=").append(limitType);
        sb.append(",TRAINING_LIMIT=").append(trainingDataLimitRestriction);
        sb.append(",TRAINING_INT=").append(trainingDataIntegerRestriction);
        return sb.toString();
    }

    public static enum NumericOutputType {

        INTEGER, REAL
    };

    public static enum LimitType {

        HARD, SOFT
    };

    public static enum TrainingDataLimitRestriction {

        ENFORCE_LIMITS, DONT_ENFORCE_LIMITS
    };

    public static enum TrainingDataIntegerRestriction {

        ENFORCE_INTEGER, DONT_ENFORCE_INTEGER
    };

    private final float min;
    private final float max;
    private final NumericOutputType outputType;
    private final LimitType limitType;
    //Issue: need to figure out how to handle outputGroup in XML; most likely don't want to store it explicitly
    private OSCOutputGroup outputGroup = null;

    private final TrainingDataLimitRestriction trainingDataLimitRestriction;
    private final TrainingDataIntegerRestriction trainingDataIntegerRestriction;

    //ID must be unique
    public OSCNumericOutput(String name,
            float min, float max,
            NumericOutputType outputType, LimitType limitType) {

        if (min > max) {
            throw new IllegalArgumentException("Illegal argument: min cannot be greater than max");
        }

        this.name = name;
        this.min = min;
        this.max = max;
        this.outputType = outputType;
        this.limitType = limitType;
        if (this.limitType == limitType.HARD) {
            this.trainingDataLimitRestriction = TrainingDataLimitRestriction.ENFORCE_LIMITS;
        } else {
            this.trainingDataLimitRestriction = TrainingDataLimitRestriction.DONT_ENFORCE_LIMITS;
        }
        this.trainingDataIntegerRestriction = TrainingDataIntegerRestriction.DONT_ENFORCE_INTEGER;
    }

    public OSCNumericOutput(String name,
            float min, float max,
            NumericOutputType outputType, LimitType limitType,
            TrainingDataLimitRestriction trainingDataLimitRestriction,
            TrainingDataIntegerRestriction trainingDataIntegerRestriction) {

        if (min > max) {
            throw new IllegalArgumentException("Illegal argument: min cannot be greater than max");
        }

        this.name = name;
        this.min = min;
        this.max = max;
        this.outputType = outputType;
        this.limitType = limitType;
        this.trainingDataLimitRestriction = trainingDataLimitRestriction;
        this.trainingDataIntegerRestriction = trainingDataIntegerRestriction;
    }

    public String getName() {
        return name;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public NumericOutputType getOutputType() {
        return outputType;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public OSCOutputGroup getOutputGroup() {
        return outputGroup;
    }

    public void setOutputGroup(OSCOutputGroup outputGroup) {
        this.outputGroup = outputGroup;
    }

    /*public static void writeToXMLFile(OSCNumericOutput o, String filename) {
     Util.writeToXMLFile(o, "OSCNumericOutput", OSCNumericOutput.class, filename);
     //TODO: Better exception handling here: Util shouldn't catch all
     }

     public static OSCNumericOutput readFromXMLFile(String filename) throws Exception {
     //TODO: Better exception handling
     return (OSCNumericOutput) Util.readFromXMLFile("OSCNumericOutput", OSCNumericOutput.class, filename);
     }  */
    @Override
    public String toString() {
        return Util.toXMLString(this, "OSCNumericOutput", OSCNumericOutput.class);
    }

    //TODO: test this!
    @Override
    public double generateRandomValue() {
        if (outputType == NumericOutputType.INTEGER) {
            Random r = new Random();
            int i = r.nextInt((int) (max - min) + 1);
            return min + i;
        } else {
            return Math.random() * (max - min) + min;
        }
    }

    @Override
    public double getDefaultValue() {
        return min;
    }

    @Override
    public LearningModelBuilder getDefaultModelBuilder() {
        return new NeuralNetModelBuilder();
    }

    @Override
    public boolean isLegalTrainingValue(double value) {
        //NOTE: Decision to not return false if value outside hard limits, because
        //this could be used in interesting ways during training.
        if (trainingDataLimitRestriction == TrainingDataLimitRestriction.ENFORCE_LIMITS) {
            if (value < min || value > max) {
                return false;
            }
        }
        if (outputType == NumericOutputType.INTEGER
                && trainingDataIntegerRestriction == TrainingDataIntegerRestriction.ENFORCE_INTEGER) {
            if (!Util.isInteger(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double forceLegalTrainingValue(double value) {
        double v = value;
        if (trainingDataLimitRestriction == TrainingDataLimitRestriction.ENFORCE_LIMITS) {
            if (value < min) {
                v = min;
            } else if (value > max) {
                v = max;
            }
        }

        if (outputType == NumericOutputType.INTEGER && trainingDataIntegerRestriction == TrainingDataIntegerRestriction.ENFORCE_INTEGER) {
            return (int) v;
        } else {
            return v;
        }
    }

    @Override
    public boolean isLegalOutputValue(double value) {
        //NOTE: Decision to not return false if value outside hard limits, because
        //this could be used in interesting ways during training.
        if (limitType == LimitType.HARD) {
            if (value < min || value > max) {
                return false;
            }
        }
        if (outputType == NumericOutputType.INTEGER) {
            if (!Util.isInteger(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double forceLegalOutputValue(double value) {
        double v = value;
        if (limitType == LimitType.HARD) {
            if (value < min) {
                v = min;
            } else if (value > max) {
                v = max;
            }
        }

        if (outputType == NumericOutputType.INTEGER) {
            return (int) v;
        } else {
            return v;
        }
    }
}
