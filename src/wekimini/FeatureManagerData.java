/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini;

import java.util.ArrayList;
import wekimini.modifiers.Feature;
import wekimini.modifiers.FeatureCollection;

/**
 *
 * @author louismccallum
 */
public class FeatureManagerData
    {
        ArrayList<ArrayList<String>> added;
        int bufferSize;
        int windowSize;
        int numOutputs;
        String[] inputNames;
        
        public FeatureManagerData(FeatureManager fm)
        {
            this.added = new ArrayList();
            this.added.clear();
            this.numOutputs = fm.featureCollections.size();
            this.inputNames = fm.inputNames;
            for(FeatureCollection fc : fm.featureCollections)
            {
                ArrayList<String> keys = new ArrayList();
                for(Feature f : fc.getCurrentFeatures())
                {
                    keys.add(f.name);
                }
                added.add(keys);
            }
            this.bufferSize = fm.getFeatureBufferSize();
            this.windowSize = fm.getFeatureWindowSize();
            System.out.println("here");
        }
    }
