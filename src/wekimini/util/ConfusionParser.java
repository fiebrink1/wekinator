/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.util;

/**
 *
 * @author louismccallum
 */
public class ConfusionParser {
    
    public static int[][] parseMatrix(String str)
    {
        String[] lines = str.split("\r\n|\r|\n");
        int numClasses = lines.length - 3;
        
        int[][] arr = new int[numClasses][numClasses];
        
        for(int i = 3; i < lines.length; i++)
        {
            String[] spaced = lines[i].split("\\s+");
            for(int j = 1; j < numClasses + 1; j++)
            {
                arr[i - 3][j - 1] = Integer.parseInt(spaced[j]);
            }
        }
        
        return arr;
    }
    
}
