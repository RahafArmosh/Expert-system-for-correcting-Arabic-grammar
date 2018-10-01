package com.alp.webservice;




import jess.*;

import java.util.*;

import alp.Alp;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.List;

import safar.basic.morphology.analyzer.factory.MorphologyAnalyzerFactory;
import safar.basic.morphology.analyzer.interfaces.IMorphologyAnalyzer;
import safar.basic.morphology.analyzer.model.MorphologyAnalysis;
import safar.basic.morphology.analyzer.model.NounMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.ParticleMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.Suffix;
import safar.basic.morphology.analyzer.model.SuffixItem;
import safar.basic.morphology.analyzer.model.VerbMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.WordMorphologyAnalysis;


public class Main {
	private enum pos {
		PRSV,PSTV, IMPV, SMN,SFN,DMN,DFN,PMN,PFN,PIN,PRO,DM,REL;
	}

	public String testing(String c) throws UnsupportedEncodingException, IOException, FileNotFoundException, ClassNotFoundException, InterruptedException, Exception {
		
//		BufferedReader in = new BufferedReader(new InputStreamReader(System.in,"UTF-8"));
//        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out,"UTF-8"));
        IMorphologyAnalyzer analyzer = MorphologyAnalyzerFactory.getAlkhalilImplementation();
        List<WordMorphologyAnalysis> wordMorphologyAnalysis;
        List<MorphologyAnalysis> listOfAnalysis;
		
		Rete engine = new Rete();
		
		Deftemplate d = new Deftemplate("Word", "A word", engine);
		d.addSlot("name", Funcall.NIL, "STRING");
		d.addSlot("id", Funcall.NIL, "INTEGER");
		d.addSlot("type", Funcall.NIL, "STRING"); // ��� , ��� , ���  
		d.addSlot("tense", Funcall.NIL, "STRING"); // ������� (��� , ����� , ��� ) , ������� (���� , ����� , ���) ,  ������ (�� , ���� ������) .�
		d.addSlot("last", Funcall.NIL, "STRING"); //���� ����� ������ �� ���� ����� ������ ������  / ���� ����� ������ �� ����� �� ���� ����� ���� ���� 
		d.addSlot("pref", Funcall.NIL, "STRING");
		d.addSlot("suff", Funcall.NIL, "STRING");
		d.addSlot("suff2", Funcall.NIL, "STRING");
		
		engine.addDeftemplate(d);
		
		Deftemplate d2 = new Deftemplate("Dep", "A dep", engine);
		d2.addSlot("id_word", Funcall.NIL, "INTEGER"); // ��� ������
		d2.addSlot("id_dep", Funcall.NIL, "INTEGER"); // ��� ������ ������� �����
		d2.addSlot("rel", Funcall.NIL, "STRING"); // ��� ������� 
		d2.addSlot("word", Funcall.NIL, "STRING"); // ������
		
		engine.addDeftemplate(d2);
		
		Deftemplate d3 = new Deftemplate("Err", "A err", engine);
		d3.addSlot("word1", Funcall.NIL, "STRING"); // ��� ������ ������� ������ 
		d3.addSlot("word2", Funcall.NIL, "STRING"); // ��� ������ ������� ������� 
		d3.addSlot("corr1", Funcall.NIL, "STRING"); // �������� ����� 
		d3.addSlot("corr2", Funcall.NIL, "STRING"); // �������� ������
		
		engine.addDeftemplate(d3);
		
		Deftemplate pro = new Deftemplate("proun", "A proun", engine); 

		  pro.addSlot("member", Funcall.NIL, "STRING");
		  pro.addSlot("member1", Funcall.NIL, "STRING");
		  pro.addSlot("m", Funcall.NIL, "STRING"); 
		  engine.addDeftemplate(pro); 
	
		  Fact o = new Fact("proun", engine);

		  o.setSlotValue("member", new Value("� �� �� �� ��� �� ��� �� ��� �� �� ���", RU.STRING)); 
		  o.setSlotValue("member1", new Value("�� ��� ��� ��� ���� ��� ���� ��� ���� ��� ��� ����", RU.STRING)); 
		  
		 // o.setSlotValue("member1", new Value("�� ��� ��� ��� ���� ��� ���� ��� ���� ��� ��� ����", RU.STRING)); 
		  o.setSlotValue("m", new Value("", RU.STRING)); 
		  engine.assertFact(o);
		
		
		// analys phrase ALP 
		Alp alp = new Alp();
		String[] res =  alp.posTag(c).split(" ");
		String[] tok = alp.tokenize(c).split(" ");
		
		String pattern,word,stem,suff,root,pos,tense;
		boolean find = false , test = false;
		int id;
		Suffix sf;
		
		String p = "���   ���   ���    ����    ����    ���   ���   ����   ���   ��    ���     ����    ���";
		String roots_v = "���    ���   ���   ���   ���    ���    ���    ���     ���    ���    ���     ���    ��� ";
		String obj2 = "��� ����  ��� ��� ���� ���� ��� ��� ��� ��� ���  ���   ����  ���  ���  ���  ����  ���  ���  ���  ���  ���  ���  ����  ���";
		String obj2Amr = "��� ";
		String obj3 = "����   ���    ���   ����    ����   ���   ���";
        MaxentTagger tagger =  new MaxentTagger("data\\arabic.tagger");
		
		for (int i=0 ;i < res.length ;i++)
		{
			String result=res[i].split("_")[1];
//			System.out.println(result);
			
			if (result.contains("B-PER"))
			{
				if(i == 0)
				{
					engine.executeCommand("(assert (name of prson found at start))");
				}
				if(i==1)
				{
					engine.executeCommand("(assert (name of prson found at second))");
				}
			}
			
			//region verb
			
			if (result.contains("PSTV")) // ��� ��� �� ��� ��� ���� ������� �� ��� ����
			{
				word = res[i].split("_")[0];
				
				Fact f1 = new Fact("Word", engine);
				
				if (p.contains(word))
				{
               		f1.setSlotValue("name", new Value(word, RU.STRING));
            		f1.setSlotValue("id", new Value(i, RU.INTEGER));
            		f1.setSlotValue("type", new Value("���", RU.STRING));
            		f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
            		f1.setSlotValue("last", new Value(word.charAt(word.length()-1)+"", RU.STRING));
            		f1.setSlotValue("pref", new Value("" , RU.STRING));
            		f1.setSlotValue("suff", new Value("", RU.STRING));
            		engine.assertFact(f1);
        			
				}
				else
				{
					
					wordMorphologyAnalysis = analyzer.analyze(word);
					
					find = false;  test = false;
					
		            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
		            	
		                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
		                
		                for (MorphologyAnalysis analysis : listOfAnalysis) {
		                    if (analysis.isVerb()) {

		                    	VerbMorphologyAnalysis verbAnalysis = (VerbMorphologyAnalysis) analysis;
		                    	
		                    	if (!find && !test)
		                    	{
			                    	find = true;
			                        
			                    	pattern=verbAnalysis.getPattern();
			                    	
			                   		f1.setSlotValue("name", new Value(word, RU.STRING));
			                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
			                		f1.setSlotValue("type", new Value("���", RU.STRING));
			                		sf = verbAnalysis.getSuffix();
			                		suff = sf.getVoweledform().toString();
			                		
			                		
	//		                		root = word;
	//		                		
	//		                		if (suff != "")
	//		                		{
	//		                			if (word.contains(suff))
	//		                			{
	//			                			id = word.lastIndexOf(suff);
	//			                			root = word.substring(0,id);
	//	//		                			System.out.println(root);
	//		                			}
	//		                		}
			                		if (roots_v.contains(verbAnalysis.getRoot()))
			                    	{
			                			test = true;
			                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
			                    	}
			                		else
			                		{
			                			root = verbAnalysis.getRoot();
			                			stem = verbAnalysis.getStem().getUnvoweledForm();
			                			if (stem.charAt(stem.length()-1) == '�')
			                				stem = stem.substring(0, stem.length()-1);
			                			
			                			if (obj3.contains(stem))
			                			{
			                				test = true;
			                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"���� ������", RU.STRING));	
			                			}
		                				else
				                			if (obj2.contains(root) || obj2.contains(stem))
				                			{
				                				test = true;
				                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"�������", RU.STRING));
				                			}
				                			else     		
			                					if (obj2Amr.contains(root) && verbAnalysis.getType().contains("���"))
			                					{
			                						test = true;
			    	                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"�������", RU.STRING));
			                					}
			    	                			else
				                					f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos(), RU.STRING));
			                		}
			                		
			                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
			                		f1.setSlotValue("pref", new Value(verbAnalysis.getPrefix().getVoweledform() , RU.STRING));
			                		
			                		f1.setSlotValue("suff", new Value(sf.getSuffixItemList().get(0).getClazz(), RU.STRING));
			                		if (sf.getSuffixItemList().size() > 1)
			                		{
			                			f1.setSlotValue("suff2", new Value(sf.getSuffixItemList().get(1).getClazz(), RU.STRING));
			                		}
			                		
			                		engine.assertFact(f1);
		                    	}
		                    
		                    	if (test)
		                    	{
		                    		break;
		                    	} else
		                    	{
		                    		if (roots_v.contains(verbAnalysis.getRoot()))
			                    	{
			                			test = true;
			                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
			                    	}
		                    	}
		                    }   
		                }
		                
		                if(!find)
		                {
		                	for (MorphologyAnalysis analysis : listOfAnalysis) {
			                    if (analysis.isNoun()) {
			                    	
			                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
			                    	
			                    	pattern = nounAnalysis.getPattern();
			                    	
			                   		f1.setSlotValue("name", new Value(word, RU.STRING));
			                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
			                		f1.setSlotValue("type", new Value("���", RU.STRING));
			                		f1.setSlotValue("tense", new Value(nounAnalysis.getPos(), RU.STRING));
			                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
			                		f1.setSlotValue("pref", new Value(nounAnalysis.getPrefix().getVoweledform() , RU.STRING));
			                		f1.setSlotValue("suff", new Value(nounAnalysis.getSuffix().getVoweledform(), RU.STRING));
			                		engine.assertFact(f1);
			                        break;
			                    }   
			                }
		                }
		            }
			
				}
			}
			
			
			if (result.contains("PRSV")) // ��� ����� �� ��� ����� ���� �������
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				Fact f1 = new Fact("Word", engine);
				
				find = false;  test = false;
	            
				for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                	
	                    if (analysis.isVerb()) {
	                    	
	                    	VerbMorphologyAnalysis verbAnalysis = (VerbMorphologyAnalysis) analysis;
	                    	
	                    	if (!find && !test)
	                    	{
	                    		find = true;
		                    	pattern=verbAnalysis.getPattern();
		                    	
		                   		f1.setSlotValue("name", new Value(word, RU.STRING));
		                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
		                		f1.setSlotValue("type", new Value("���", RU.STRING));
		                		
		                		if (roots_v.contains(verbAnalysis.getRoot()))
		                    	{
		                			test = true;
		                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
		                    	}
		                		else
		                		{
		                			root = verbAnalysis.getRoot();
		                			stem = verbAnalysis.getStem().getUnvoweledForm();
		                			if (stem.charAt(stem.length()-1) == '�')
		                				stem = stem.substring(0, stem.length()-1);
		                			
		                			if (obj3.contains(stem))
		                			{
		                				test = true;
		                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"���� ������", RU.STRING));	
		                			}
	                				else
			                			if (obj2.contains(root) || obj2.contains(stem))
			                			{
			                				test = true;
			                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"�������", RU.STRING));
			                			}
			                			else     		
		                					if (obj2Amr.contains(root) && verbAnalysis.getType().contains("���"))
		                					{
		                						test = true;
		    	                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"�������", RU.STRING));
		                					}
		    	                			else
			                					f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos(), RU.STRING));
		                		}
		                		
		                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
		                		f1.setSlotValue("pref", new Value(verbAnalysis.getPrefix().getVoweledform() , RU.STRING));
		                		sf = verbAnalysis.getSuffix();
		                		f1.setSlotValue("suff", new Value(sf.getSuffixItemList().get(0).getClazz(), RU.STRING));
		                		if (sf.getSuffixItemList().size() > 1)
		                		{
		                			f1.setSlotValue("suff2", new Value(sf.getSuffixItemList().get(1).getClazz(), RU.STRING));
		                		}
		                		engine.assertFact(f1);
	                    	}
	                    	
	                    	if (test)
	                    	{
	                    		break;
	                    	} else
	                    	{
	                    		if (roots_v.contains(verbAnalysis.getRoot()))
		                    	{
		                			test = true;
		                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
		                    	}
	                    	}
	                    
	                    }
	                }
	            }
			}
			
			if (result.contains("IMPV")) // ��� ���
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
				Fact f1 = new Fact("Word", engine);
				
				find = false;  test = false;
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                	
	                    if (analysis.isVerb()) {

	                    	VerbMorphologyAnalysis verbAnalysis = (VerbMorphologyAnalysis) analysis;
	                    	
	                    	if (!find && !test)
	                    	{
	                    		find = true;
		                    	
		                    	pattern=verbAnalysis.getPattern();
		                    	
		                   		f1.setSlotValue("name", new Value(word, RU.STRING));
		                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
		                		f1.setSlotValue("type", new Value("���", RU.STRING));
	
		                		if (roots_v.contains(verbAnalysis.getRoot()))
		                    	{
		                			test = true;
		                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
		                    	}
		                		else
		                		{
		                			root = verbAnalysis.getRoot();
		                			stem = verbAnalysis.getStem().getUnvoweledForm();
		                			if (stem.charAt(stem.length()-1) == '�')
		                				stem = stem.substring(0, stem.length()-1);
		                			
		                			if (obj3.contains(stem))
		                			{
		                				test = true;
		                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"���� ������", RU.STRING));	
		                			}
	                				else
			                			if (obj2.contains(root) || obj2.contains(stem))
			                			{
			                				test = true;
			                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"�������", RU.STRING));
			                			}
			                			else     		
		                					if (obj2Amr.contains(root) && verbAnalysis.getType().contains("���"))
		                					{
		                						test = true;
		    	                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"�������", RU.STRING));
		                					}
		    	                			else
			                					f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos(), RU.STRING));
		                		}
		                		
		                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
		                		f1.setSlotValue("pref", new Value(verbAnalysis.getPrefix().getVoweledform() , RU.STRING));
		                		sf = verbAnalysis.getSuffix();
		                		f1.setSlotValue("suff", new Value(sf.getSuffixItemList().get(0).getClazz(), RU.STRING));
		                		if (sf.getSuffixItemList().size() > 1)
		                		{
		                			f1.setSlotValue("suff2", new Value(sf.getSuffixItemList().get(1).getClazz(), RU.STRING));
		                		}
		                		engine.assertFact(f1);
	                    	}
	                    
	                    	if (test)
	                    	{
	                    		break;
	                    	} else
	                    	{
	                    		if (roots_v.contains(verbAnalysis.getRoot()))
		                    	{
		                			test = true;
		                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
		                    	}
	                    	}
	                    
	                    }
	                }
	            }
			}
			
			//endregion
			
			//---------------------------------------------------
			
			//region Noun
			if ((result.contains("N")||result.contains("AJ")) && (result.contains("S")|| result.contains("DM") || result.contains("DF"))) // ����� ������ � ������
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
				find  =  false;
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                if (listOfAnalysis.size() == 0)
	                {
	                	Fact f1 = new Fact("Word", engine);
                    	
                   		f1.setSlotValue("name", new Value(word, RU.STRING));
                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
                		f1.setSlotValue("type", new Value("���", RU.STRING));
                		f1.setSlotValue("last", new Value(word.charAt(word.length()-1)+"", RU.STRING));
                		
	                	if (result.contains("S") && result.contains("M"))
	                	{
	                		if (result.contains("D+"))
	                		{
	                			if (result.contains("N"))
	                				tense = "��� ���� ����  �����";
	                			else
	                				tense = "��� ���� ����  �����";
	                			f1.setSlotValue("pref", new Value("��", RU.STRING));
	                		}
	                		else
	                		{
	                			if (result.contains("N"))
	                				tense = "��� ���� ����  ����";
	                			else
	                				tense = "��� ���� ����  ����";
	                			f1.setSlotValue("pref", new Value("", RU.STRING));
	                		}
	                		
	                	} 
	                	else
	                		if (result.contains("S") && result.contains("F"))
		                	{
		                		if (result.contains("D+"))
		                		{
		                			if (result.contains("N"))
		                				tense = "��� ���� ����  �����";
		                			else
		                				tense = "��� ���� ����  �����";
		                			f1.setSlotValue("pref", new Value("��", RU.STRING));
		                		}
		                		else
		                		{
		                			if (result.contains("N"))
		                				tense = "��� ���� ����  ����";
		                			else
		                				tense = "��� ���� ����  ����";
		                			f1.setSlotValue("pref", new Value("", RU.STRING));
		                		}
		                	}
	                		else
	                			if (result.contains("DM"))
	    	                	{
	    	                		if (result.contains("D+"))
	    	                		{
	    	                			if (result.contains("N"))
	    	                				tense = "��� ���� ����  �����";
	    	                			else
	    	                				tense = "��� ���� ����  �����";
	    	                			f1.setSlotValue("pref", new Value("��", RU.STRING));
	    	                		}
	    	                		else
	    	                		{
	    	                			if (result.contains("N"))
	    	                				tense = "��� ���� ����  ����";
	    	                			else
	    	                				tense = "��� ���� ����  ����";
	    	                			f1.setSlotValue("pref", new Value("", RU.STRING));
	    	                		}
	    	                		
	    	                	} 
	    	                	else
	    		                	{
	    		                		if (result.contains("D+"))
	    		                		{
	    		                			if (result.contains("N"))
	    		                				tense = "��� ���� ����  �����";
	    		                			else
	    		                				tense = "��� ���� ����  �����";
	    		                			f1.setSlotValue("pref", new Value("��", RU.STRING));
	    		                		}
	    		                		else
	    		                		{
	    		                			if (result.contains("N"))
	    		                				tense = "��� ���� ����  ����";
	    		                			else
	    		                				tense = "��� ���� ����  ����";
	    		                			f1.setSlotValue("pref", new Value("", RU.STRING));
	    		                		}
	    		                		
	    		                	}
	    	            
	                	if (word.charAt(word.length()-1) == '�' || word.charAt(word.length()-1) == '�')
	    	            	tense += " ����� ";
	                	else
	                		if (word.charAt(word.length()-1) == '�' || word.charAt(word.length()-1) == '�')
		    	            	tense += " ����� ";
	                		else
		                		if (word.charAt(word.length()-1) == '�' || word.charAt(word.length()-1) == '�')
			    	            	tense += " ����� ";
	                	
	                	f1.setSlotValue("tense", new Value(tense, RU.STRING));
	                	f1.setSlotValue("suff", new Value("", RU.STRING));
                		engine.assertFact(f1);
	                }
	                else
	                {
	                	String tagged = tagger.tagString(word);
	                	
	                	if (tagged.split("/")[1].contains("V"))
	                	{
	                		Fact f1 = new Fact("Word", engine);
	                		for (MorphologyAnalysis analysis : listOfAnalysis) {
	                			
			                    if (analysis.isVerb()) {

			                    	VerbMorphologyAnalysis verbAnalysis = (VerbMorphologyAnalysis) analysis;
			                    	
			                    	if (!find && !test)
			                    	{
				                    	find = true;
				                        
				                    	pattern=verbAnalysis.getPattern();
				                    	
				                   		f1.setSlotValue("name", new Value(word, RU.STRING));
				                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
				                		f1.setSlotValue("type", new Value("���", RU.STRING));
				                		sf = verbAnalysis.getSuffix();
				                		suff = sf.getVoweledform().toString();
				                		
				                		
		//		                		root = word;
		//		                		
		//		                		if (suff != "")
		//		                		{
		//		                			if (word.contains(suff))
		//		                			{
		//			                			id = word.lastIndexOf(suff);
		//			                			root = word.substring(0,id);
		//	//		                			System.out.println(root);
		//		                			}
		//		                		}
				                		if (roots_v.contains(verbAnalysis.getRoot()))
				                    	{
				                			test = true;
				                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
				                    	}
				                		else
				                		{
				                			root = verbAnalysis.getRoot();
				                			stem = verbAnalysis.getStem().getUnvoweledForm();
				                			if (stem.charAt(stem.length()-1) == '�')
				                				stem = stem.substring(0, stem.length()-1);
				                			
				                			if (obj3.contains(stem))
				                			{
				                				test = true;
				                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"���� ������", RU.STRING));	
				                			}
			                				else
					                			if (obj2.contains(root) || obj2.contains(stem))
					                			{
					                				test = true;
					                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"�������", RU.STRING));
					                			}
					                			else     		
				                					if (obj2Amr.contains(root) && verbAnalysis.getType().contains("���"))
				                					{
				                						test = true;
				    	                				f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos()+" "+"�������", RU.STRING));
				                					}
				    	                			else
					                					f1.setSlotValue("tense", new Value(verbAnalysis.getType()+" "+verbAnalysis.getPos(), RU.STRING));
				                		}
				                		
				                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
				                		f1.setSlotValue("pref", new Value(verbAnalysis.getPrefix().getVoweledform() , RU.STRING));
				                		
				                		f1.setSlotValue("suff", new Value(sf.getSuffixItemList().get(0).getClazz(), RU.STRING));
				                		if (sf.getSuffixItemList().size() > 1)
				                		{
				                			f1.setSlotValue("suff2", new Value(sf.getSuffixItemList().get(1).getClazz(), RU.STRING));
				                		}
				                		
				                		engine.assertFact(f1);
			                    	}
			                    
			                    	if (test)
			                    	{
			                    		break;
			                    	} else
			                    	{
			                    		if (roots_v.contains(verbAnalysis.getRoot()))
				                    	{
				                			test = true;
				                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
				                    	}
			                    	}
			                    }   
			                }
	                	}
	                	else
			                for (MorphologyAnalysis analysis : listOfAnalysis) {
			                	
			                	
			                    if (analysis.isNoun() && (analysis.getPos()!="#")) {
			                    	
			                    	find = true;
			                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
			                    	
			                    	Fact f1 = new Fact("Word", engine);
			                    	pattern=nounAnalysis.getPattern();
			                    	
			                   		f1.setSlotValue("name", new Value(word, RU.STRING));
			                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
			                		f1.setSlotValue("type", new Value("���", RU.STRING));
			                		pos = nounAnalysis.getPos();
			                		
			                		if (result.contains("AJ"))
			                			pos += " ���" ;
			                		
			                		if (pos.contains("���"))
			                		{
			                			pos = pos.replace("���", "����");
			                		}
			                		
			                		if (pos.contains("����") && result.contains("F"))
			                		{
			                			pos = pos.replace("����", "����");
			                		} else {
			                			
			                			if (pos.contains("����") && result.contains("M"))
				                		{
				                			pos = pos.replace("����", "����");
				                		}
			                		}
			                		f1.setSlotValue("tense", new Value(pos + " " + nounAnalysis.getType(), RU.STRING));
			                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
			                		f1.setSlotValue("pref", new Value(nounAnalysis.getPrefix().getVoweledform() , RU.STRING));
			                		f1.setSlotValue("suff", new Value(nounAnalysis.getSuffix().getVoweledform(), RU.STRING));
			                		engine.assertFact(f1);
			                        break;
			                    }
			                }
			            
		                if (!find)
			            {
		                	 for (MorphologyAnalysis analysis : listOfAnalysis) {
		 	                    if (analysis.isVerb() && (analysis.getPos()!="#")) {
		 	                    	
		 	                    	find = true;
		 	                    	VerbMorphologyAnalysis verbAnalysis = (VerbMorphologyAnalysis) analysis;
		 	                    	
		 	                    	Fact f1 = new Fact("Word", engine);
		 	                    	pattern=verbAnalysis.getPattern();
		 	                    	
		 	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
		 	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
		 	                		f1.setSlotValue("type", new Value("���", RU.STRING));
		 	                		pos = verbAnalysis.getPos();
		 	                			 	                		
		 	                		f1.setSlotValue("tense", new Value(verbAnalysis.getType() + " " + pos, RU.STRING));
		 	                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
		 	                		f1.setSlotValue("pref", new Value(verbAnalysis.getPrefix().getVoweledform() , RU.STRING));
			                		
			                		f1.setSlotValue("suff", new Value(verbAnalysis.getSuffix().getSuffixItemList().get(0).getClazz(), RU.STRING));
			                		if (verbAnalysis.getSuffix().getSuffixItemList().size() > 1)
			                		{
			                			f1.setSlotValue("suff2", new Value(verbAnalysis.getSuffix().getSuffixItemList().get(1).getClazz(), RU.STRING));
			                		}
		 	                		engine.assertFact(f1);
		 	                        break;
		 	                    }
		 	                }
		 	            
			            }
		                
	                }
	            }
	            
			}
			
			if ((result.contains("N")||result.contains("AJ")) && (result.contains("P") && !result.contains("PR") && !result.contains("P+"))) // ����� ��� 
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
				
				for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                find  =  false;
	                
	                if (listOfAnalysis.size() == 0)
					{
	                	find = true;
						Fact f1 = new Fact("Word", engine);
	               		f1.setSlotValue("name", new Value(word, RU.STRING));
	            		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	            		f1.setSlotValue("type", new Value("���", RU.STRING));
	            		if (result.contains("F"))
	            			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
	            		else
	            			if (result.contains("M"))
	                			f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
	            			else
	            				f1.setSlotValue("tense", new Value("���", RU.STRING));
	            		f1.setSlotValue("last", new Value(word.charAt(word.length()-1)+"", RU.STRING));
	            		f1.setSlotValue("pref", new Value("" , RU.STRING));
	            		f1.setSlotValue("suff", new Value("", RU.STRING));
	            		engine.assertFact(f1);
					}
					else
						for (MorphologyAnalysis analysis : listOfAnalysis) {
							
		                    if (analysis.isNoun() && (analysis.getPos().contains("���"))) {
		                    	
		                    	find = true;
		                    	
		                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
		                    	
		                    	Fact f1 = new Fact("Word", engine);
		                    	pattern=nounAnalysis.getPattern();
		                   		f1.setSlotValue("name", new Value(word, RU.STRING));
		                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
		                		f1.setSlotValue("type", new Value("���", RU.STRING));
		                		pos = nounAnalysis.getPos();
		                		
		                		if (result.contains("AJ"))
		                			pos += " ���" ;
		                		
		                		if (pos.contains("����"))
		                		{
		                			pos = pos.replace("����", "���");
		                		}
		                		
		                		if (pos.contains("����") && result.contains("F"))
		                		{
		                			pos = pos.replace("����", "����");
		                		} else {
		                			
		                			if (pos.contains("����") && result.contains("M"))
			                		{
			                			pos = pos.replace("����", "����");
			                		}
		                		}
		                		
		                		f1.setSlotValue("tense", new Value(pos+ " " + nounAnalysis.getType(), RU.STRING));
		                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
		                		f1.setSlotValue("pref", new Value(nounAnalysis.getPrefix().getVoweledform() , RU.STRING));
		                		f1.setSlotValue("suff", new Value(nounAnalysis.getSuffix().getVoweledform(), RU.STRING));
		                		engine.assertFact(f1);
		                        break;
		                    }
		                }
	            
	                if (!find)
		            {
	                	 for (MorphologyAnalysis analysis : listOfAnalysis) {
	 	                    if (analysis.isVerb() && (analysis.getPos()!="#")) {
	 	                    	
	 	                    	VerbMorphologyAnalysis verbAnalysis = (VerbMorphologyAnalysis) analysis;
	 	                    	
	 	                    	Fact f1 = new Fact("Word", engine);
	 	                    	pattern=verbAnalysis.getPattern();
	 	                    	
	 	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	 	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	 	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	 	                		pos = verbAnalysis.getPos();
	 	                			 	                		
	 	                		f1.setSlotValue("tense", new Value(pos + " " + verbAnalysis.getType(), RU.STRING));
	 	                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
	 	                		f1.setSlotValue("pref", new Value(verbAnalysis.getPrefix().getVoweledform() , RU.STRING));
		                		
		                		f1.setSlotValue("suff", new Value(verbAnalysis.getSuffix().getSuffixItemList().get(0).getClazz(), RU.STRING));
		                		if (verbAnalysis.getSuffix().getSuffixItemList().size() > 1)
		                		{
		                			f1.setSlotValue("suff2", new Value(verbAnalysis.getSuffix().getSuffixItemList().get(1).getClazz(), RU.STRING));
		                		}
	 	                		engine.assertFact(f1);
	 	                        break;
	 	                    }
	 	                }
	 	            
		            }
	            
				}
			}
			
			if (result.contains("PER")) // ����� ��� 
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
				for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
					
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                find = false;
	                
	                if ((listOfAnalysis.size() == 1) && listOfAnalysis.get(0).getType().contains("��� ���") ) {
                    	
                    	find = true;
                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) listOfAnalysis.get(0);
                    	
                    	Fact f1 = new Fact("Word", engine);
                    	pattern=nounAnalysis.getPattern();
                   		f1.setSlotValue("name", new Value(word, RU.STRING));
                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
                		f1.setSlotValue("type", new Value("���", RU.STRING));
                		f1.setSlotValue("tense", new Value(nounAnalysis.getPos()+ " " + nounAnalysis.getType(), RU.STRING));
                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
                		f1.setSlotValue("pref", new Value(nounAnalysis.getPrefix().getVoweledform() , RU.STRING));
                		f1.setSlotValue("suff", new Value(nounAnalysis.getSuffix().getVoweledform(), RU.STRING));
                		engine.assertFact(f1);
                        break;
                    }
	                
	                if (listOfAnalysis.size() == 0)
					{
	                	find = true;
						Fact f1 = new Fact("Word", engine);
	               		f1.setSlotValue("name", new Value(word, RU.STRING));
	            		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	            		f1.setSlotValue("type", new Value("���", RU.STRING));
	            		if (word.charAt(word.length()-1) == '�' || word.charAt(word.length()-1) == '�' )
	            			f1.setSlotValue("tense", new Value("���� �����", RU.STRING));
	            		else
	            			if (word.charAt(word.length()-1) == '�' || word.charAt(word.length()-1) == '�' )
		            			f1.setSlotValue("tense", new Value("���� �����", RU.STRING));
	            			else
	            				if (word.charAt(word.length()-1) == '�' || word.charAt(word.length()-1) == '�' )
	    	            			f1.setSlotValue("tense", new Value("���� �����", RU.STRING));
	            		f1.setSlotValue("last", new Value(word.charAt(word.length()-1)+"", RU.STRING));
	            		f1.setSlotValue("pref", new Value("" , RU.STRING));
	            		f1.setSlotValue("suff", new Value("", RU.STRING));
	            		engine.assertFact(f1);
					}
					else
						for (MorphologyAnalysis analysis : listOfAnalysis) {
							
		                    if (analysis.isNoun() && (analysis.getPos().contains("���") || ((analysis.getType().contains("���") || analysis.getType().contains("���")) && analysis.getPos() != "#"))) {
		                    	
		                    	find = true;
		                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
		                    	
		                    	Fact f1 = new Fact("Word", engine);
		                    	pattern=nounAnalysis.getPattern();
		                   		f1.setSlotValue("name", new Value(word, RU.STRING));
		                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
		                		f1.setSlotValue("type", new Value("���", RU.STRING));
		                		f1.setSlotValue("tense", new Value(nounAnalysis.getPos()+ " " + nounAnalysis.getType(), RU.STRING));
		                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
		                		f1.setSlotValue("pref", new Value(nounAnalysis.getPrefix().getVoweledform() , RU.STRING));
		                		f1.setSlotValue("suff", new Value(nounAnalysis.getSuffix().getVoweledform(), RU.STRING));
		                		engine.assertFact(f1);
		                        break;
		                    }
		                }
	            
	                if (!find)
		            {
	                	 for (MorphologyAnalysis analysis : listOfAnalysis) {
	 	                    if (analysis.isVerb() && (analysis.getPos()!="#")) {
	 	                    	
	 	                    	VerbMorphologyAnalysis verbAnalysis = (VerbMorphologyAnalysis) analysis;
	 	                    	
	 	                    	Fact f1 = new Fact("Word", engine);
	 	                    	pattern=verbAnalysis.getPattern();
	 	                    	
	 	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	 	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	 	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	 	                		pos = verbAnalysis.getPos();
	 	                			 	                		
	 	                		f1.setSlotValue("tense", new Value(pos + " " + verbAnalysis.getType(), RU.STRING));
	 	                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
	 	                		f1.setSlotValue("pref", new Value(verbAnalysis.getPrefix().getVoweledform() , RU.STRING));
		                		
		                		f1.setSlotValue("suff", new Value(verbAnalysis.getSuffix().getSuffixItemList().get(0).getClazz(), RU.STRING));
		                		if (verbAnalysis.getSuffix().getSuffixItemList().size() > 1)
		                		{
		                			f1.setSlotValue("suff2", new Value(verbAnalysis.getSuffix().getSuffixItemList().get(1).getClazz(), RU.STRING));
		                		}
	 	                		engine.assertFact(f1);
	 	                        break;
	 	                    }
	 	                }
	 	            
		            }
	            
				}
			}
			
			
			//endregion	
			
			//---------------------------------------------------
			
			//region Prepositions
			
			if (result.contentEquals("P") || result.contains("P+")) // ��� ��
			{
				word = res[i].split("_")[0];
								
				wordMorphologyAnalysis = analyzer.analyze(word);
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                find = false;
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isParticle()) {
	                    	find = true;
	                    	ParticleMorphologyAnalysis particleAnalysis = (ParticleMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(particleAnalysis.getType(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value(particleAnalysis.getPrefix().getVoweledform() , RU.STRING));
	                		f1.setSlotValue("suff", new Value(particleAnalysis.getSuffix().getSuffixItemList().get(0).getEntry(), RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	                
	                if (!find)
	                	for (MorphologyAnalysis analysis : listOfAnalysis) {
		                	if (analysis.isNoun() && (analysis.getPos()!="#")) {
		                    	
		                    	find = true;
		                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
		                    	
		                    	Fact f1 = new Fact("Word", engine);
		                    	pattern=nounAnalysis.getPattern();
		                    	
		                   		f1.setSlotValue("name", new Value(word, RU.STRING));
		                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
		                		f1.setSlotValue("type", new Value("���", RU.STRING));
		                		pos = nounAnalysis.getPos();
		                		
		                		if (result.contains("AJ"))
		                			pos += " ���" ;
		                		
	//	                		if (pos.contains("���"))
	//	                		{
	//	                			pos = pos.replace("���", "����");
	//	                		}
		                		
		                		if (pos.contains("����") && result.contains("F"))
		                		{
		                			pos = pos.replace("����", "����");
		                		} else {
		                			
		                			if (pos.contains("����") && result.contains("M"))
			                		{
			                			pos = pos.replace("����", "����");
			                		}
		                		}
		                		f1.setSlotValue("tense", new Value(pos + " " + nounAnalysis.getType(), RU.STRING));
		                		f1.setSlotValue("last", new Value(pattern.charAt(pattern.length()-1)+"", RU.STRING));
		                		f1.setSlotValue("pref", new Value(nounAnalysis.getPrefix().getVoweledform() , RU.STRING));
		                		f1.setSlotValue("suff", new Value(nounAnalysis.getSuffix().getVoweledform(), RU.STRING));
		                		engine.assertFact(f1);
		                        break;
		                    }
	                	}
	            }
			}			

			//endregion
			
			//---------------------------------------------------
			
			//region Conjunctions
			
			if (result.contentEquals("C") || result.contentEquals("RET")) // ��� ��� 
			{
				word = res[i].split("_")[0];
				
				Fact f1 = new Fact("Word", engine);
            	
           		f1.setSlotValue("name", new Value(word, RU.STRING));
        		f1.setSlotValue("id", new Value(i, RU.INTEGER));
        		f1.setSlotValue("type", new Value("���", RU.STRING));
        		f1.setSlotValue("tense", new Value("���", RU.STRING));
        		f1.setSlotValue("last", new Value("", RU.STRING));
        		f1.setSlotValue("pref", new Value("" , RU.STRING));
        		f1.setSlotValue("suff", new Value("", RU.STRING));
        		engine.assertFact(f1);
        		
			}
			
			
			//endregion
			
			//---------------------------------------------------
			
			//region accusative_particles
			
			if (result.contains("ACC")) // ��� ���� ������ 
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
//				if (wordMorphologyAnalysis.size()==0)
//				{
//					suff = "";
//					if (tok[i].contains("+"))
//						suff = tok[i].split("+")[1];
//					Fact f1 = new Fact("Word", engine);
//                	
//               		f1.setSlotValue("name", new Value(word, RU.STRING));
//            		f1.setSlotValue("id", new Value(i, RU.INTEGER));
//            		f1.setSlotValue("type", new Value("���", RU.STRING));
//            		f1.setSlotValue("tense", new Value("���� ������", RU.STRING));
//            		f1.setSlotValue("last", new Value("", RU.STRING));
//            		f1.setSlotValue("pref", new Value("" , RU.STRING));
//            		f1.setSlotValue("suff", new Value(suff, RU.STRING));
//            		engine.assertFact(f1);
//				}
//				else

				for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isParticle()) {
	                    	
	                    	ParticleMorphologyAnalysis particleAnalysis = (ParticleMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(particleAnalysis.getType(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value("" , RU.STRING));
	                		f1.setSlotValue("suff", new Value(particleAnalysis.getSuffix().getSuffixItemList().get(0).getEntry(), RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	            }
                
			}
			
			//endregion
		
			//---------------------------------------------------
			
			//region subordinate_particles
			
			if (result.contains("SUB")) // ������ ������� 
			{
				word = res[i].split("_")[0];
				
				Fact f1 = new Fact("Word", engine);
            	
           		f1.setSlotValue("name", new Value(word, RU.STRING));
        		f1.setSlotValue("id", new Value(i, RU.INTEGER));
        		f1.setSlotValue("type", new Value("���", RU.STRING));
        		f1.setSlotValue("tense", new Value("�����", RU.STRING));
        		f1.setSlotValue("last", new Value("", RU.STRING));
        		f1.setSlotValue("pref", new Value("" , RU.STRING));
        		f1.setSlotValue("suff", new Value("", RU.STRING));
        		engine.assertFact(f1);
                
			}
			
			//endregion

			//---------------------------------------------------
			
			//region Pronouns
		
			if (result.contentEquals("PRO")) // ������� ��������(�������,������,�������)�
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isParticle() && analysis.getType().contains("������")) {
	                    	
	                    	ParticleMorphologyAnalysis particleAnalysis = (ParticleMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(analysis.getType(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value("" , RU.STRING));
	                		f1.setSlotValue("suff", new Value("", RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	            }
                
			}
			
			//endregion

			//---------------------------------------------------
			
			
			//region Demonstrative_pronouns
			
			if (result.contains("DM")) // ����� �������
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isNoun() && analysis.getType().contains("��� �����")) {
	                    	
	                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(nounAnalysis.getType(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value("" , RU.STRING));
	                		f1.setSlotValue("suff", new Value("", RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	            }
                
			}
			
			
			//endregion

			//---------------------------------------------------

			
			//region Relative_pronouns
			
			if (result.contentEquals("REL")) // ����� ��������
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isNoun() && analysis.getType().contains("���")) {
	                    	
	                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(nounAnalysis.getType()+" "+nounAnalysis.getPos(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value("" , RU.STRING));
	                		f1.setSlotValue("suff", new Value("", RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	            }
                
			}
			
			
			//endregion

			//---------------------------------------------------

			//region Temporal_adverb
			
			if (result.contentEquals("T")) // ��� ����  
			{
				word = res[i].split("_")[0];
				
				Fact f1 = new Fact("Word", engine);
            	
           		f1.setSlotValue("name", new Value(word, RU.STRING));
        		f1.setSlotValue("id", new Value(i, RU.INTEGER));
        		f1.setSlotValue("type", new Value("���", RU.STRING));
        		f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
        		f1.setSlotValue("last", new Value("", RU.STRING));
        		f1.setSlotValue("pref", new Value("" , RU.STRING));
        		f1.setSlotValue("suff", new Value("", RU.STRING));
        		engine.assertFact(f1);
        		
			}
			
			
			//endregion
			
			//---------------------------------------------------
			
			//region Location_adverb
			
			if (result.contentEquals("LC")) //  ��� ���� 
			{
				word = res[i].split("_")[0];
				
				Fact f1 = new Fact("Word", engine);
            	
           		f1.setSlotValue("name", new Value(word, RU.STRING));
        		f1.setSlotValue("id", new Value(i, RU.INTEGER));
        		f1.setSlotValue("type", new Value("���", RU.STRING));
        		f1.setSlotValue("tense", new Value("��� ����", RU.STRING));
        		f1.setSlotValue("last", new Value("", RU.STRING));
        		f1.setSlotValue("pref", new Value("" , RU.STRING));
        		f1.setSlotValue("suff", new Value("", RU.STRING));
        		engine.assertFact(f1);
        		
			}
			
			
			//endregion
			
			//---------------------------------------------------
			
			//region Negation_particles
			
			if (result.contains("NEG") || result.contentEquals("PR")) //  ���� ����� � ����� 
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isParticle()) {
	                    	
	                    	ParticleMorphologyAnalysis particleAnalysis = (ParticleMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(analysis.getType(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value("" , RU.STRING));
	                		f1.setSlotValue("suff", new Value("", RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	            }
        		
			}
			
			
			//endregion
			
			//---------------------------------------------------
			
			//region Restriction_particle
			
			if (result.contentEquals("RES")) //  ���� ��� 
			{
				word = res[i].split("_")[0];
				
				Fact f1 = new Fact("Word", engine);
            	
           		f1.setSlotValue("name", new Value(word, RU.STRING));
        		f1.setSlotValue("id", new Value(i, RU.INTEGER));
        		f1.setSlotValue("type", new Value("���", RU.STRING));
        		f1.setSlotValue("tense", new Value("���� ���", RU.STRING));
        		f1.setSlotValue("last", new Value("", RU.STRING));
        		f1.setSlotValue("pref", new Value("" , RU.STRING));
        		f1.setSlotValue("suff", new Value("", RU.STRING));
        		engine.assertFact(f1);        		
			}
			
			
			//endregion
			
			//---------------------------------------------------
		
			//region Surprise_particle
			
			if (result.contentEquals("SUR")) //��� / ��� 
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isParticle()) {
	                    	
	                    	ParticleMorphologyAnalysis particleAnalysis = (ParticleMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(analysis.getType(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value("" , RU.STRING));
	                		f1.setSlotValue("suff", new Value("", RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	            }
        		
			}
			
			
			//endregion
			
			//---------------------------------------------------
			
			//region Interrogative_particles
			
			if (result.contentEquals("Q")) // ��� / ��� / ����
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isNoun() && !analysis.getType().contains("��� ���")) {
	                    	
	                    	NounMorphologyAnalysis nounAnalysis = (NounMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(nounAnalysis.getType(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value("" , RU.STRING));
	                		f1.setSlotValue("suff", new Value("", RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	            }
        		
			}
			
			
			//endregion
			
			//---------------------------------------------------
			
			//region Conditional_particles
			
			if (result.contentEquals("COND")) // �� / ��� / ��
			{
				word = res[i].split("_")[0];
				
				wordMorphologyAnalysis = analyzer.analyze(word);
				
	            for (WordMorphologyAnalysis wordAnalysis : wordMorphologyAnalysis) {
	            	
	                listOfAnalysis = wordAnalysis.getStandardAnalysisList();
	                
	                for (MorphologyAnalysis analysis : listOfAnalysis) {
	                    if (analysis.isParticle()) {
	                    	
	                    	ParticleMorphologyAnalysis particleAnalysis = (ParticleMorphologyAnalysis) analysis;
	                    	
	                    	Fact f1 = new Fact("Word", engine);
	                    	
	                   		f1.setSlotValue("name", new Value(word, RU.STRING));
	                		f1.setSlotValue("id", new Value(i, RU.INTEGER));
	                		f1.setSlotValue("type", new Value("���", RU.STRING));
	                		f1.setSlotValue("tense", new Value(particleAnalysis.getType(), RU.STRING));
	                		f1.setSlotValue("last", new Value("", RU.STRING));
	                		f1.setSlotValue("pref", new Value("" , RU.STRING));
	                		f1.setSlotValue("suff", new Value("", RU.STRING));
	                		engine.assertFact(f1);
	                        break;
	                    }
	                }
	            }
        		
			}
			
			
			//endregion
			
			//---------------------------------------------------
			
		}
		
		//---------------------------------------------------
		
		//region Variables
		Value t = new Value("���", RU.STRING);
		Value t1 = new Value("���", RU.STRING);
		Value t2 = new Value("���", RU.STRING);
		Value t3 = new Value("��� ��� ���� �������", RU.STRING);
		Value t4 = new Value("", RU.STRING);
		Value t5 = new Value("����", RU.STRING);
		Value t5_2 = new Value("���� �����", RU.STRING);
		Value t6 = new Value("��� ��� ���� �������", RU.STRING);
		Value t7 = new Value("���� ����", RU.STRING);
		Value t8 = new Value("����� ��", RU.STRING);
		Value t9 = new Value(" ��� ������� �������", RU.STRING);
		Value t10 = new Value(" ���� �������", RU.STRING);
		Value t11 = new Value(" ���� ������", RU.STRING);
		Value t11_2 = new Value(" ���� ��������", RU.STRING);
		Value t12 = new Value(" ���� �������", RU.STRING);
		Value t13 = new Value(" ���� ���������", RU.STRING);
		Value t14 = new Value("��� ����� ���� �������", RU.STRING);
		Value t15 = new Value("��� ����� ���� �������", RU.STRING);			
		Value t16 = new Value(" ��� ������� + ��� �������", RU.STRING);
		Value t17 = new Value("��� �������", RU.STRING);
		Value t18 = new Value(" ��� �������", RU.STRING);
		Value t19 = new Value(" ��� �������", RU.STRING);
		Value t20 = new Value(" ��� ������", RU.STRING);
		Value t21 = new Value(" ��� ���������", RU.STRING);
		Value t22 = new Value(" ��� �������� + � ����� �����", RU.STRING);
		Value t23 = new Value(" ��� ��������", RU.STRING);
		Value t24 = new Value(" ��� ������", RU.STRING);
		Value t25 = new Value(" ��� ������ + � ����� �����", RU.STRING);
		Value t26 = new Value(" ��� ������� + � ����� �����", RU.STRING);
		Value t27 = new Value("��� ���", RU.STRING);
		Value t28 = new Value("����", RU.STRING);
		
		Value dm = new Value(" �����", RU.STRING);
		Value dmm = new Value("�", RU.STRING);
		Value fth = new Value(" �����", RU.STRING);
		Value ks = new Value(" �����", RU.STRING);
		
		Value b = new Value("�", RU.STRING);
		Value l = new Value("��", RU.STRING);
		Value la = new Value("��", RU.STRING);
		Value lam = new Value("�", RU.STRING);
		Value fa = new Value("�", RU.STRING);
		Value k = new Value("�", RU.STRING);
		Value tt = new Value("�", RU.STRING);
		Value y = new Value("�", RU.STRING);		
		Value jr = new Value("��", RU.STRING);
		Value m2 = new Value("�������", RU.STRING);
		Value m3 = new Value("���� ������", RU.STRING);
		Value m = new Value("���� �������", RU.STRING);
		Value mj = new Value("���� �������", RU.STRING);
		Value h = new Value("�", RU.STRING);
		Value el = new Value("���� ���", RU.STRING);
		Value jm = new Value("����", RU.STRING);
		Value mjz = new Value("�����", RU.STRING);
		Value mdar = new Value("�����", RU.STRING);
		Value mady = new Value("���", RU.STRING);
		Value nafi = new Value("���", RU.STRING);
		Value nasb = new Value("����", RU.STRING);
		Value naasb = new Value("�����", RU.STRING);
		Value yansb = new Value("�����", RU.STRING);
		Value nsb = new Value("���", RU.STRING);
		Value nk = new Value("��� ����", RU.STRING);
		Value name_nk = new Value("��� ��� ����", RU.STRING);
		Value obj_nk = new Value("��� ��� ����", RU.STRING);
		Value nak = new Value("����", RU.STRING);
		
		Value male = new Value(" ����", RU.STRING);
		Value female = new Value(" ����", RU.STRING);
		Value plu = new Value("���", RU.STRING);
		Value du = new Value("����", RU.STRING);
		Value sin = new Value("����", RU.STRING);
		
		Value jazmch1 = new Value("��", RU.STRING);
		Value jazmch2 = new Value("��", RU.STRING);
		Value jazmch3 = new Value("��", RU.STRING);
		Value jazmch4 = new Value("���", RU.STRING);
		Value jazmch5 = new Value("�����", RU.STRING);
		Value jazmch6 = new Value("����", RU.STRING);
		Value jazmch7 = new Value("�����", RU.STRING);
		Value jazmch8 = new Value("�����", RU.STRING);
		Value jazmch9 = new Value("����", RU.STRING);
		
		//-----------------------------------------------------------------------------------------
		
		String s;
		String tz="������";
		Value z=new Value("��", RU.STRING);
		Value z1=new Value("���", RU.STRING);
		Value z2=new Value("", RU.STRING);
		Value z3=new Value("����� �����", RU.STRING);
		Value z4=new Value("��� �����", RU.STRING);
		Value z5=new Value("�", RU.STRING);
		Value z6=new Value("���� ����", RU.STRING);
		Value z7=new Value("������", RU.STRING);
		Value l8=new Value("���", RU.STRING);
		Value l9=new Value("#", RU.STRING);
		Value l10=new Value("��� ��� �����", RU.STRING);
		Value l11=new Value("���", RU.STRING);
		Value l12=new Value("��� ���� �����", RU.STRING);
		Value l13=new Value("��� ��", RU.STRING);
		Value l14=new Value("��� �����", RU.STRING);
		Value l15=new Value("�� ��������", RU.STRING);
		Value z16=new Value("��� ����", RU.STRING);
		Value z17=new Value("���� ����", RU.STRING);
		Value z18=new Value("���", RU.STRING);
		Value z19=new Value("��� �����", RU.STRING);
		Value z20=new Value("��� ���� ���� ������", RU.STRING);
		Value z21=new Value("��� ����� ������ ������ �����", RU.STRING);
		Value z22=new Value("����� ��� �� ���� ���� ", RU.STRING);
		Value z23=new Value("����� ��� �� ���� ���� ", RU.STRING);
		Value z24=new Value("����� ��� �� ���� ���� ", RU.STRING);
		Value z25=new Value("����� ��� �� ���� ��� ", RU.STRING);
		Value z26=new Value("����� ��� �� ���� ���� ", RU.STRING);
		Value z27=new Value("�", RU.STRING);
		Value z28=new Value("���", RU.STRING);
		Value z29=new Value("��� �����", RU.STRING);
		Value z30=new Value("��� ����� ��� �� ���� ������ �� ��  ��� ������� � �����", RU.STRING);
		Value z31=new Value("��� ����� ��� �� ���� ������ �� ��  ��� ������� � �����", RU.STRING);
		Value z32=new Value("��� ����� ��� �� ���� ������ �� ��  ��� ������� ", RU.STRING);
//		Value z33=new Value("��� ����� ��� �� ���� ������ �� ��  ��� ����� ", RU.STRING);
		Value z34=new Value("��� ����� ����� � ������� �� ������� � ������� ", RU.STRING);
		Value z35=new Value("��� ����� ����� � ������� �� ������� � ������� � ����� ", RU.STRING);
		Value z36=new Value("��� ����� ����� � ������� �� ������� � ������� ", RU.STRING);
		Value z37=new Value("����� ��� �� ���� ����", RU.STRING);
		Value z38=new Value("���", RU.STRING);
		Value z39=new Value("���� ���� ���� ��� ������� ���", RU.STRING);
		Value z40=new Value("��� ��� ����� �� ����� ������", RU.STRING);
		Value z41=new Value("��� ����� ������� � ������� ���� �� ����� �������", RU.STRING);
		Value z42=new Value("����� ���� �����", RU.STRING);
		Value z43=new Value("��� �����", RU.STRING);
		Value z44=new Value("����� �����  �����", RU.STRING);
		Value z45=new Value("��� �� ����", RU.STRING);
		Value z46=new Value("���", RU.STRING);
		Value proun=new Value("� �� �� ��� �� ��� � ��� �� �� ���", RU.STRING);
		//endregion
		
		String rule = "";
//		String rule = "(defrule sentence_type" +
//				"(Word (id 0) (type "+t+"))" +
//				"=>" +
//				"(assert (Type "+t+")))\n";
		
//		rule += "(defrule print_type" +
//		         "(declare (salience -2))"+
//				"(Type ?t)" +
//				"=>" +
//				"(printout t ?t crlf))\n";
		
		//region Rules
		
		//region Sub
		
		//region Sub_Damir
		
		//��� ��� ���� ������� + ���� ���� 
		rule += "(defrule Subject1" +
				"(declare (salience -2))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(?te contains "+t3+")) (suff ?s &:(or (eq ?s "+t17+") (eq ?s "+t18+") (eq ?s "+t19+") (eq ?s "+t20+") (eq ?s "+t21+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+t5+")(word ?s)) ))\n";
		
		//��� ��� ���� ������� + ���� ���� ���� 
		rule += "(defrule Subject2" +
				"(declare (salience -2))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(?te contains "+t6+")) (suff ?s &:(or (eq ?s "+t17+") (eq ?s "+t18+") (eq ?s "+t19+") (eq ?s "+t20+") (eq ?s "+t21+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+t7+")(word ?s)) ))\n";
		
		//---------------------------------------------------------------------------
		
		//��� ����� ���� ������� + ���� ���� 
		rule += "(defrule Subject3" +
				"(declare (salience -2))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(?te contains "+t14+")) (suff ?s &:(or (eq ?s "+t17+") (eq ?s "+t19+") (eq ?s "+t20+") (eq ?s "+t21+") (eq ?s "+t22+") (eq ?s "+t23+") (eq ?s "+t24+") (eq ?s "+t25+") (eq ?s "+t26+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+t5+")(word ?s)) ))\n";
		
		//��� ����� ���� ������� + ���� ���� ���� 
		rule += "(defrule Subject4" +
				"(declare (salience -2))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(?te contains "+t15+")) (suff ?s &:(or (eq ?s "+t19+") (eq ?s "+t20+") (eq ?s "+t21+") (eq ?s "+t22+") (eq ?s "+t23+") (eq ?s "+t24+") (eq ?s "+t25+") (eq ?s "+t26+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+t7+")(word ?s)) ))\n";
		
		//----------------------------------------------------------------------------
		
		//��� ��� + ���� ���� 
		rule += "(defrule Subject5" +
				"(declare (salience -2))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(?te contains "+t27+")) (suff ?s &:(or (eq ?s "+t19+") (eq ?s "+t20+") (eq ?s "+t23+") (eq ?s "+t24+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+t5+")(word ?s)) ))\n";
		
		//endregion
		
		//----------------------------------------------------------------------------
		
		//region Sub_Name
		
		//���� ��� ����
		rule += "(defrule Subject6" +
				"(declare (salience -3))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(and (not (?te contains "+t27+")) (?te contains "+m+") ) ) )"+
				"(not (Dep (id_dep ?i) (rel ?re &:(?re contains "+t5+"))) )"+
				"(Word (name ?n2) (id ?i2 &:( > ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+dm+")))"+
				"(not (Word (id ?i3 &:(and (< ?i3 ?i2) (< ?i ?i3))) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
				//"(not (Word (id ?i4 &:(< ?i4 ?i2)) (type "+t2+") (tense ?tm &:(?tm contains "+fth+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t5+")(word ?n2)) ))\n";
		
		//----------------------------------------------------------------------------
		
//		rule += "(defrule Subject7" +
//				"(declare (salience -3))"+
//				"(Word (id ?i) (type "+t+") (tense ?te &:(and (not (?te contains "+t27+")) (?te contains "+m+") ) ) )"+
//				"(not (Dep (id_dep ?i) (rel ?re &:(?re contains "+t5+"))) )"+
//				"(Word (name ?n2) (id ?i2 &:( > ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+dm+")) (suff ?sf &:(and (neq ?sf nil) (?sf contains "+h+"))))"+
//				"(not (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
//				"(Word (id ?i4 &:(< ?i4 ?i2)) (type "+t2+") (tense ?tm &:(?tm contains "+fth+")))"+
//				"=>" +
//				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t5+")(word ?n2)) ))\n";
//		
//		rule += "(defrule Subject7_2" +
//				"(declare (salience -3))"+
//				"(Word (id ?i) (type "+t+") (tense ?te &:(and (not (?te contains "+t27+")) (?te contains "+m+") ) ) )"+
//				"(not (Dep (id_dep ?i) (rel ?re &:(?re contains "+t5+"))) )"+
//				"(Word (tense ?ne &:(?ne contains "+el+")) (id ?e))"+
//				"(Word (name ?n2) (id ?i2 &:(and ( > ?i2 ?i) ( > ?i2 ?e))) (type "+t2+") (tense ?t &:(?t contains "+dm+")))"+
//				"(not (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
//				"(Word (id ?i4 &:(< ?i4 ?i2)) (type "+t2+") (tense ?tm &:(?tm contains "+fth+")))"+
//				"=>" +
//				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t5+")(word ?n2)) ))\n";
//		
//		rule += "(defrule Subject8" +
//				"(declare (salience -4))"+
//				"(Word (id ?i) (type "+t+") (tense ?te &:(and (not (?te contains "+t27+")) (?te contains "+m+") ) ) )"+
//				"(not (Dep (id_dep ?i) (rel ?re &:(?re contains "+t5+"))) )"+
//				"(Word (name ?n2) (id ?i2 &:( > ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+dm+")) (suff2 ?sf &:(and (neq ?sf nil) (?sf contains "+h+"))))"+
//				"(not (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
//				"(Word (id ?i4 &:(< ?i4 ?i2)) (type "+t2+") (tense ?tm &:(?tm contains "+fth+")))"+
//				"=>" +
//				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t5+")(word ?n2)) ))\n";
		
		
		//���� ���� ��� ����
		rule += "(defrule Subject9" +
				"(declare (salience -3))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(and (not (?te contains "+t27+")) (?te contains "+t28+") (?te contains "+mj+") ) ) )"+
				"(Word (name ?n2) (id ?i2 &:( > ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+dm+")))"+
				"(not (Word (id ?i3 &:(and (< ?i3 ?i2) (< ?i ?i3))) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
				//"(not (Word (id ?i4 &:(< ?i4 ?i2)) (type "+t2+") (tense ?tm &:(?tm contains "+fth+"))) )"+
				"(not (Dep (id_dep ?i) (rel "+t7+")) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t7+")(word ?n2)) ))\n";
		
		//----------------------------------------------------------------------------
		
//		rule += "(defrule Subject10" +
//				"(declare (salience -3))"+
//				"(Word (id ?i) (type "+t+") (tense ?te &:(and (not (?te contains "+t27+")) (?te contains "+mj+") ) ) )"+
//				"(not (Dep (id_dep ?i) (rel ?re &:(?re contains "+t7+"))) )"+
//				"(Word (name ?n2) (id ?i2 &:( > ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+dm+")) (suff ?sf &:(and (neq ?sf nil) (?sf contains "+h+"))))"+
//				"(not (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
//				"(Word (id ?i4 &:(< ?i4 ?i2)) (type "+t2+") (tense ?tm &:(?tm contains "+fth+")))"+
//				"=>" +
//				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t7+")(word ?n2)) ))\n";
//		
//		rule += "(defrule Subject10_2" +
//				"(declare (salience -3))"+
//				"(Word (id ?i) (type "+t+") (tense ?te &:(and (not (?te contains "+t27+")) (?te contains "+mj+") ) ) )"+
//				"(not (Dep (id_dep ?i) (rel ?re &:(?re contains "+t7+"))) )"+
//				"(Word (tense ?ne &:(?ne contains "+el+")) (id ?e))"+
//				"(Word (name ?n2) (id ?i2 &:(and ( > ?i2 ?i) ( > ?i2 ?e))) (type "+t2+") (tense ?t &:(?t contains "+dm+")))"+
//				"(not (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
//				"(Word (id ?i4 &:(< ?i4 ?i2)) (type "+t2+") (tense ?tm &:(?tm contains "+fth+")))"+
//				"=>" +
//				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t7+")(word ?n2)) ))\n";
//		
//		rule += "(defrule Subject11" +
//				"(declare (salience -3))"+
//				"(Word (id ?i) (type "+t+") (tense ?te &:(and (not (?te contains "+t27+")) (?te contains "+mj+") ) ) )"+
//				"(not (Dep (id_dep ?i) (rel ?re &:(?re contains "+t7+"))) )"+
//				"(Word (name ?n2) (id ?i2 &:( > ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+dm+")) (suff2 ?sf &:(and (neq ?sf nil) (?sf contains "+h+"))))"+
//				"(not (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
//				"(Word (id ?i4 &:(< ?i4 ?i2)) (type "+t2+") (tense ?tm &:(?tm contains "+fth+")))"+
//				"=>" +
//				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t7+")(word ?n2)) ))\n";
		
		//endregion
		
		//----------------------------------------------------------------------------
		
		//region Sub_mustatr
		
		//���� ���� �����
		rule += "(defrule SubjectM" +
				"(declare (salience -3))"+
				"(Dep (id_word ?i) (rel ?re &:(?re contains "+z3+")))"+
				"(Word (name ?n2) (id ?i2 &:(> ?i2 ?i)) (type "+t+"))"+
//				"(not (Dep (id_dep ?i2) (rel ?rl &:(?rl contains "+t5+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t5_2+")(word ?n2)) ))\n";
		
		//endregion
		
		//endregion
		
		//----------------------------------------------------------------------------------------------
		
		//region Obj
		
		//����� ��  ���� ����
		rule += "(defrule Obj1" +
				"(declare (salience -1))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(or (?te contains "+m+") (and (?te contains "+mj+") (or (?te contains "+m2+") (?te contains "+m3+")) ))) (suff ?s &:(or (eq ?s "+t10+") (eq ?s "+t11+") (eq ?s "+t11_2+") (eq ?s "+t12+") (eq ?s "+t13+") (eq ?s "+t16+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+t8+")(word ?s)) ))\n";
		
		//����� ��  ���� ����
		rule += "(defrule Obj2" +
				"(declare (salience -1))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(or (?te contains "+m+") (and (?te contains "+mj+") (or (?te contains "+m2+") (?te contains "+m3+")) ))) (suff2 ?s &:(or (eq ?s "+t10+") (eq ?s "+t11+") (eq ?s "+t11_2+") (eq ?s "+t12+") (eq ?s "+t13+") (eq ?s "+t16+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+t8+")(word ?s)) ))\n";
		
		
		// ����� �� ��� ����(������� �����)�
		rule += "(defrule Obj3" +
				"(declare (salience -2))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(and (?te contains "+m+") (?te contains "+t28+"))))"+
				"(not (Dep  (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?i2 &:(> ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+fth+")))"+
				"(not (Word (id ?i1 &:(and (< ?i1 ?i2) (< ?i ?i1))) (type "+t2+") (tense ?tn &:(?tn contains "+fth+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t8+")(word ?n2)) ))\n";
		
		//----------------------------------------------------------------------------
		
		// ����� �� ��� ����(������� �����) ���� �������
		rule += "(defrule Obj3_2" +
				"(declare (salience -2))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(and (?te contains "+mj+") (or (?te contains "+m2+") (?te contains "+m3+")) ) ))"+
				"(not (Dep  (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?i2&:(> ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+fth+")))"+
				"(not (Word (id ?i1 &:(and (< ?i1 ?i2) (< ?i ?i1))) (type "+t2+") (tense ?tn &:(?tn contains "+fth+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t8+")(word ?n2)) ))\n";
		
		// ����� �� ��� ����(������� ������)�
		rule += "(defrule Obj4" +
				"(declare (salience -3))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(and (?te contains "+m+") (or (?te contains "+m2+") (?te contains "+m3+")) ) ) )"+
				"(Word (name ?n2) (id ?i2 &:(> ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+fth+")))"+
				"(Dep (id_word ?w1) (id_dep ?i) (rel "+t8+"))"+
				"(not (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+")))"+
				"(not (Word (id ?i1 &:(and (< ?i1 ?i2) (< ?i ?i1) (<> ?i1 ?w1))) (type "+t2+") (tense ?tn &:(?tn contains "+fth+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t8+")(word ?n2)) ))\n";
		
		//---------------------------------------------------------------------------
		
		// ����� �� ��� ����(������� ������) ���� �������
		rule += "(defrule Obj4_2" +
				"(declare (salience -3))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(and (?te contains "+mj+") (?te contains "+m3+") ) ) )"+
				"(Word (name ?n2) (id ?i2 &:(> ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+fth+")))"+
				"(Dep (id_word ?w1) (id_dep ?i) (rel "+t8+"))"+
				"(not (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+")))"+
				"(not (Word (id ?i1 &:(and (< ?i1 ?i2) (< ?i ?i1) (<> ?i1 ?w1))) (type "+t2+") (tense ?tn &:(?tn contains "+fth+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t8+")(word ?n2)) ))\n";
		
		// ����� �� ��� ����(������� ������)�
		rule += "(defrule Obj5" +
				"(declare (salience -4))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(and (?te contains "+m+") (?te contains "+m3+") ) ))"+
				"(Word (name ?n2) (id ?i2 &:(> ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+fth+")))"+
				"(Dep (id_word ?w1) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+"))"+
				"(not (Dep (id_word ?w3 &:(and (neq ?w1 ?w3) (neq ?w2 ?w3))) (id_dep ?i) (rel "+t8+")))"+
				"(not (Word (id ?i1 &:(and (< ?i1 ?i2) (< ?i ?i1) (neq ?i1 ?w1) (neq ?i1 ?w2))) (type "+t2+") (tense ?tn &:(?tn contains "+fth+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+t8+")(word ?n2)) ))\n";
				

		//endregion

		//----------------------------------------------------------------------------------------------
		
		//region Jazem
		
		rule += "(defrule Jazem1" +
				"(declare (salience -1))"+
				"(Word (id ?i) (type "+t1+") (tense ?tn &:(?tn contains "+jm+")) )"+
				"(Word (name ?n) (id ?i2 &:(> ?i2 ?i)) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (?te contains "+mjz+"))))"+
				"(not (Dep  (id_dep ?i) (rel "+jm+")) )"+
				"(not (Word (id ?i3 &:(> ?i2 ?i3)) (type "+t+") (tense ?t &:(and (?t contains "+mdar+") (?t contains "+mjz+")))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+jm+")(word ?n)) ))\n";
		
		rule += "(defrule Jazem2" +
				"(declare (salience -1))"+
				"(Word (id ?i) (name "+la+"))"+
				"(Word (name ?n) (id ?i2 &:(> ?i2 ?i)) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (?te contains "+mjz+"))))"+
				"(not (Dep  (id_dep ?i) (rel "+jm+")) )"+
				"(not (Word (id ?i3 &:(> ?i2 ?i3)) (type "+t+") (tense ?t &:(and (?t contains "+mdar+") (?t contains "+mjz+")))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+jm+")(word ?n)) ))\n";
		
		rule += "(defrule Jazem3" +
				"(declare (salience -1))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (?te contains "+mjz+"))) (pref ?sf &:(?sf contains "+lam+")))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+jm+")(word ?n)) ))\n";
		
		//endregion
		
		//----------------------------------------------------------------------------------------------
		
		//region Jazem_sharti
		
		rule += "(defrule Jazemch1" +
				"(declare (salience -1))"+
				"(Word (id ?i) (name ?n &:(or (?n contains "+jazmch1+") (?n contains "+jazmch2+") (?n contains "+jazmch3+") (?n contains "+jazmch4+") (?n contains "+jazmch5+") (?n contains "+jazmch6+") (?n contains "+jazmch7+") (?n contains "+jazmch8+") (?n contains "+jazmch9+") ) ) )"+
				"(Word (name ?n2) (id ?i2 &:(> ?i2 ?i)) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (?te contains "+mjz+"))))"+
				"(not (Dep  (id_dep ?i) (rel "+jm+")) )"+
				"(not (Word (id ?i3 &:(> ?i2 ?i3)) (type "+t+") (tense ?t &:(and (?t contains "+mdar+") (?t contains "+mjz+")))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+jm+")(word ?n2)) ))\n";
		
		rule += "(defrule Jazemch2" +
				"(declare (salience -1))"+
				"(Word (id ?i) (name ?n &:(or (?n contains "+jazmch1+") (?n contains "+jazmch2+") (?n contains "+jazmch3+") (?n contains "+jazmch4+") (?n contains "+jazmch5+") (?n contains "+jazmch6+") (?n contains "+jazmch7+") (?n contains "+jazmch8+") (?n contains "+jazmch9+") )))"+
				"(Word (name ?n2) (id ?i2 &:(> ?i2 ?i)) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (?te contains "+mjz+"))))"+
				"(Dep  (id_word ?w1) (id_dep ?i) (rel "+jm+"))"+
				"(not (Dep  (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+jm+")) )"+
				"(not (Word (id ?i3 &:(and (> ?i2 ?i3) (neq ?w1 ?i3))) (type "+t+") (tense ?t &:(and (?t contains "+mdar+") (?t contains "+mjz+")))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+jm+")(word ?n2)) ))\n";
		
		//endregion
		
		//----------------------------------------------------------------------------------------------
		
		
		//region Nafi
		
		rule += "(defrule Nafi1" +
				"(declare (salience -1))"+
				"(Word (id ?i) (name "+la+"))"+
				"(Word (name ?n) (id ?i2 &:(> ?i2 ?i)) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (?te contains "+dm+"))))"+
				"(not (Dep  (id_dep ?i) (rel "+nafi+")) )"+
				"(not (Word (id ?i3 &:(> ?i2 ?i3)) (type "+t+") (tense ?t &:(and (?t contains "+mdar+") (?t contains "+dm+")))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+nafi+")(word ?n)) ))\n";
		
		//endregion
		
		//----------------------------------------------------------------------------------------------
		
		//region Nasb
		
		rule += "(defrule Nasb1" +
				"(declare (salience -1))"+
				"(Word (id ?i) (type "+t1+") (tense ?tn &:(or (?tn contains "+nsb+") (?tn contains "+nasb+") (?tn contains "+yansb+") (?tn contains "+naasb+"))) )"+
				"(Word (name ?n) (id ?i2 &:(> ?i2 ?i)) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (?te contains "+fth+"))))"+
				"(not (Dep  (id_dep ?i) (rel "+nasb+")) )"+
				"(not (Word (id ?i3 &:(> ?i2 ?i3)) (type "+t+") (tense ?t &:(and (?t contains "+mdar+") (?t contains "+fth+")))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+nasb+")(word ?n)) ))\n";
		
		
		rule += "(defrule Nasb2" +
				"(declare (salience -1))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (?te contains "+fth+"))) (pref ?sf &:(or (?sf contains "+lam+") (?sf contains "+fa+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+nasb+")(word ?n)) ))\n";
		
		//endregion
		
		//----------------------------------------------------------------------------------------------
				
		//region Nakesa
		
		rule += "(defrule Nak1" +
				"(declare (salience -1))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(?te contains "+nk+")) (suff ?s &:(or (eq ?s "+t17+") (eq ?s "+t18+") (eq ?s "+t19+") (eq ?s "+t20+") (eq ?s "+t21+") (eq ?s "+t22+") (eq ?s "+t23+") (eq ?s "+t24+") (eq ?s "+t25+") (eq ?s "+t26+"))))"+
				"=>" +
				"(assert (Dep (id_word ?i)(id_dep ?i)(rel "+name_nk+")(word ?s)) ))\n";
//		
//		rule += "(defrule Nak2" +
//				"(declare (salience -2))"+
//				"(Word (id ?i) (type "+t+") (tense ?te &:(?te contains "+nk+")))"+
//				"(not (Dep  (id_dep ?i) (rel "+obj_nk+")) )"+
//				"(Word (name ?n) (id ?i2 &:(> ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+fth+")))"+
//				"(not (Word (id ?i3 &:(and (> ?i2 ?i3) (> ?i3 ?i))) (type "+t2+") (tense ?tn &:(?tn contains "+fth+"))) )"+
//				"=>" +
//				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+obj_nk+")(word ?n)) ))\n";
//		
		//----------------------------------------------------------------------------------
		rule += "(defrule Nak4" +
				"(declare (salience -2))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(?te contains "+nk+")))"+
				"(not (Dep  (id_dep ?i) (rel "+name_nk+")) )"+
				"(Word (name ?n) (id ?i2 &:(> ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+dm+")))"+
				"(not (Word (id ?i3 &:(and (> ?i2 ?i3) (> ?i3 ?i))) (type "+t2+") (tense ?tn &:(?tn contains "+dm+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+name_nk+")(word ?n)) ))\n";
		
		rule += "(defrule Nak5" +
				"(declare (salience -2))"+
				"(Word (id ?i) (type "+t+") (tense ?te &:(?te contains "+nk+")))"+
				"(not (Dep  (id_dep ?i) (rel "+obj_nk+")) )"+
				"(Word (name ?n) (id ?i2 &:(> ?i2 ?i)) (type "+t2+") (tense ?t &:(?t contains "+fth+")))"+
				"(not (Word (id ?i3 &:(and (> ?i2 ?i3) (> ?i3 ?i))) (type "+t2+") (tense ?tn &:(?tn contains "+fth+"))) )"+
				"=>" +
				"(assert (Dep (id_word ?i2)(id_dep ?i)(rel "+obj_nk+")(word ?n)) ))\n";
		//endregion
		
		//----------------------------------------------------------------------------------------------
		
		//endregion
		
		//----------------------------------------------------------------------------------------------
		
//		//region Verbes_F_M
//		
//		rule += "(defrule v1" +
//				"(declare (salience 1))"+
//				"?id <- (Word (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mdar+") (not (?tn contains "+male+")))) (pref ?pr &:(?pr contains "+y+")))"+
//				"=>" +
//				"(bind ?su (call ?tn concat " + male + "))"+
//				"(modify ?id (tense ?su)))\n";
//		
//		rule += "(defrule v2" +
//				"(declare (salience 1))"+
//				"?id <-  (Word (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mdar+") (not (?tn contains "+female+")))) (pref ?pr &:(?pr contains "+tt+")))"+
//				"=>" +
//				"(bind ?su (call ?tn concat " + female + "))"+
//				"(modify ?id (tense ?su)))\n";
//		
//		rule += "(defrule v2" +
//				"(declare (salience 1))"+
//				"?id <-  (Word (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mdar+") (not (?tn contains "+female+")))) (pref ?pr &:(?pr contains "+tt+")))"+
//				"=>" +
//				"(bind ?su (call ?tn concat " + female + "))"+
//				"(modify ?id (tense ?su)))\n";
//		
//		//endregion
		
		//region Correct
		
		//region Verb_with_Sub_mabni_llmaaloum
		
		//region Present_Verb
		
		rule += "(defrule c1" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mdar+") (?tn contains "+m+"))) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?w) (id_dep ?i &:(neq ?i ?w)) (rel "+t5+"))"+
				"(Word (name ?n2) (id ?w) (type "+t2+") (tense ?te &:(?te contains "+female+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� �������� (�) ��� ������ ����", RU.STRING))+") (corr2 "+(new Value("������ ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mdar+") (?tn contains "+m+"))) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?w) (id_dep ?i &:(neq ?i ?w)) (rel "+t5+"))"+
				"(Word (name ?n2) (id ?w) (type "+t2+") (tense ?te &:(?te contains "+male+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� �������� (�) ��� ������ ����", RU.STRING))+") (corr2 "+(new Value("������ ���� ����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Past_Verb
		
		rule += "(defrule c3" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mady+") (?tn contains "+m+"))) (suff ?sf &:(?sf contains "+t9+")))"+
				"(Dep (id_word ?w) (id_dep ?i &:(neq ?i ?w)) (rel "+t5+"))"+
				"(Word (name ?n2) (id ?w) (type "+t2+") (tense ?te &:(?te contains "+male+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ������� ������� ��� ������ ����", RU.STRING))+") (corr2 "+(new Value("������ ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c4" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mady+") (?tn contains "+m+"))) (suff ?sf &:(not (?sf contains "+t9+"))))"+
				"(Dep (id_word ?w) (id_dep ?i &:(neq ?i ?w)) (rel "+t5+"))"+
				"(Word (name ?n2) (id ?w) (type "+t2+") (tense ?te &:(?te contains "+female+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ��� ������� ������� ��� ������ ����", RU.STRING))+") (corr2 "+(new Value("������ ���� ����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Sub_Not_exist_mabni_llmaaloum
		
		//region Verb_lazem
		
		rule += "(defrule c5" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (not (?tn contains "+t28+")) (?tn contains "+m+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t5+")))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+fth+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� ��  ������ ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c5_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (not (?tn contains "+t28+")) (?tn contains "+m+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t5+")))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� �������  � ����� ���� ��  ������ ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//---------------------------------------------------------------------------------------------------------
		
		//region Verb_mtaady_mfoul_esm
		
		rule += "(defrule c6" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+m+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t5+")))"+
				"(Dep (id_word ?w1) (id_dep ?i &:(neq ?i ?w1))(rel "+t8+") (word ?s))"+
				"(not (Dep (id_word ?w2 &:(< ?w2 ?w1)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+fth+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?s) (corr1 "+(new Value("����� �� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� �� ������ ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c6_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+m+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t5+")))"+
				"(Dep (id_word ?w1) (id_dep ?i &:(neq ?i ?w1))(rel "+t8+") (word ?s))"+
				"(not (Dep (id_word ?w2 &:(< ?w2 ?w1)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� �� ������ ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//---------------------------------------------------------------------------------------------------------
		
		//region Verb_mtaady_mfoul_damir
		
		rule += "(defrule c7" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+m+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t5+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?w1) (id_dep ?i &:(neq ?i ?w1))(rel "+t8+") (word ?s))"+
				"(not (Dep (id_word ?w2 &:(< ?w2 ?w1)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+fth+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?s) (corr1 "+(new Value("����� �� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� �� ������ ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c7_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+m+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t5+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?w1) (id_dep ?i &:(neq ?i ?w1))(rel "+t8+") (word ?s))"+
				"(not (Dep (id_word ?w2 &:(< ?w2 ?w1)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?s) (corr1 "+(new Value("����� �� ���� ���� , ��� �����  �����", RU.STRING))+") (corr2 "+(new Value("��� �����  � ����� ���� �� ������ ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//---------------------------------------------------------------------------------------------------------
		
		//region Verb_mtaady_mfoul_whed
		
		rule += "(defrule c8" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+m+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t5+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t8+"))"+
				"(not (Dep (id_word ?w2 &:(neq ?w2 ?i)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+fth+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� �� ������ ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		

		rule += "(defrule c8_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+m+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t5+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t8+"))"+
				"(not (Dep (id_word ?w2 &:(neq ?w2 ?i)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� �� ������ ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//endregion
		
		//---------------------------------------------------------------------------------------------------------
		
		//region Obj_Not_exist_mabni_llmaaloum
		
		//region Verb_Mtaady_mfoul
		
		rule += "(defrule c9" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+m+") (not (?tn contains "+m2+")) (not (?tn contains "+m3+")))))"+
				"(not (Dep (id_dep ?i) (rel "+t8+")))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+dm+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ������ � �� ���� ����� �� , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c9_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+m+") (not (?tn contains "+m2+")) (not (?tn contains "+m3+")))))"+
				"(not (Dep (id_dep ?i) (rel "+t8+")))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ������ � �� ���� ����� �� , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Verb_Mtaady_mfoulen
		
		rule += "(defrule c10" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+m+") (?tn contains "+m2+"))))"+
				"(or (not (Dep (id_dep ?i) (rel "+t8+"))) (and (Dep (id_word ?w1) (id_dep ?i) (rel "+t8+")) (not (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+"))) ) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+dm+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� �������� � ��� ����� �������  , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c10_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+m+") (?tn contains "+m2+"))))"+
				"(or (not (Dep (id_dep ?i) (rel "+t8+"))) (and (Dep (id_word ?w1) (id_dep ?i) (rel "+t8+")) (not (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+"))) ) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� �������� � ��� ����� �������  , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Verb_Mtaady_3mfouls
		
		rule += "(defrule c11" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+m+") (?tn contains "+m3+"))))"+
				"(or (not (Dep (id_dep ?i) (rel "+t8+"))) (and (Dep (id_word ?w1) (id_dep ?i) (rel "+t8+")) (not (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+"))) ) (and (Dep (id_word ?w1) (id_dep ?i) (rel "+t8+")) (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+")) (not (Dep (id_word ?w3 &:(and (neq ?w3 ?w1) (neq ?w3 ?w2))) (id_dep ?i) (rel "+t8+")))) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+dm+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ����� ������ � ��� ����� �������  , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c11_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+m+") (?tn contains "+m3+"))))"+
				"(or (not (Dep (id_dep ?i) (rel "+t8+"))) (and (Dep (id_word ?w1) (id_dep ?i) (rel "+t8+")) (not (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+"))) ) (and (Dep (id_word ?w1) (id_dep ?i) (rel "+t8+")) (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+")) (not (Dep (id_word ?w3 &:(and (neq ?w3 ?w1) (neq ?w3 ?w2))) (id_dep ?i) (rel "+t8+")))) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ����� ������ � ��� ����� �������  , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//endregion
				
		//endregion
		
		//--------------------------------------------------------------------------------------------------------

		//region Verb_with_Sub_mabni_llmajhoul
		
		//region Present_Verb
		
		rule += "(defrule c12" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mdar+") (?tn contains "+mj+"))) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?w) (id_dep ?i &:(neq ?i ?w)) (rel "+t7+"))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+female+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� �������� (�) ��� ���� ������ ����", RU.STRING))+") (corr2 "+(new Value("������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c13" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mdar+") (?tn contains "+mj+"))) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?w) (id_dep ?i &:(neq ?i ?w)) (rel "+t7+"))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+male+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� �������� (�) ��� ���� ������ ����", RU.STRING))+") (corr2 "+(new Value("������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Past_Verb
		
		rule += "(defrule c14" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mady+") (?tn contains "+mj+"))) (suff ?sf &:(?sf contains "+t9+")))"+
				"(Dep (id_word ?w) (id_dep ?i &:(neq ?i ?w)) (rel "+t7+"))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+male+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ������� ������� ��� ���� ������ ����", RU.STRING))+") (corr2 "+(new Value("������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c15" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mady+") (?tn contains "+mj+"))) (suff ?sf &:(not (?sf contains "+t9+"))))"+
				"(Dep (id_word ?w) (id_dep ?i &:(neq ?i ?w)) (rel "+t7+"))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+female+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ��� ������� ������� ��� ������ ����", RU.STRING))+") (corr2 "+(new Value("������ ���� ����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//endregion

		//--------------------------------------------------------------------------------------------------------
		
		//region Sub_Not_exist_mabni_llmajhoul
		
		//region Verb_mtaady_mfoul_esm
		
		rule += "(defrule c16" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t7+")))"+
				"(Dep (id_word ?w1) (id_dep ?i &:(neq ?i ?w1))(rel "+t8+") (word ?s))"+
				"(not (Dep (id_word ?w2 &:(< ?w2 ?w1)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+fth+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?s) (corr1 "+(new Value("����� �� ���� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� ���� �� ������ ���� ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c16_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t7+")))"+
				"(Dep (id_word ?w1) (id_dep ?i &:(neq ?i ?w1))(rel "+t8+") (word ?s))"+
				"(not (Dep (id_word ?w2 &:(< ?w2 ?w1)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� ���� �� ������ ���� ������ ���� �����", RU.STRING))+")) ) )\n";
		
	
		//endregion
		
		//---------------------------------------------------------------------------------------------------------
		
		//region Verb_mtaady_mfoul_damir
		
		rule += "(defrule c17" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t7+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?w1) (id_dep ?i &:(neq ?i ?w1))(rel "+t8+") (word ?s))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+fth+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?s) (corr1 "+(new Value("����� �� ���� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� ���� �� ������ ���� ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c17_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t7+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?w1) (id_dep ?i &:(neq ?i ?w1))(rel "+t8+") (word ?s))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?s) (corr1 "+(new Value("����� �� ���� ���� ���� , ��� �����  �����", RU.STRING))+") (corr2 "+(new Value("��� �����  � ����� ���� ���� �� ������ ���� ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//---------------------------------------------------------------------------------------------------------
		
		//region Verb_mtaady_mfoul_whed
		
		rule += "(defrule c18" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t7+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t8+"))"+
				"(not (Dep (id_word ?w2 &:(neq ?w2 ?i)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+fth+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� ���� �� ������ ���� ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		

		rule += "(defrule c18_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t7+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t8+"))"+
				"(not (Dep (id_word ?w2 &:(neq ?w2 ?i)) (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� ���� �� ������ ���� ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c19" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t7+")))"+
				"(not (Dep (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+fth+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� ���� �� ������ ���� ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//---------------------------------------------------------------------------------------------------------
		

		rule += "(defrule c19_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t7+")))"+
				"(not (Dep (id_dep ?i) (rel "+t8+")) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� �� ���� ���� ���� , ��� ����� ������� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� ������� � ����� ���� ���� �� ������ ���� ������ ���� �����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//endregion
		
		//---------------------------------------------------------------------------------------------------------
		
		//region Obj_Not_exist_mabni_llmajhoul
		
		//region Verb_Mtaady_mfoul
		
		rule += "(defrule c19" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+m2+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t8+")))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+dm+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ������ � �� ���� ����� �� , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c19_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+m2+") (?tn contains "+mj+"))))"+
				"(not (Dep (id_dep ?i) (rel "+t8+")))"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ������ � �� ���� ����� �� , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Verb_Mtaady_mfoulen
		
		rule += "(defrule c20" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mj+") (?tn contains "+m3+"))))"+
				"(or (not (Dep (id_dep ?i) (rel "+t8+"))) (and (Dep (id_word ?w1) (id_dep ?i) (rel "+t8+")) (not (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+"))) ) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+dm+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� �������� � ��� ����� �������  , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c20_2" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+mj+") (?tn contains "+m3+"))))"+
				"(or (not (Dep (id_dep ?i) (rel "+t8+"))) (and (Dep (id_word ?w1) (id_dep ?i) (rel "+t8+")) (not (Dep (id_word ?w2 &:(neq ?w1 ?w2)) (id_dep ?i) (rel "+t8+"))) ) )"+
				"(Word (name ?n2) (id ?w &:(> ?w ?i)) (type "+t2+") (tense ?te &:(?te contains "+ks+")))"+
				"(not (Dep (id_word ?w)))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� �������� � ��� ����� �������  , ����� ������� ��� �� ���� �����", RU.STRING))+") (corr2 "+(new Value("��� ����� � ����� ����� ��", RU.STRING))+")) ) )\n";
		
		//endregion
				
		//endregion
		
		//--------------------------------------------------------------------------------------------------------

		//region Nakes_Name
		rule += "(defrule c21" +
				"(declare (salience -10))"+
				"(Word (id ?i) (name ?n1) (type "+t+") (tense ?te &:(?te contains "+nk+")))"+
				"(Dep (id_word ?i2 &:(neq ?i2 ?i))(id_dep ?i)(rel "+name_nk+"))"+
				"(Word (name ?n2) (id ?i2) (type "+t2+") (tense ?tn &:(?tn contains "+nak+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� ������ ��� �� ���� ������", RU.STRING))+") (corr2 "+(new Value("", RU.STRING))+")) ) )\n";
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Jazem
		rule += "(defrule c22" +
				"(declare (salience -10))"+
				"(Word (id ?i) (name ?n1) (type "+t1+") (tense ?tn &:(?tn contains "+jm+")) )"+
				"(Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (name ?n2) (type "+t+") (tense ?te &:(not (?te contains "+mdar+"))))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("�� ���� ��� ���� ����� ��� ��� �������", RU.STRING))+") (corr2 "+(new Value("", RU.STRING))+")) ) )\n";
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Nafi
		rule += "(defrule c23" +
				"(declare (salience -10))"+
				"(Word (id ?i) (name "+la+"))"+
				"(Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (name ?n2) (type "+t+") (tense ?te &:(not (?te contains "+mdar+"))))"+
				"=>" +
				"(assert (Err (word1 "+la+") (word2 ?n2) (corr1 "+(new Value("�� ���� ��� ���� ����� (��) ��� ��� �������", RU.STRING))+") (corr2 "+(new Value("", RU.STRING))+")) ) )\n";
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Naseb
		rule += "(defrule c24" +
				"(declare (salience -10))"+
				"(Word (id ?i) (name ?n1) (type "+t1+") (tense ?tn &:(or (?tn contains "+nsb+") (?tn contains "+nasb+") (?tn contains "+yansb+") (?tn contains "+naasb+"))) )"+
				"(Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (name ?n2) (type "+t+") (tense ?te &:(not (?te contains "+mdar+"))))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("�� ���� ��� ���� ����� ��� ��� �������", RU.STRING))+") (corr2 "+(new Value("", RU.STRING))+")) ) )\n";
		
		//*****
		rule += "(defrule c25" +
				"(declare (salience -10))"+
				"(Word (name ?n) (id ?i) (type "+t+") (tense ?te &:(not (?te contains "+mdar+"))) (pref ?sf &:(or (?sf contains "+lam+") (?sf contains "+fa+"))))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("�� ���� ��� ���� ����� ��� ��� �������", RU.STRING))+") (corr2 "+(new Value("", RU.STRING))+")) ) )\n";
		
		rule += "(defrule c26" +
				"(declare (salience -10))"+
				"(Word (id ?i) (name ?n1) (type "+t1+") (tense ?tn &:(or (?tn contains "+nsb+") (?tn contains "+nasb+") (?tn contains "+yansb+") (?tn contains "+naasb+"))) )"+
				"(Word (name ?n2) (id ?i2 &:(eq ?i2 (+ ?i 1))) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (not (?te contains "+fth+")))))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ������� ���� ���� ��� ��� �� ���� �������", RU.STRING))+") (corr2 "+(new Value("", RU.STRING))+")) ) )\n";
				
		
		rule += "(defrule c27" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?te &:(and (?te contains "+mdar+") (not (?te contains "+fth+")))) (pref ?sf &:(or (?sf contains "+lam+") (?sf contains "+fa+"))))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?sf) (corr1 "+(new Value("����� ������� ���� �� ���� ��� ��� �� ���� �������", RU.STRING))+") (corr2 "+(new Value("", RU.STRING))+")) ) )\n";
				
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Aatef
		rule += "(defrule c28" +
				"(declare (salience -10))"+
				"(Word (name ?n1) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Word (id ?w &:(> ?w ?i)) (type "+t1+") (tense ?te &:(?te contains "+z18+")))"+
				"(Word (name ?n2) (id ?i2 &:(eq ?i2 (+ ?w 1))) (type "+t+") (tense ?tn2 &:(?tn2 contains "+mdar+")) (pref ?pr2 &:(?pr2 contains "+tt+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� �� ���� ����� ������� ����� ����� ��� ����", RU.STRING))+") (corr2 "+(new Value("��� �� ���� ����� ������� ������ ����� ��� ����", RU.STRING))+")) ) )\n";
		
		
		//endregion
		
		//--------------------------------------------------------------------------------------------------------
		
		//region Sub_mutater
		
		//region Present
		
		rule += "(defrule c29" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� �������� ��� �� ����(�) ��� ����� ������ ���� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c30" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������ ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c31" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������  � ��� �������� (�) ���� ���� ��� ��� ���� ������ (��)", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c32" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������ ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c33" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������  � ��� �������� (�) ���� ���� ��� ��� ���� ������ (��)", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c34" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������ ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c35" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������  � ��� �������� (�) ���� ���� ��� ��� ���� ������ (��)", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c36" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("���  ��� ��������  �� ���� (�) ���� ���� ��� ��� ���� ������ (��)", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ������", RU.STRING))+")) ) )\n";
		
		//--------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c37" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� � ��� �������� (�) ��� ����� ������� ���� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ������", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		

		rule += "(defrule c38" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� �������  ��� ����� ������� ����� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c39" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c40" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� � ��� �������� (�) ���� ���� ��� ��� ���� ������ (��)", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c41" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c42" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� � ��� �������� (�) ���� ���� ��� ��� ���� ������ (��)", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c43" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� �������� ��� �� ����(�)� ��� ��� ������� ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c44" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� �� ����� ���� ���� ��� ���� ����  ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//--------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c45" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������ � ��� ��� �������� (�) ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ������", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		

		rule += "(defrule c46" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������  ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c47" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c48" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� � ��� ��� ������ (�) ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c49" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� �������� (�) ���� ���� ��� ���� ���� ������ (��)", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c50" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c51" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� �� ����� � ��� ��� �������� (�) ���� ���� ��� ���� ����  ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//--------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c52" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������  ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ������", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		

		rule += "(defrule c53" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������  � ��� ��� �������� (�) ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c54" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� � ��� ��� �������� (�) ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c55" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� �������  ��� ����� ������ ���� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c56" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� �������� (�) ���� ���� ��� ���� ���� ������ (��)", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		
		//-------------------------------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c57" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� �������� ��� �� ����(�)� ������� ��� ������� ���� ������� ��� ����� ������ ���� ���� ���� ������ ���", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c58" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ������� ��� ������� ���� ������� ���� ���� ��� ���� ����  ������ ���", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//--------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c59" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ������� ��� ������ ���� ������� � ��� ��� �������� (�) ��� ����� ������ ���� ���� ���� ������ ���", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ������", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		

		rule += "(defrule c60" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ������� ��� ������ ���� ������� ��� ����� ������ ���� ���� ���� ������ ���", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c61" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("���  ��� ��� ������ (�) ��� ����� ������ ���� ���� ���� ������ ���", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c62" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� �������� (�) � ����� ����� ���� ������� ���� ���� ��� ���� ���� ������ (���)", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c63" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("���  ����� ����� ���� ������� ���� ���� ��� ���� ���� ������ (���)", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c64" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("���  ������� ��� ������� ���� ������� ��� ����� ������ ���� ���� ���� ������ ���", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c65" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� �������� (�) � ������� ��� ������� ���� ������� ���� ���� ��� ���� ����  ������ ���", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//--------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c66" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ������� ��� ������ ���� �������  ��� ����� ������ ���� ���� ���� ������ ���", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ������", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		

		rule += "(defrule c67" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ������� ��� ������ ���� ������� � ��� ��� �������� (�) ��� ����� ������ ���� ���� ���� ������ ���", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c68" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t24+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("���  ��� ��� ������ (�) ��� ����� ������ ���� ���� ���� ������ ���", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c69" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+tt+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("���  ����� ����� ���� ������� ���� ���� ��� ���� ���� ������ (���)", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c70" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+du+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mdar+")) (pref ?pr &:(?pr contains "+y+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("���  ��� ��� �������� (�)� ����� ����� ���� ������� ���� ���� ��� ���� ���� ������ (���)", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//endregion
		
		//-------------------------------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------------------------------
		
		//region Past
		rule += "(defrule c71" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")) (suff ?sf &:(?sf contains "+t9+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ��� �� ���� ���� ������� � ��� ��� ������� ������� ���� ���� ��� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ��� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c72" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")) (suff ?sf &:(?sf contains "+t9+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ��� �� ���� ���� ������ � ��� ��� ������� ������� ���� ���� ��� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ��� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		rule += "(defrule c73" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t18+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ��� �� ���� ���� ������� � ��� ��� ������ �������� ���� ���� ��� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ���", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c74" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t18+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("����� ����� ��� �� ���� ���� ������ � ��� ��� ������ �������� ���� ���� ��� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ���", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c75" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������  � ��� ��� ������� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c76" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t17+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������  � ��� ��� ������� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ���� �� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c77" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t17+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� �������  � ��� ��� ������� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ���� �� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c78" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")) (suff ?sf &:(not (?sf contains "+t9+"))))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������ ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c79" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")) (suff ?sf &:(not (?sf contains "+t9+"))))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� ���� ���� ��� ��� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c80" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������ � ����� ��� ������� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//--------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c81" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t21+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� � ��� �� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ���", RU.STRING))+")) ) )\n";
		
		//--------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c82" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t21+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������ � ��� �� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ���", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		//-------------------------------------------------------------------------------------------------------

		//region Past
		rule += "(defrule c83" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")) (suff ?sf &:(?sf contains "+t9+")))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� ������� ���� ���� ��� ��� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ��� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
				
		rule += "(defrule c84" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t18+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������ �������� ���� ���� ��� ��� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ���", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------

		rule += "(defrule c85" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t18+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������ ��������  � ��������� ���� ������� ������� ���� ���� ��� ��� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ���", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
				
		rule += "(defrule c86" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� ���� ���� ��� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c87" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t19+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������� � �������� ���� ������� ������� ���� ���� ��� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ��� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c88" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+male+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t17+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("���  ��� ��� ������� ���� ���� ��� ����  ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ���� �� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c89" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
				"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t17+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� ������� � ��� ��� ������� ���� ���� ��� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ����� ������ ���� ���� ���� �� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
		rule += "(defrule c90" +
				"(declare (salience -10))"+
				"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
				"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+sin+") (?te contains "+female+"))))"+
				"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")) (suff ?sf &:(not (?sf contains "+t9+"))))"+
				"(not (Dep (id_word ?i) (id_dep ?i) (rel "+t5+")))"+
				"=>" +
				"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� ������� ���� ���� ��� ���� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ��� ������ ���� ���� ����", RU.STRING))+")) ) )\n";
		
		//-------------------------------------------------------------------------------------------------------
		
//			rule += "(defrule c91" +
//					"(declare (salience -10))"+
//					"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
//					"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
//					"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
//					"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t20+")))"+
//					"=>" +
//					"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ��� ��� ������ � ����� ��� ������� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("������ ���� ��� ����", RU.STRING))+")) ) )\n";
//			
//			//--------------------------------------------------------------------------------------------------------
//					
//				rule += "(defrule c81" +
//						"(declare (salience -10))"+
//						"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
//						"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+male+"))))"+
//						"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
//						"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t21+")))"+
//						"=>" +
//						"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������� � ��� �� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ���", RU.STRING))+")) ) )\n";
//				
//				//--------------------------------------------------------------------------------------------------------
//				
//				rule += "(defrule c82" +
//						"(declare (salience -10))"+
//						"(Dep (id_word ?i) (id_dep ?i2) (rel "+t5_2+"))"+
//						"(Word (name ?n1) (id ?i2) (type "+t2+") (tense ?te &:(and (?te contains "+plu+") (?te contains "+female+"))))"+
//						"(Word (name ?n2) (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+mady+")))"+
//						"(Dep (id_word ?i) (id_dep ?i) (rel "+t5+") (word ?w &:(?w contains "+t21+")))"+
//						"=>" +
//						"(assert (Err (word1 ?n1) (word2 ?n2) (corr1 "+(new Value("��� ����� �� ���� ���� ������ � ��� �� ���� ���� ��� ��� ���� ������ ��", RU.STRING))+") (corr2 "+(new Value("��� ���� ����� ���", RU.STRING))+")) ) )\n";
//				
//				//-------------------------------------------------------------------------------------------------------
//				//-------------------------------------------------------------------------------------------------------

		//endregion
		
		//endregion
		
		//endregion
		
		//region Edit
		
		//region Lazem
		
		rule += "(defrule edit1" +
				"(declare (salience 1))"+
				"(Word (type "+t+") (tense ?tn &:(not (?tn contains "+t28+"))))"+
				"?id <- (Word (type "+t2+") (tense ?te&/.*���� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit2" +
				"(declare (salience 1))"+
				"(Word (type "+t+") (tense ?tn &:(not (?tn contains "+t28+"))))"+
				"?id <- (Word (type "+t2+") (tense ?te&/.*��� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit3" +
				"(declare (salience 1))"+
				"(Word (type "+t+") (tense ?tn &:(not (?tn contains "+t28+"))))"+
				"?id <- (Word (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ))"+
				"=>" +
				"(bind ?su (call ?te concat " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		//endregion

		//region Mtaady_have_obj
		
		rule += "(defrule edit4" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (not (?tn contains "+m2+")) (not (?tn contains "+m3+")) )))"+
				"(Dep (id_word ?i2) (id_dep ?i4 &:(eq ?i4 ?i)) (rel "+t8+"))"+
				"?id <- (Word (id ?i3 &:(neq ?i3 ?i2)) (type "+t2+") (tense ?te&/.*��� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit4_2" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+m2+")))"+
				"(Dep (id_word ?i2) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i4 &:(neq ?i4 ?i2)) (id_dep ?i) (rel "+t8+"))"+
				"?id <- (Word (id ?i3 &:(and (neq ?i2 ?i3) (neq ?i4 ?i3))) (type "+t2+") (tense ?te&/.*��� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit4_3" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+m3+")))"+
				"(Dep (id_word ?i2) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i3 &:(neq ?i3 ?i2)) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i4 &:(and (neq ?i3 ?i4) (neq ?i2 ?i4))) (id_dep ?i) (rel "+t8+"))"+
				"?id <- (Word (id ?i5 &:(and (neq ?i5 ?i4) (neq ?i5 ?i3) (neq ?i5 ?i2))) (type "+t2+") (tense ?te&/.*��� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit5" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (not (?tn contains "+m2+")) (not (?tn contains "+m3+")) )))"+
				"(Dep (id_word ?i2) (id_dep ?i4 &:(eq ?i4 ?i)) (rel "+t8+"))"+
				"?id <- (Word (id ?i3 &:(neq ?i3 ?i2)) (type "+t2+") (tense ?te&/.*���� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit5_2" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+m2+")))"+
				"(Dep (id_word ?i2) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i4 &:(neq ?i4 ?i2)) (id_dep ?i) (rel "+t8+"))"+
				"?id <- (Word (id ?i3 &:(and (neq ?i2 ?i3) (neq ?i4 ?i3))) (type "+t2+") (tense ?te&/.*���� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit5_3" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+m3+")))"+
				"(Dep (id_word ?i2) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i3 &:(neq ?i3 ?i2)) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i4 &:(and (neq ?i3 ?i4) (neq ?i2 ?i4))) (id_dep ?i) (rel "+t8+"))"+
				"?id <- (Word (id ?i5 &:(and (neq ?i5 ?i4) (neq ?i5 ?i3) (neq ?i5 ?i2))) (type "+t2+") (tense ?te&/.*���� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit6" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (not (?tn contains "+m2+")) (not (?tn contains "+m3+")) )))"+
				"(Dep (id_word ?i2) (id_dep ?i) (rel "+t8+"))"+
				"?id <- (Word (id ?i3 &:(neq ?i3 ?i2)) (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ) )"+
				"=>" +
				"(bind ?su (call ?te concat " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit6_2" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+m2+")))"+
				"(Dep (id_word ?i2) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i4 &:(neq ?i4 ?i2)) (id_dep ?i) (rel "+t8+"))"+
				"?id <- (Word (id ?i3 &:(and (neq ?i2 ?i3) (neq ?i4 ?i3))) (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ) )"+
				"=>" +
				"(bind ?su (call ?te concat " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit6_3" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+m3+")))"+
				"(Dep (id_word ?i2) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i3 &:(neq ?i3 ?i2)) (id_dep ?i) (rel "+t8+"))"+
				"(Dep (id_word ?i4 &:(and (neq ?i3 ?i4) (neq ?i2 ?i4))) (id_dep ?i) (rel "+t8+"))"+
				"?id <- (Word (id ?i5 &:(and (neq ?i5 ?i4) (neq ?i5 ?i3) (neq ?i5 ?i2))) (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ) )"+
				"=>" +
				"(bind ?su (call ?te concat " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		//endregion

		//region Jar_w_Mjror
		
		// ��� �� ����
		rule += "(defrule edit7" +
				"(declare (salience 1))"+
				"?id <- (Word (type "+t2+") (tense ?te&/.*���� .*�����.*/) (pref ?p&:(and (neq ?p nil) (or (?p contains "+b+") (?p contains "+l+") (?p contains "+k+") (?p contains "+tt+")))))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit8" +
				"(declare (salience 1))"+
				"?id <- (Word (type "+t2+") (tense ?te&/.*��� .*�����.*/) (pref ?p&:(or (?p contains "+b+") (?p contains "+l+") (?p contains "+k+") (?p contains "+tt+"))))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit9" +
				"(declare (salience 1))"+
				"?id <- (Word (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ) (pref ?p&:(or (?p contains "+b+") (?p contains "+l+") (?p contains "+k+") (?p contains "+tt+"))))"+
				"=>" +
				"(bind ?su (call ?te concat " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		//----------------------------------------------------------------------------------------------------
		
		// ��� �� �����  
		rule += "(defrule edit10" +
				"(declare (salience 1))"+
				"(Word (id ?i) (tense "+jr+"))"+
				"?id <- (Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (type "+t2+") (tense ?te&/.*���� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit11" +
				"(declare (salience 1))"+
				"(Word (id ?i) (tense "+jr+"))"+
				"?id <- (Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (type "+t2+") (tense ?te&/.*��� .*�����.*/))"+
				"=>" +
				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit12" +
				"(declare (salience 1))"+
				"(Word (id ?i) (tense "+jr+"))"+
				"?id <- (Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ) )"+
				"=>" +
				"(bind ?su (call ?te concat " + ks + "))"+
				"(modify ?id (tense ?su)))\n";
		
		//region
		
		//endregion
		
		engine.executeCommand(rule);
		engine.executeCommand("(run)");
		
		//endregion

////		//region Modaf
//		
//		rule += "(defrule edit13" +
//				"(declare (salience 1))"+
//				"(Word (id ?i) (type "+t2+") (tense ?tn &:(and (not (?tn contains "+dm+")) (not (?tn contains "+fth+")) ) ) )"+
//				"?id <- (Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (type "+t2+") (tense ?te&/.*���� .*�����.*/))"+
//				"=>" +
//				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
//				"(modify ?id (tense ?su)))\n";
//		
//		rule += "(defrule edit14" +
//				"(declare (salience 1))"+
//				"(Word (id ?i) (type "+t2+") (tense ?tn &:(and (not (?tn contains "+dm+")) (not (?tn contains "+fth+")) ) ) )"+
//				"?id <- (Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (type "+t2+") (tense ?te&/.*��� .*�����.*/))"+
//				"=>" +
//				"(bind ?su (call ?te replace " + fth + " " + ks + "))"+
//				"(modify ?id (tense ?su)))\n";
//		
//		rule += "(defrule edit15" +
//				"(declare (salience 1))"+
//				"(Word (id ?i) (type "+t2+") (tense ?tn &:(and (not (?tn contains "+dm+")) (not (?tn contains "+fth+")) ) ) )"+
//				"?id <- (Word (id ?i2 &:(eq ?i2 (+ ?i 1))) (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not (?te contains "+fth+")) (not (?te contains "+ks+")) ) ) )"+
//				"=>" +
//				"(bind ?su (call ?te concat " + ks + "))"+
//				"(modify ?id (tense ?su)))\n";
//				
//		//endregion
		
		//endregion
	   
		//region Edit2
		
		// ����� + ��� ���� 
		rule += "(defrule edit15" +
				"(declare (salience 2))"+
				"?id <- (Word (type "+t2+") (tense ?te &:(not(?te contains "+dm+"))) (last "+dmm+") )"+
				"=>" +
				"(bind ?su (call ?te concat " + dm + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit16" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(and (?tn contains "+t28+") (not (?tn contains "+m2+")) (not (?tn contains "+m3+")) )))"+
				"?id <- (Word (id ?i2) (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ) )"+
				"(not (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?te1 &:(?te1 contains "+fth+"))))"+
				"=>" +
				"(bind ?su (call ?te concat " + fth + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit16_2" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+m2+")))"+
				"?id <- (Word (id ?i2) (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ) )"+
				"(not (and (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?te1 &:(?te1 contains "+fth+"))) (Word (id ?i4 &:(and (neq ?i4 ?i3) (< ?i4 ?i2))) (type "+t2+") (tense ?te2 &:(?te2 contains "+fth+")))))"+
				"=>" +
				"(bind ?su (call ?te concat " + fth + "))"+
				"(modify ?id (tense ?su)))\n";
		
		rule += "(defrule edit16_3" +
				"(declare (salience 1))"+
				"(Word (id ?i) (type "+t+") (tense ?tn &:(?tn contains "+m3+")))"+
				"?id <- (Word (id ?i2) (type "+t2+") (tense ?te&/.*��� ����.*/&:(and (not(?te contains "+fth+")) (not(?te contains "+ks+")) ) ) )"+
				"(not (and (Word (id ?i3 &:(< ?i3 ?i2)) (type "+t2+") (tense ?te1 &:(?te1 contains "+fth+"))) (Word (id ?i4 &:(and (neq ?i4 ?i3) (< ?i4 ?i2))) (type "+t2+") (tense ?te2 &:(?te2 contains "+fth+"))) (Word (id ?i5 &:(and (neq ?i5 ?i3) (neq ?i5 ?i4) (< ?i5 ?i2))) (type "+t2+") (tense ?te3 &:(?te3 contains "+fth+"))) ))"+
				"=>" +
				"(bind ?su (call ?te concat " + fth + "))"+
				"(modify ?id (tense ?su)))\n";
		
		//endregion
		
		//-----------------------------------------------------------------------------------------------
		
		//region Rules_2
		String zrule = "(defrule �������" + 
				  "(Word (id 0)(name ?n) (type "+z1+") (tense ?t)(pref "+z+"))" + 
				  "(test (not (?t contains ��� )))" +
						  
				  "=>" + 
				  "(assert (Dep (id_word 0) (id_dep 0) (rel "+z3+") (word ?n))))";
				  
				  
		 String zrule1 = "(defrule �����" + 
				  "(Dep (id_word 0) (id_dep 0) (rel "+z3+") )" + 
				  "(Word (id 0)(name ?n) (pref "+z+"))"+
				  "(Word (id 1)(name ?n1)(type "+z1+") (pref "+z2+"))"+
			      "(not (Dep (id_word 1) (id_dep 1) (rel "+l15+") ))"+
				  "=>" + 
				  "(assert (Dep (id_word 1) (id_dep 0) (rel "+z4+") (word ?n1))))"; 
		  
		  
		  
		 String zrule2 = "(defrule ����_���� " + 
				  "(declare (salience 100))"+
				  "(Word (id ?i)(name ?n) (type "+z1+")(tense ?t) (pref "+z2+"))" + 
				  "(Word (id ?i1)(name ?n1) (type "+z1+") (pref "+z+"))"+
			      "(test (not(?t contains ����� )))" +
			      "(not(Dep (id_word ?i1) (id_dep ?i) (rel ?r) (word ?n2)))"+
				  " (test (= ?i1(+ ?i 1)) )"+
				  "=>" + 
				  "(assert (Dep (id_word ?i1) (id_dep ?i) (rel "+z6+") (word ?n1))))"; 
		  
		  String zrule3 = "(defrule �������1" + 
				  "(Word (id 0)(name ?n) (tense ?x))" +  
				  "(test (?x contains ������))" +
				  "=>" + 
				  "(assert (Dep (id_word 0) (id_dep 0) (rel "+z3+") (word ?n))))";
		  
		  String zrule4 = "(defrule �������2" + 
				  "(Word (id 0)(name ?n) (tense ?x))" +  
				  "(test (?x contains �����))" +
				  "(Word (id 1)(name ?n1) (type "+z1+"))" +  
				  "=>" + 
				  "(assert (Dep (id_word 0) (id_dep 0) (rel "+z3+") (word ?n)))"+
		          "(assert (Dep (id_word 1) (id_dep 0) (rel "+z43+") (word ?n1))))";
		  
		  String zrule5 = "(defrule 3�������" + 
				  "(Word (id 0)(name ?n) (type "+z1+") (tense "+l9+"))" + 
				  "=>" + 
				  "(assert (Dep (id_word 0) (id_dep 0) (rel "+z3+") (word ?n))))";
		  
		  String zrule6 = "(defrule 4�������" + 
				  "(Word (id 0)(name ?n) (type "+z1+") )" + 
				  "(name of prson found at start)" +
				  "=>" + 
				  "(assert (Dep (id_word 0) (id_dep 0) (rel "+z3+") (word ?n))))";
		  
		  String zrule7 = "(defrule 5�������" + 
				  "(Word (id 0)(name ?n) (type "+z1+") (pref "+z2+"))" + 
				  "(Word (id 1)(name ?n1) (type "+z1+") (pref "+z+"))"+
				  "=>" + 
				  "(assert (Dep (id_word 0) (id_dep 0) (rel "+z3+") (word ?n))))";
				
				  
		  String zrule8 = "(defrule �����2" + 
			        "(Word (id 0)(name ?n) (type "+z1+") (pref "+z2+"))" +
			        "(Word (id 1)(name ?n1) (type "+z1+") (pref "+z+")(last "+z5+"))"+
			        "(Word (id 2)(name ?n3)(type "+z1+") (pref "+z2+"))"+
			        "(not (Dep (id_word 2) (id_dep 2) (rel "+l15+") ))"+
			        "=>" + 
			        "(assert (Dep (id_word 2) (id_dep 0) (rel "+z4+") (word ?n3))))";
		  
		  String zrule9 = "(defrule �����_������ " + 
				  "(Dep (id_word 0) (id_dep 0) (rel "+z3+") )" + 
				  "(Dep (id_word 1) (id_dep 0) (rel "+z4+") )"+
				  "(Word (id 2)(name ?n3)(type "+z1+") (pref "+z2+"))"+
				  "=>" + 
				  "(assert (Dep (id_word 2) (id_dep 0) (rel "+l10+") (word ?n3))))";
		  
		  
		  String zrule10 = "(defrule 2�����_������ " + 
				    "(Word (id 0)(name ?n) (type "+z1+") (pref "+z2+"))" +
			        "(Word (id 1)(name ?n1) (type "+z1+") (pref "+z+")(last "+z5+"))"+
			        "(Word (id 2)(name ?n3)(type "+z1+") (pref "+z2+"))"+
			        "(Word (id 3)(name ?n4)(type "+z1+") (pref "+z2+"))"+
			        "=>" + 
			        "(assert (Dep (id_word 3) (id_dep 0) (rel "+l10+") (word ?n3))))";
		  
		  String zrule11 = "(defrule 6������� " + 
				    "(Word (id 0)(name ?n) (type "+z1+") (tense ?x) (pref "+z2+") )" +
			        "(Word (id 1)(name ?n1) (type "+z1+")(tense ?t) (pref "+z2+"))"+
			        "(test (?t contains ���))" +
			        "(not (Dep (id_word 1) (id_dep 1) (rel "+l15+") ))"+
			        "(Word (id 2))"+
			        "=>" + 
			        "(assert (Dep (id_word 1) (id_dep 0) (rel "+l11+") (word ?n1)))"+
			        "(assert (Dep (id_word 0) (id_dep 0) (rel "+z3+") (word ?n))))";
		  
		  String zrule12 = "(defrule �����3" + 
				  "(Dep (id_word 0) (id_dep 0) (rel "+z3+") )" + 
				  "(Word (id 0)(name ?n) (tense ?x))" +  
				  "(test (?x contains ������))" +
				  "(Word (id 1)(name ?n1)(type "+z1+") (pref "+z2+"))"+
				  "(not (Dep (id_word 1) (id_dep 1) (rel "+l15+") ))"+
				  "=>" + 
				  "(assert (Dep (id_word 1) (id_dep 0) (rel "+z4+") (word ?n1))))"; 
		  
		  String zrule13 = "(defrule �����4" + 
				  "(Dep (id_word 0) (id_dep 0) (rel "+z3+") )" + 
				  "(Word (id 0)(name ?n) (tense ?x))" +  
				  "(test (?x contains �����))" +
				  "(Word (id 2)(name ?n1)(type "+z1+") (pref "+z2+"))"+
				  "(not(Dep (id_word 2) (id_dep 2) (rel "+l15+" )))"+
				  "=>" + 
				  "(assert (Dep (id_word 2) (id_dep 0) (rel "+z4+") (word ?n1))))"; 
		  
		  String zrule14 = "(defrule �����5" + 
				  "(Dep (id_word 0) (id_dep 0) (rel "+z3+") )" + 
				  "(Word (id 0)(name ?n) (type "+z1+") )" + 
				  "(name of prson found at start)" +
				  "(Word (id 1)(name ?n1)(type "+z1+") (pref "+z2+"))"+
				  "(not (Dep (id_word 1) (id_dep 1) (rel "+l15+") ))"+
				  "=>" + 
				  "(assert (Dep (id_word 1) (id_dep 0) (rel "+z4+") (word ?n1))))"; 
		  
		  String zrule15 = "(defrule �����_������ " + 
				  "(Dep (id_word 0) (id_dep 0) (rel "+z3+") )" + 
				  "(Dep (id_word 1) (id_dep 0) (rel "+z4+") )"+
				  "(Dep (id_word 2) (id_dep 0) (rel "+l10+") )"+
				  "(Word (id 3)(name ?n3)(type "+z1+") (pref "+z2+"))"+
				  "=>" + 
				  "(assert (Dep (id_word 3) (id_dep 0) (rel "+l12+") (word ?n3))))";
		  
		  String zrule16 = "(defrule 2�����_������ " + 
				    "(Word (id 0)(name ?n) (type "+z1+") (pref "+z2+"))" +
			        "(Word (id 1)(name ?n1) (type "+z1+") (pref "+z+")(last "+z5+"))"+
			        "(Word (id 2)(name ?n3)(type "+z1+") (pref "+z2+"))"+
			        "(Word (id 3)(name ?n4)(type "+z1+") (pref "+z2+"))"+
			        "(Word (id 4)(name ?n5)(type "+z1+") (pref "+z2+"))"+
			        "=>" + 
			        "(assert (Dep (id_word 3) (id_dep 0) (rel "+l12+") (word ?n4))))";
		  
		  String zrule17 = "(defrule ���_����� " + 
				    "(Word (id ?i)(name ?n) (type "+l8+") (tense ?t))" +
				    "(test  (or(?t contains �� )(?t contains ��� )))" +
			        "(Word (id ?i1)(name ?n1) (type "+z1+") )"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "=>" + 
			        "(assert (Dep (id_word ?i1) (id_dep ?i) (rel "+l14+") (word ?n1))))";
		  
		  String zrule18 = "(defrule ��_�������� " + 
				    "(Word (id ?i)(name ?n) (type "+z1+")(tense ?o) (pref "+z2+") (suff ?x))" +
				    "(test  (?o contains ���� ))" +
				    "(test  (?o contains ���� ))" +
				    "(proun (member ?m)(m ?h))"+
				    "(test (not(?h contains ?x)))"+
				    "(test (?m contains ?x))"+
			        "=>" + 
			        "(assert (Dep (id_word ?i) (id_dep ?i) (rel "+l15+") (word ?x))))";
		  
		  String zrule19 = "(defrule ��_��������1 " + 
				    "(declare (salience -10))"+
				    "(Word (id ?i)(name ?n) (type "+z1+") (tense ?o)(pref "+z2+") (suff ?x))" +
				    "(test  (not(?o contains ���� )))" +
				    "(proun (member ?m)(m ?h))"+
				    "(test (not(?h contains ?x)))"+
				    "(test (?m contains (sub-string 1  (str-length ?x) ?x)))"+
			        "=>" + 
			        "(assert (Dep (id_word ?i) (id_dep ?i) (rel "+l15+") (word (sub-string 2  (str-length ?x) ?x)))))";
		 
		  String zrule20 = "(defrule ��_��������2 " + 
				    "(Word (id ?i)(name ?n) (type "+z1+")(tense ?o) (pref "+z2+") (suff ?x))" +
				    "(test  (?o contains ���� ))" +
				    "(test  (?o contains ���� ))" +
				    "(test  (?o contains ������� ))" +
				    "(proun (member ?m)(m ?h))"+
				    "(test (not(?h contains ?x)))"+
				    "(test (?m contains (sub-string 1  (str-length ?x) ?x)))"+
			        "=>" + 
			        "(assert (Dep (id_word ?i) (id_dep ?i) (rel "+l15+") (word (sub-string 2  (str-length ?x) ?x)))))";
		 
		  String zrule21 = "(defrule 7�������" + 
				  "(Word (id 0)(name ?n) (type "+z1+") (pref "+z2+"))" + 
				  "(Dep (id_word 0) (id_dep 0) (rel "+l15+") )"+ 
				  "=>" + 
				  "(assert (Dep (id_word 0) (id_dep 0) (rel "+z3+") (word ?n))))";
		  
		  String zrule22 = "(defrule 8�������" + 
				  "(Dep (id_word 0) (id_dep 0) (rel "+z3+") )"+
				  "(Dep (id_word 0) (id_dep 0) (rel "+l15+") )"+ 
				  "(Word (id 1)(name ?n) (type "+z1+") (pref "+z2+"))" +
				  "=>" + 
				  "(assert (Dep (id_word 1) (id_dep 0) (rel "+z4+") (word ?n))))";
		  
		  
		  String zrule23 = "(defrule ���_�����2 " + 
				    "(Word (id ?i)(name ?n) (type "+l8+") (tense "+z16+"))" +
			        "(Word (id ?i1)(name ?n1) (type "+z1+") )"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "=>" + 
			        "(assert (Dep (id_word ?i1) (id_dep ?i) (rel "+l14+") (word ?n1)))"+
		            "(assert (Dep (id_word ?i) (id_dep ?i) (rel "+z29+") (word ?n))))";
		  
		  String zrule24 = "(defrule ��_��������3 " + 
				    "(declare (salience 1000))"+
				    "(Word (id ?i)(name ?n) (type "+z1+")(tense ?o) (pref "+z2+") (suff ?x))" +
				    "(Dep (id_word ?i1) (id_dep ?i) (rel "+z17+"))"+
				    "(test  (?o contains ��� ))" +
				    "(test  (?o contains ���� ))" +
				    "(test  (?o contains ������� ))" +
				   // "(?id <-(rule is chang))"+
			        "=>" + 
			     
			        //"(?f1 <-(rule is chang))"+
			        "(retract 5))";
			        
		  
		  String zrule25 = "(defrule ��_��������4 " + 
				    "(declare (salience -100))"+
				    "(Word (id ?i)(name ?n) (type "+z1+")(tense ?o) (pref "+z2+") (suff ?x))" +
				    "(test  (?o contains ��� ))" +
				    "(test  (?o contains ���� ))" +
				    "(test  (?o contains ������� ))" +
				    "(proun (member1 ?m)(m ?h))"+
				    "(test (not(?h contains ?x)))"+
				    "(test (?m contains (sub-string 2  (str-length ?x) ?x)))"+
			        "=>" + 
			        "(assert (Dep (id_word ?i) (id_dep ?i) (rel "+l15+") (word (sub-string 3  (str-length ?x) ?x)))))";
		 
		  
		  String zrule26 = "(defrule ���_����� " + 
				    "(Word (id ?i)(name ?n) (type "+l8+") (tense "+z18+"))" +
			        "(Word (id ?i1)(name ?n1) (type "+z1+") )"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "=>" + 
			        "(assert (Dep (id_word ?i1) (id_dep (- ?i 1)) (rel "+z19+") (word ?n1))))";
		  
		  String zrule27 = "(defrule ��� " + 
				    "(Word (id ?i)(name ?n) (type ?t) (pref ?p)(suff ?s))" +
			        "(Word (id ?i1)(name ?n1) (type "+z1+")(tense ?t3)(pref ?p1)(suff ?s1) )"+
			        "(test  (?t contains ��� ))" +
			        "(test  (?t3 contains ��� ))" +
			        "(test (not(?t contains ����� )))" +
			        "(not(Dep (id_word ?i3 &:(= ?i1 ?i3 )) (id_dep ?i2) (rel ?r )))" +
			        " (test (= ?i1(+ ?i 1)) )"+
			        " (test (eq ?p ?p1) )"+
			        " (test (eq ?s ?s1) )"+
			        "=>" + 
			        "(assert (Dep (id_word ?i1) (id_dep ?i) (rel "+l11+") (word ?n1))))";
		  
		  String zrule28 = "(defrule ���_����_������ " + 
				    "(Word (id 0)(name ?n) (type "+l8+") (tense ?p))" +
			        "(Word (id 1)(name ?n1) (type "+z1+")(pref "+z+"))"+
			        "(Word (id 2)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "(test  (?p contains ����� ))" +
			        "=>" + 
			        "(assert (Dep (id_word 1) (id_dep 0) (rel "+z20+") (word ?n1)))"+
			        "(assert (Dep (id_word 2) (id_dep 0) (rel "+z21+") (word ?n2))))";
		  
		  
		
		  String zrule30 = "(defrule ���_����_������2 " + 
				    "(Word (id 0)(name ?n) (type "+l8+") (tense ?p))" +
			        "(Word (id 1)(name ?n1) (type "+z1+")(pref "+z2+"))"+
			        "(Word (id 2)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "(Dep (id_word 1) (id_dep 1) (rel "+l15+"))"+
			        "(test  (?p contains ����� ))" +
			        "=>" + 
			        "(assert (Dep (id_word 1) (id_dep 0) (rel "+z20+") (word ?n1)))"+
			        "(assert (Dep (id_word 2) (id_dep 0) (rel "+z21+") (word ?n2))))";
		  
		  
		  String zrule44 = "(defrule ���_����_������6 " + 
				    "(Word (id 0)(name ?n) (tense ?t ))" +
			        "(Word (id 1)(name ?n1) (type "+z1+")(pref "+z2+"))"+
			        "(Word (id 2)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "(Dep (id_word 1) (id_dep 1) (rel "+l15+"))"+
			        "(test  (or(?n contains ��� ) (?t contains ����� )))" +
			        "=>" + 
			        "(assert (Dep (id_word 1) (id_dep 0) (rel "+z20+") (word ?n1)))"+
			        "(assert (Dep (id_word 2) (id_dep 0) (rel "+z21+") (word ?n2))))";
		  
		  
		  String zrule61 = "(defrule ������_���� " + 
				    "(declare (salience 10))"+
				    "(Word (id ?i)(name ?n) (type "+z1+")  (tense ?t))"+
				    "(Word (id ?i1)(name ?n1) (type ?y)  (tense ?t1)(pref "+z2+"))"+
				    " (test (= ?i1(- ?i 1)) )"+
				    "(test  (?t contains ����� ))" +
				    "(test (not (?t1 contains ��� )))" +
				    "(test (not (?t1 contains ����� )))" +
				    "(test (not (?t1 contains �� )))" +
				    "(test (not (?t contains ��� )))" +
			        "=>" + 
			        "(assert (Dep (id_word ?i) (id_dep ?i1) (rel "+z6+") (word ?n))))";
		  
		  
		  String zrule65 = "(defrule ��������� " +  
			        "(Word (id 0)(name ?n)  (tense ?e))"+
				    "(or(test  (or(?e contains �������) (?e contains ������  )))(name of prson found at start) )"+
			        "(Word (id 1)(name ?n1) (type "+z1+")(pref "+z2+"))"+
			        "(Dep (id_word 1) (id_dep 1) (rel "+l15+") )" +  
			        "(Word (id 2)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "=>" + 
			        "(assert (Dep (id_word 2) (id_dep 1) (rel "+z4+") (word ?n2)))"+
	 	            "(assert (Dep (id_word 1) (id_dep 1) (rel "+z42+") (word ?n1))))";
		  
		    
		  String zrule67 = "(defrule ���_�����2 " + 
				  "(Word (id ?i)(name ?n) (type "+z1+") (tense ?t)(pref ?p))" +
				  "(test (?t contains ����� ))"+
				  "(test (or (or(?p contains � ) (?p contains � )) (?p contains � )))"+
			      "=>" + 
			      "(assert (Dep (id_word ?i) (id_dep ?i) (rel "+l14+") (word ?n))))";	    
		  
		  String zrule69 = "(defrule ���������1 " +  
				    "(Dep (id_word 0) (id_dep 0) (rel "+z3+") )"+
			        "(Word (id 2)(name ?n1) (type "+z1+")(pref "+z2+"))"+
			        "(Dep (id_word 2) (id_dep 2) (rel "+l15+") )" +  
			        "(Word (id 3)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "=>" + 
			        "(assert (Dep (id_word 3) (id_dep 2) (rel "+z4+") (word ?n2)))"+
	 	            "(assert (Dep (id_word 2) (id_dep 2) (rel "+z42+") (word ?n1))))";
		  
	
		  String zrule66 = "(defrule ���������1 " +  
				   "(declare (salience 10))"+
			        "(Word (id 0)(name ?n) (type "+z1+")(pref "+z+"))"+
			        "(Word (id 1)(name ?n1) (type "+z1+")(pref "+z2+"))"+ 
			        "(Word (id 2)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "(Dep (id_word 2) (id_dep 2) (rel "+l15+") )" + 
			        "(Word (id 3)(name ?n3) (type "+z1+")(pref "+z2+"))"+
			        "=>" + 
			        "(assert (Dep (id_word 3) (id_dep 1) (rel "+z4+") (word ?n3)))"+
	 	            "(assert (Dep (id_word 1) (id_dep 1) (rel "+z42+") (word ?n1)))"+
		            "(assert (Dep (id_word 2) (id_dep 1) (rel "+z6+") (word ?n2))))";
		  
		  String zrule68 = "(defrule �����_����1 " +  
				   "(declare (salience 10))"+
			        "(Word (id 0)(name ?n) )"+
			        "(test  (?n contains ���� ))" +
			        "(Word (id 1)(name ?n1) (type "+z1+")(pref "+z2+"))"+ 
			        "=>" + 
			        "(assert (Dep (id_word 0) (id_dep 0) (rel "+z45+") (word ?n)))"+
			        "(assert (Dep (id_word 1) (id_dep 1) (rel "+z44+") (word ?n1))))";
		  
		  String zrule70 = "(defrule ���_����_������ " + 
				    "(declare (salience 10))"+
				    "(Word (id 0)(name ?n) (tense ?t))" +
			        "(Word (id 1)(name ?n1)  (tense ?t1))"+
			        "(or(test  (or(?t1 contains �������) (?t1 contains ������  )))(name of prson found at second) )"+
			        "(Word (id 2)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "(test (or (?t contains ����� )(?n contains ��� )))" +
			        "=>" + 
			        "(assert (Dep (id_word 1) (id_dep 0) (rel "+z20+") (word ?n1)))"+
			        "(assert (Dep (id_word 2) (id_dep 1) (rel "+z21+") (word ?n2))))";
	 	           
		  
		  String zrule71 = "(defrule 1���_����_������ " + 
				    "(declare (salience 10))"+
				    "(Word (id 0)(name ?n) (tense ?t))" +
				    "(Word (id 1)(name ?n1) (type "+z1+")(pref "+z2+"))"+
			        "(Word (id 3)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "(Dep (id_word 2) (id_dep 1) (rel "+z17+"))"+
			        "(test (or (?t contains ����� )(?n contains ��� )))" +
			        "=>" + 
			        "(assert (Dep (id_word 1) (id_dep 0) (rel "+z20+") (word ?n1)))"+
			        "(assert (Dep (id_word 3) (id_dep 1) (rel "+z21+") (word ?n2))))";
		  
		  String zrule72 = "(defrule 2���_����_������ " + 
				    "(declare (salience 10))"+
				    "(Word (id 0)(name ?n) (tense ?t))" +
				    "(Word (id 1)(name ?n1) (type "+z1+")(tense ?t1))"+
				    "(test  (?t1 contains ����� ))" +
			        "(Word (id 3)(name ?n2) (type "+z1+")(pref "+z2+"))"+
			        "(test (or (?t contains ����� )(?n contains ��� )))" +
			        "=>" + 
			        "(assert (Dep (id_word 1) (id_dep 0) (rel "+z20+") (word ?n1)))"+
			        "(assert (Dep (id_word 3) (id_dep 1) (rel "+z21+") (word ?n2))))";
		  
		  
		 
		  
		  //endregion
		//---------------------------------------------------
		  //region Correct2
		  
		  String zrule31 = "(defrule �����_�������_�_����� " + 
				    "(declare (salience -2))"+  
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z3+"))" +
			        "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z4+"))"+
			        "(Word (id ?i4)(name ?n1) (tense ?t))"+
			        "(Word (id ?i5)(name ?n2) (tense ?t1))"+
			        "(test  (= ?i ?i3 ))" +
			        "(test  (= ?i ?i4 ))" +
			        "(test  (= ?i2 ?i5 ))" +
			        "(test  (?t contains ���� ))" +
			        "(test  (?t1 contains ���� ))" +
			        "=>" + 
			        "(assert (Err (word1 ?n2) (corr1 "+z22+") )))";
			       
		  String zrule32 = "(defrule �����_�������_�_�����1 " + 
				   "(declare (salience -2))"+
				   "(Dep (id_word ?i) (id_dep ?i1) (rel "+z3+"))" +
			        "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z4+"))"+
			        "(Word (id ?i4)(name ?n1) (tense ?t))"+
			        "(Word (id ?i5)(name ?n2) (tense ?t1))"+
			        "(test  (= ?i ?i3 ))" +
			        "(test  (= ?i ?i4 ))" +
			        "(test  (= ?i2 ?i5 ))" +
			        "(test  (?t contains ���� ))" +
			        "(test ( not(?t1 contains ���� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n2) (corr1 "+z23+") )))";
		  
		  String zrule33 = "(defrule �����_�������_�_�����2 " +
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z3+"))" +
			        "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z4+"))"+
			        "(Word (id ?i4)(name ?n1) (tense ?t))"+
			        "(Word (id ?i5)(name ?n2) (tense ?t1))"+
			        "(test  (= ?i ?i3 ))" +
			        "(test  (= ?i ?i4 ))" +
			        "(test  (= ?i2 ?i5 ))" +
			        "(test  (?t contains ���� ))" +
			        "(test ( not(?t1 contains ���� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n2) (corr1 "+z24+") )))";
		  
		  String zrule34 = "(defrule �����_�������_�_�����3 " + 
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z3+"))" +
			        "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z4+"))"+
			        "(Word (id ?i4)(name ?n1) (tense ?t))"+
			        "(Word (id ?i5)(name ?n2) (tense ?t1))"+
			        "(test  (= ?i ?i3 ))" +
			        "(test  (= ?i ?i4 ))" +
			        "(test  (= ?i2 ?i5 ))" +
			        "(test  (?t contains ��� ))" +
			        "(test ( not(?t1 contains ��� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n2) (corr1 "+z25+") )))";
			       
		  String zrule35 = "(defrule �����_�������_�_�����4 " +
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z3+"))" +
			        "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z4+"))"+
			        "(Word (id ?i4)(name ?n1) (tense ?t))"+
			        "(Word (id ?i5)(name ?n2) (tense ?t1))"+
			        "(test  (= ?i ?i3 ))" +
			        "(test  (= ?i ?i4 ))" +
			        "(test  (= ?i2 ?i5 ))" +
			        "(test  (?t contains ���� ))" +
			        "(test ( not(?t1 contains ���� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n2) (corr1 "+z26+") )))";
		  
		  String zrule36 = "(defrule ����� " + 
				  "(declare (salience 100))"+
					"?id <- (Word (type "+z1+") (tense ?te &:(?te contains ���� )) (suff "+z27+") )"+
					"=>" +
					"(bind ?su  "+ z28 +" )"+
					"(modify ?id (suff ?su)))\n";
			
		  
		  String zrule37 = "(defrule �����_�����_������� " + 
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+l14+") )" +
			        "(Word (id ?i4)(name ?n1) (tense ?t)(suff ?s))"+
			        "(test  (= ?i ?i4 ))" +
			        "(test  (or(?t contains ���� )(?t contains ��� )))" +
			        "(test  (?t contains ���� ))" +
			        "(test  (not(?s contains �� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z30+") )))";
		  
		  String zrule38 = "(defrule �����_�����_�������1 " +
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z6+") )" +
			        "(Word (id ?i4)(name ?n1) (tense ?t)(suff ?s))"+
			        "(test  (= ?i ?i4 ))" +
			        "(test  (or(?t contains ���� )(?t contains ��� )))" +
			        "(test  (?t contains ���� ))" +
			        "(test  (not(?s contains �� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z30+") )))";
		  
		  String zrule39 = "(defrule �����_�����_�������2 " + 
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+l14+") )" +
				    "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z19+") )" +
			        "(Word (id ?i4)(name ?n) (tense ?t)(suff ?s))"+
			        "(Word (id ?i5)(name ?n1) (tense ?t1)(suff ?s1))"+
			        "(test  (= ?i ?i3 ))" +
			        "(test  (= ?i ?i4 ))" +
			        "(test  (= ?i2 ?i5 ))" +
			        "(test  (or(?t contains ���� )(?t contains ��� )))" +
			        "(test  (or(?t1 contains ���� )(?t1 contains ��� )))" +
			        "(test  (?t contains ���� ))" +
			        "(test  (?t1 contains ���� ))" +
			        "(test  (not(?s1 contains �� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z30+") )))";
		  
		  
		  String zrule40 = "(defrule �����_�����_�������3 " + 
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z6+") )" +
				    "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z19+") )" +
			        "(Word (id ?i4)(name ?n) (tense ?t)(suff ?s))"+
			        "(Word (id ?i5)(name ?n1) (tense ?t1)(suff ?s1))"+
			        "(test  (= ?i ?i3 ))" +
			        "(test  (= ?i ?i4 ))" +
			        "(test  (= ?i2 ?i5 ))" +
			        "(test  (or(?t contains ���� )(?t contains ��� )))" +
			        "(test  (or(?t1 contains ���� )(?t1 contains ��� )))" +
			        "(test  (?t contains ���� ))" +
			        "(test  (?t1 contains ���� ))" +
			        "(test  (not(?s1 contains �� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z30+") )))";
		  
		  String zrule41 = "(defrule �����_�����_�������1 " + 
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z20+") )" +
			        "(Word (id ?i4)(name ?n1) (tense ?t)(suff ?s))"+
			        "(test  (= ?i ?i4 ))" +
			        "(test  (or(?t contains ���� )(?t contains ��� )))" +
			        "(test  (?t contains ���� ))" +
			        "(test  (not(?s contains �� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z31+") )))";
		  		  
		  String zrule45 = "(defrule �����_�����_������� " +
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel ?r) )" +
			        "(Word (id ?i2)(name ?n1) (tense ?t)(suff ?s))"+
			        "(test  (= ?i ?i2 ))" +
			        "(test  (?r contains ����� ))" +
			        "(test  (?t contains ���� ))" +
			        "(test  (?t contains ���� ))" +
			        "(test  (not(?s contains �� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z32+") )))";
		  
	
//		  String zrule46 = "(defrule 1�����_�����_������� " + 
//				   "(declare (salience -2))"+  
//				   "(Dep (id_word ?i) (id_dep ?i1) (rel ?r) )" +
//			        "(Word (id ?i2)(name ?n1) (tense ?t)(suff ?s))"+
//			        "(test  (= ?i ?i2 ))" +
//			        "(test  (?r contains ����� ))" +
//			        "(test  (?t contains ��� ))" +
//			        "(test  (?t contains ���� ))" +
//			        "(test  (not(?s contains �� )))" +
//			        "=>" + 
//			        "(assert (Err (word1 ?n1) (corr1 "+z33+") )))";
//		  
		  
		  String zrule47 = "(defrule �����_�����_�������2 " + 
				   "(declare (salience -2))"+  
				   "(Dep (id_word ?i) (id_dep ?i1) (rel ?r) )" +
				    "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z19+") )" +
			        "(Word (id ?i4)(name ?n) (tense ?t)(suff ?s))"+
			        "(Word (id ?i5)(name ?n1) (tense ?t1)(suff ?s1))"+
			        "(test  (= ?i ?i3 ))" +
			        "(test  (= ?i ?i4 ))" +
			        "(test  (= ?i2 ?i5 ))" +
			        "(test  (?r contains ����� ))" +
			        "(test  (or(?t1 contains ���� )))" +
			        "(test  (?t1 contains ���� ))" +
			        "(test  (not(?s1 contains �� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z32+") )))";
		  
//		  String zrule48 = "(defrule �����_�����_�������3 " + 
//				    "(declare (salience -2))"+
//				    "(Dep (id_word ?i) (id_dep ?i1) (rel ?r) )" +
//				    "(Dep (id_word ?i2) (id_dep ?i3) (rel "+z19+") )" +
//			        "(Word (id ?i4)(name ?n) (tense ?t)(suff ?s))"+
//			        "(Word (id ?i5)(name ?n1) (tense ?t1)(suff ?s1))"+
//			        "(test  (= ?i ?i3 ))" +
//			        "(test  (= ?i ?i4 ))" +
//			        "(test  (= ?i2 ?i5 ))" +
//			        "(test  (?r contains ����� ))" +
//			        "(test  (or(?t1 contains ��� )))" +
//			        "(test  (?t1 contains ���� ))" +
//			        "(test  (not(?s1 contains �� )))" +
//			        "=>" + 
//			        "(assert (Err (word1 ?n1) (corr1 "+z33+") )))";
		  
		  
		  String zrule49 = "(defrule �����_�������_�������� " + 
				    "(declare (salience -2))"+
			        "(Word (id ?i)(name ?n)(type ?t) (tense ?t2))"+
			        "(Word (id ?i2)(name ?n1)(type ?t1) (tense ?t3))"+
			        " (test (= ?i2(+ ?i 1)) )"+
			        "(test  (or(?t contains ��� )(?t contains ��� )))" +
			        "(test  (or(?t2 contains ����� )(?t2 contains ���� )))" +
			        "(test  (?t2 contains �������� ))" +
			        "(test  (not(?t3 contains ���� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z23+") )))";
		  
		  String zrule50 = "(defrule �����_�������_�������� " + 
				    "(declare (salience -2))"+
			        "(Word (id ?i)(name ?n)(type ?t) (tense ?t2))"+
			        "(Word (id ?i2)(name ?n1)(type ?t1) (tense ?t3))"+
			        " (test (= ?i2(+ ?i 1)) )"+
			        "(test  (or(?t contains ��� )(?t contains ��� )))" +
			        "(test  (or(?t2 contains ����� )(?t2 contains ���� )))" +
			        "(test  (?t2 contains ���������� ))" +
			        "(test  (not(?t3 contains ���� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z22+") )))";
		  
		  
		  String zrule51 = "(defrule �����_�������_�������� " + 
				    "(declare (salience -2))"+
			        "(Word (id ?i)(name ?n)(type ?t) (tense ?t2))"+
			        "(Word (id ?i2)(name ?n1)(type ?t1) (tense ?t3))"+
			        " (test (= ?i2(+ ?i 1)) )"+
			        "(test  (or(?t contains ��� )(?t contains ��� )))" +
			        "(test  (or(?t2 contains ����� )(?t2 contains ���� )))" +
			        "(test  (?t2 contains ���������� ))" +
			        "(test  (not(?t3 contains ���� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z26+") )))";
		  
		  String zrule52 = "(defrule �����_�������_�������� " + 
				    "(declare (salience -2))"+
			        "(Word (id ?i)(name ?n)(type ?t) (tense ?t2))"+
			        "(Word (id ?i2)(name ?n1)(type ?t1) (tense ?t3))"+
			        " (test (= ?i2(+ ?i 1)) )"+
			        "(test  (or(?t contains ��� )(?t contains ��� )))" +
			        "(test  (or(?t2 contains ����� )(?t2 contains ���� )))" +
			        "(test  (?t2 contains ���������� ))" +
			        "(test  (not(?t3 contains ���� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z24+") )))";
		  
		  String zrule53 = "(defrule �����_�������_�������� " + 
				    "(declare (salience -2))"+
			        "(Word (id ?i)(name ?n)(type ?t) (tense ?t2))"+
			        "(Word (id ?i2)(name ?n1)(type ?t1) (tense ?t3))"+
			        " (test (= ?i2(+ ?i 1)) )"+
			        "(test  (or(?t contains ��� )(?t contains ��� )))" +
			        "(test  (or(?t2 contains ����� )(?t2 contains ���� )))" +
			        "(test  (?t2 contains ����� ))" +
			        "(test  (not(?t3 contains ��� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z25+") )))";
		  
		  String zrule54 = "(defrule �����_����� " + 
				    "(declare (salience -2))"+
				    "(Word (id ?i)(name ?n) (type "+z1+") (tense ?t2) (pref ?p)(suff ?s))"+
			        "(Word (id ?i1)(name ?n1) (type "+z1+") (tense ?t3)(pref ?p1)(suff ?s1))"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "(not(Dep (id_word ?i3 &:(= ?i1 ?i3 )) (id_dep ?i2) (rel "+z21+" )))" +
			        "(test  (?t3 contains ��� ))" +
			        "(test  (?t2 contains ���� ))" +
			        "(test  (not(eq ?p ?p1)) )" +
			        "(test  (eq ?s ?s1) )" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z34+") )))";
		  
		  String zrule55 = "(defrule �����_����� " + 
				    "(declare (salience -2))"+
				    "(Word (id ?i)(name ?n) (type "+z1+") (tense ?t2) (pref ?p)(suff ?s))"+
			        "(Word (id ?i1)(name ?n1) (type "+z1+") (tense ?t3)(pref ?p1)(suff ?s1))"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "(not(Dep (id_word ?i3 &:(= ?i1 ?i3 )) (id_dep ?i2) (rel "+z4+" )))" +
			        "(test  (?t3 contains ��� ))" +
			        "(test  (?t2 contains ���� ))" +
			        "(test (not(?t3 contains ���� )))" +
			        "(not (Dep (id_word ?i1) (id_dep ?i1) (rel "+l15+") ))"+
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z35+") )))";
		  
		  
		  String zrule56 = "(defrule �����_����� " + 
				    "(declare (salience -2))"+
				    "(Word (id ?i)(name ?n) (type "+z1+") (tense ?t2) (pref ?p)(suff ?s))"+
			        "(Word (id ?i1)(name ?n1) (type "+z1+") (tense ?t3)(pref ?p1)(suff ?s1))"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "(not(Dep (id_word ?i3 &:(= ?i1 ?i3 )) (id_dep ?i2) (rel "+z4+" )))" +
			        "(test  (?t3 contains ��� ))" +
			        "(test  (?t2 contains ���� ))" +
			        "(test (not(?t3 contains ���� )))" +
			        "(not (Dep (id_word ?i1) (id_dep ?i1) (rel "+l15+") ))"+
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z35+") )))";
		  
		  String zrule57 = "(defrule �����_����� " + 
				    "(declare (salience -2))"+
				    "(Word (id ?i)(name ?n) (type "+z1+") (tense ?t2) (pref ?p)(suff ?s))"+
			        "(Word (id ?i1)(name ?n1) (type "+z1+") (tense ?t3)(pref ?p1)(suff ?s1))"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "(not(Dep (id_word ?i3 &:(= ?i1 ?i3 )) (id_dep ?i2) (rel "+z4+" )))" +
			        "(test  (?t3 contains ��� ))" +
			        "(test  (?t2 contains ��� ))" +
			        "(test (not(?t3 contains ��� )))" +
			        "(not (Dep (id_word ?i1) (id_dep ?i1) (rel "+l15+") ))"+
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z35+") )))";
		  
		  String zrule58 = "(defrule �����_����� " + 
				    "(declare (salience -2))"+
				    "(Word (id ?i)(name ?n) (type "+z1+") (tense ?t2) (pref ?p)(suff ?s))"+
			        "(Word (id ?i1)(name ?n1) (type "+z1+") (tense ?t3)(pref ?p1)(suff ?s1))"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "(not(Dep (id_word ?i3 &:(= ?i1 ?i3 )) (id_dep ?i2) (rel "+z4+" )))" +
			        "(test  (?t3 contains ��� ))" +
			        "(test  (?t2 contains ���� ))" +
			        "(test (not(?t3 contains ���� )))" +
			        "(not (Dep (id_word ?i1) (id_dep ?i1) (rel "+l15+") ))"+
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z36+") )))";
		  
		  String zrule59 = "(defrule �����_����� " + 
				    "(declare (salience -2))"+
				    "(Word (id ?i)(name ?n) (type "+z1+") (tense ?t2) (pref ?p)(suff ?s))"+
			        "(Word (id ?i1)(name ?n1) (type "+z1+") (tense ?t3)(pref ?p1)(suff ?s1))"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "(not(Dep (id_word ?i3 &:(= ?i1 ?i3 )) (id_dep ?i2) (rel "+z4+" )))" +
			        "(test  (?t3 contains ��� ))" +
			        "(test  (?t2 contains ���� ))" +
			        "(test (not(?t3 contains ���� )))" +
			        "(not (Dep (id_word ?i1) (id_dep ?i1) (rel "+l15+") ))"+
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z36+") )))";
		  
		  String zrule60 = "(defrule �������_����� " + 
				    "(declare (salience -2))"+
				    "(Word (id 0)(name ?n) (type "+z1+")(tense ?t1)  (pref "+z2+"))"+
			        "(not(Dep (id_word 0) (id_dep 0) (rel "+z3+" )))" +
			        "(test (not(?n contains ���� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n) (corr1 "+z37+") )))";
		  
		 
		  String zrule62 = "(defrule ������_���� " +
				    "(declare (salience -2))"+
				    "(Word (id ?i)(name ?n) (type "+l8+")  (tense ?t))"+
				    "(Word (id ?i1)(name ?n1) (type "+z38+")  (tense ?t1))"+
				    " (test (= ?i1(+ ?i 1)) )"+
				    "(test  (?t contains �� ))" +
			        "=>" + 
			        "(assert (Err (word1 ?n) (corr1 "+z39+") )))";
		  
		  String zrule63 = "(defrule ���_������� " +
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z17+") )" +   
			        "(Word (id ?i4)(name ?n) (last ?t)(suff ?s))"+
			        "(test  (= ?i1 ?i4 ))" +
			        "(test  (not(?s contains � )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n) (corr1 "+z40+") )))";
		  
		  String zrule64 = "(defrule �����_����� " + 
				    "(declare (salience -2))"+
				    "(Dep (id_word ?i) (id_dep ?i1) (rel "+z19+") )" +   
			        "(Word (id ?i2)(name ?n) (type "+z1+")(tense ?t))"+
			        "(Word (id ?i3)(name ?n1) (type "+z1+")(tense ?t1))"+
			        "(test  (= ?i1 ?i2 ))" +
			        "(test  (= ?i ?i3 ))" +
			        "(test  (?t contains ����� ))" +
			        "(test  (not(?t1 contains ����� )))" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z41+") )))";
		  
		  
		 
		  String zrule73 = "(defrule �����_����� " + 
				    "(declare (salience -2))"+
				    "(Word (id ?i)(name ?n) (type "+z1+") (tense ?t2) (pref ?p)(suff ?s))"+
			        "(Word (id ?i1)(name ?n1) (type "+z1+") (tense ?t3)(pref ?p1)(suff ?s1))"+
			        " (test (= ?i1(+ ?i 1)) )"+
			        "(not(Dep (id_word ?i3 &:(= ?i1 ?i3 )) (id_dep ?i2) (rel "+z21+" )))" +
			        "(test  (?t3 contains ��� ))" +
			        "(test  (?t2 contains ���� ))" +
			        "(test  (not(eq ?p ?p1)) )" +
			        "(test  (eq ?s ?s1) )" +
			        "=>" + 
			        "(assert (Err (word1 ?n1) (corr1 "+z34+") )))";
		  
		//endregion
	 	           
		  String e ="(defglobal ?*pr* = 1) ";

		  //String e1 ="(bind $?*pr*  � �� ��  �� ��� �� ��� �� ��� �� �� ���)";
		 // String e2="(bind ?x (sub-string 2  (str-length hello) hello))";
		  
		  engine.executeCommand(e); 
		  //engine.executeCommand(e2); 
		 // engine.executeCommand("(printout t ?x crlf)"); 
		 // engine.executeCommand("(assert (rule is chang))"); 
		  engine.executeCommand(zrule); 	  
		  engine.executeCommand(zrule1); 
		  engine.executeCommand(zrule2); 
 		  engine.executeCommand(zrule3); 
 		  engine.executeCommand(zrule4); 
 		  engine.executeCommand(zrule5); 
 		  engine.executeCommand(zrule6); 
 		  engine.executeCommand(zrule7); 
 		  engine.executeCommand(zrule8);
 		  engine.executeCommand(zrule9);
 		  engine.executeCommand(zrule10);
 		  engine.executeCommand(zrule11);
 		  engine.executeCommand(zrule12);
 		  engine.executeCommand(zrule13);
 		  engine.executeCommand(zrule14);
 		  engine.executeCommand(zrule15);
 		  engine.executeCommand(zrule16);
 		  engine.executeCommand(zrule17);
 		  engine.executeCommand(zrule18);
 		  engine.executeCommand(zrule19);
 		  engine.executeCommand(zrule20);
 		  engine.executeCommand(zrule21);
 		  engine.executeCommand(zrule22);
 		  engine.executeCommand(zrule23);
 		  engine.executeCommand(zrule24);
 		  engine.executeCommand(zrule25);
 		  engine.executeCommand(zrule26);
 		  engine.executeCommand(zrule27);
 		 // engine.executeCommand(zrule28);
 		 // engine.executeCommand(zrule29);
 		 // engine.executeCommand(zrule30);
 		  engine.executeCommand(zrule31);
 		  engine.executeCommand(zrule32);
 		  engine.executeCommand(zrule33);
 		  engine.executeCommand(zrule34);
 		  engine.executeCommand(zrule35);
 		  engine.executeCommand(zrule36);
 		  engine.executeCommand(zrule37);
 		  engine.executeCommand(zrule38);
 		  engine.executeCommand(zrule39);
 		  engine.executeCommand(zrule40);
 		  engine.executeCommand(zrule41);
 		 // engine.executeCommand(zrule42);
 		//  engine.executeCommand(zrule43);
 		  engine.executeCommand(zrule44);
 		  engine.executeCommand(zrule45);
// 		  engine.executeCommand(zrule46);
 		  engine.executeCommand(zrule47);
// 		  engine.executeCommand(zrule48);
 		  engine.executeCommand(zrule49);
 		  engine.executeCommand(zrule50);
 		  engine.executeCommand(zrule51);
 		  engine.executeCommand(zrule52);
 		  engine.executeCommand(zrule53);
 		 // engine.executeCommand(zrule54);
 		  engine.executeCommand(zrule55);
 		  engine.executeCommand(zrule56);
 		  engine.executeCommand(zrule57);
 		  engine.executeCommand(zrule58);
 		  engine.executeCommand(zrule59);
 		  engine.executeCommand(zrule60);
 		  engine.executeCommand(zrule61);
 		  engine.executeCommand(zrule62);
 		 // engine.executeCommand(zrule63);
 	      engine.executeCommand(zrule64);
 	      engine.executeCommand(zrule69);
 	      engine.executeCommand(zrule65);
 	      engine.executeCommand(zrule66);
 	      engine.executeCommand(zrule67);
 	      engine.executeCommand(zrule68);
 	     // engine.executeCommand(zrule69);
 	      engine.executeCommand(zrule70);
 	      engine.executeCommand(zrule71);
 	      engine.executeCommand(zrule72);
 	      engine.executeCommand(zrule73);
		 // Value sumValue = engine.fetch("ss");
           
		// s = sumValue.stringValue(engine.getGlobalContext()); 
		//  System.out.println(t7.toString().contains("�")); 
		//endregion
		
		  
//		engine.executeCommand("(watch activations)");
		engine.executeCommand(rule);
		engine.executeCommand("(run)");
		engine.executeCommand("(facts)");
		Iterator sumValue = engine.listFacts();
		ArrayList<Fact> fs=new ArrayList<Fact>(); 
		ArrayList<Fact> fs1=new ArrayList<Fact>(); 
         String result="";
		while (sumValue.hasNext())
		{
			Fact i = (Fact)sumValue.next();
			if (i.getName().contains("Err"))
			{
				fs.add(i);
				System.out.println(fs.get(0).getSlotValue("word1")+" , " + fs.get(0).getSlotValue("word2")+" , " +fs.get(0).getSlotValue("corr1")+" , " +fs.get(0).getSlotValue("corr2"));
                result=fs.get(0).getSlotValue("word1")+" , " + fs.get(0).getSlotValue("word2")+" , " +fs.get(0).getSlotValue("corr1")+" , " +fs.get(0).getSlotValue("corr2");
               // return result;
			}
			else 
			{ fs1.add(i);
             result="������ �����";
             
			}
		}
		return result;
		
	}
	
		
	}



