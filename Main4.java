package com.alp.webservice;

import safar.basic.morphology.analyzer.factory.MorphologyAnalyzerFactory;
import safar.basic.morphology.analyzer.interfaces.IMorphologyAnalyzer;
import safar.basic.morphology.analyzer.model.MorphologyAnalysis;
import safar.basic.morphology.analyzer.model.NounMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.ParticleMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.VerbMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.WordMorphologyAnalysis;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class Main4 {
	public String testing(String c) throws UnsupportedEncodingException, IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, Exception {

    String rusult="";
    
    boolean corr = false;
BufferedReader in = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out,"UTF-8"));
IMorphologyAnalyzer analyzer = MorphologyAnalyzerFactory.getAlkhalilImplementation();
List<WordMorphologyAnalysis> wordMorphologyAnalysis;
List<MorphologyAnalysis> listOfAnalysis;


    wordMorphologyAnalysis = analyzer.analyze(c);
    for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
        listOfAnalysis = wordAnalysis.getStandardAnalysisList();
        for (MorphologyAnalysis analysis : listOfAnalysis) {
            if (analysis.isNoun()) {
                NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
if (nounAnalysis.getPos().contains("‰ﬂ—…") || nounAnalysis.getPos().contains("Õ«·… «·«÷«›…")){
	rusult="«·ÃÊ«» ’ÕÌÕ";
                    corr = true;
                }else
                {
                    if (nounAnalysis.getType().contains("«”„ ⁄·„")) {
                    	rusult="«·ÃÊ«» ’ÕÌÕ";
                        corr = true;
                       
                    }
                }
            break;
            }
            
        }
    }
            
    if (!corr)
    {
    	rusult="«·ÃÊ«» Œ«ÿ∆";
    }

return rusult;
}
	}
