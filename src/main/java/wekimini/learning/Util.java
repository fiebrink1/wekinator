package wekimini.learning;

import weka.classifiers.Classifier;
import weka.core.Instance;
import wekimini.WekaException;

public final class Util {
    static double classifyOrThrow(Classifier model, Instance instance) throws WekaException {
        try {
            return model.classifyInstance(instance);
        } catch (Exception e) {
            throw new WekaException(e);
        }
    }
  }
