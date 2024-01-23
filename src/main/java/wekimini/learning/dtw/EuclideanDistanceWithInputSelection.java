/*
* Euclidean distance with option to ignore certain features
* Implements DTW DistanceFunction
*/
package wekimini.learning.dtw;

import com.util.DistanceFunction;

public class EuclideanDistanceWithInputSelection implements DistanceFunction
{
   public final boolean[] inputMask;
   
   public EuclideanDistanceWithInputSelection(boolean[] inputMask)
   {
      this.inputMask = new boolean[inputMask.length];
      System.arraycopy(inputMask, 0, this.inputMask, 0, inputMask.length);
   }
   
   public double calcDistance(double[] vector1, double[] vector2)
   {
      if (vector1.length != vector2.length || vector1.length != inputMask.length)
         throw new InternalError("ERROR:  cannot calculate the distance "
                                 + "between vectors of different sizes.");

      double sqSum = 0.0;
      for (int x=0; x<vector1.length; x++)
          if (inputMask[x]) {
            sqSum += Math.pow(vector1[x]-vector2[x], 2.0);
          }
      return Math.sqrt(sqSum);
   }
}