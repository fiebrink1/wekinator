/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.featureanalysis;

/**
 *
 * @author louismccallum
 */
public class RankedFeatureSelector extends FeatureSelector     
{
    public double threshold = 0.2;
    public int featuresToPick = 10;
    public boolean useThreshold = true;
}
