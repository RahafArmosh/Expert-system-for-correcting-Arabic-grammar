package com.alp.webservice;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import alp.Alp;
import safar.basic.morphology.analyzer.factory.MorphologyAnalyzerFactory;
import safar.basic.morphology.analyzer.interfaces.IMorphologyAnalyzer;
import safar.basic.morphology.analyzer.model.MorphologyAnalysis;
import safar.basic.morphology.analyzer.model.NounMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.ParticleMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.VerbMorphologyAnalysis;
import safar.basic.morphology.analyzer.model.WordMorphologyAnalysis;
import java.util.List;

public class Main2 {
public String test(String c) throws UnsupportedEncodingException{
	  String c1="";
	  String t="";

      Alp alp = new Alp();
      
   
              boolean corr = false;

            //  c = "Â„«"+" "+ "ÿ›·«‰";
              
              System.out.println(alp.posTag(c));
              c1 = alp.posTag(c).split(" ")[0].split("_")[0];
              System.out.println(c1);
              c1=c1+" ";
              if (c1.contains("ÂÌ"))
    {
              c = alp.posTag(c).split(" ")[1].split("_")[1];
              System.out.println(c);

              if (c.contains("SF"))
              {
                  t="«·ÃÊ«» ’ÕÌÕ";
                  corr = true;
                  
              }
      
              if (!corr)
              {
                  t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                 
              }
  }
      else if (c1.contains("ÂÊ")){
      boolean corr1=false;

              c = alp.posTag(c).split(" ")[1].split("_")[1];
              System.out.println(c);

              if (c.contains("SM"))
              {
                  t="«·ÃÊ«» ’ÕÌÕ";
                  corr1 = true;
                  
              }
      
              if (!corr1)
              {
                  t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                 
              }  }      
//////////////////////////////////////////////////////////////////////////////////////
         else if (c1.contains("Â‰")){

      boolean corr2=false;
              

      c = alp.posTag(c).split(" ")[1].split("_")[1];

              if (c.contains("PF")|c.contains("PI"))
              {
                  t="«·ÃÊ«» ’ÕÌÕ";
                  corr2 = true;
                  
              }
      
              if (!corr2)
              {
                  t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                 
              }      
              }
///////////////////////////////////////////////////////////////////////////////////////////////
         else if (c1.contains("Â„ ")){

boolean corr3=false;
              

 c = alp.posTag(c).split(" ")[1].split("_")[1];

              if (c.contains("PM") |c.contains("PI"))
              {
                  t="«·ÃÊ«» ’ÕÌÕ";
                  corr3 = true;
                  
              }
      
              if (!corr3)
              {
                  t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                 
              }      
         }
//////////////////////////////////////////////////////////////////////////////////////
    
         else if (c1.contains("Â„«")){

         boolean corr4=false;
              
        System.out.println(alp.posTag(c));

         c = alp.posTag(c).split(" ")[1].split("_")[1];

              if (c.contains("DM")|c.contains("DF"))
              {
                  t="«·ÃÊ«» ’ÕÌÕ";
                  corr4 = true;
                  
              }
      
              if (!corr4)
              {
                  t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                 
              } }
        ///////////////////////////////////////////////////////////////////
         else if (c1.contains("√‰  ")){

             boolean corr6=false;
                  

             c = alp.posTag(c).split(" ")[1].split("_")[1];

                  if (c.contains("SM")|c.contains("SF"))
                  {
                      t="«·ÃÊ«» ’ÕÌÕ";
                      corr6 = true;
                      
                  }
          
                  if (!corr6)
                  {
                      t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                     
                  } }
          /////////////////////////////////////////////////////////////////////////////////////
             else if (c1.contains("√‰ „«")){

             boolean corr7=false;
                  

             c = alp.posTag(c).split(" ")[1].split("_")[1];

                  if (c.contains("DM")|c.contains("DF"))
                  {
                      t="«·ÃÊ«» ’ÕÌÕ";
                      corr7 = true;
                      
                  }
          
                  if (!corr7)
                  {
                      t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                     
                  } }
             ////////////////////////////////////////////////////////////////////////
                else if (c1.contains("√‰ „ ")){

             boolean corr8=false;
                  

             c = alp.posTag(c).split(" ")[1].split("_")[1];

                  if (c.contains("PM")|c.contains("PI"))
                  {
                      t="«·ÃÊ«» ’ÕÌÕ";
                      corr8 = true;
                      
                  }
          
                  if (!corr8)
                  {
                      t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                     
                  } }
             /////////////////////////////////////////////////////////////////////////
               else if (c1.contains("√‰ ‰")){

             boolean corr9=false;
                  

             c = alp.posTag(c).split(" ")[1].split("_")[1];

                  if (c.contains("PF")|c.contains("PI"))
                  {
                      t="«·ÃÊ«» ’ÕÌÕ";
                      corr9 = true;
                      
                  }
          
                  if (!corr9)
                  {
                      t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                     
                  } }
             //////////////////////////////////////////////////////////////////
             else if (c1.contains("√‰« ")){

             boolean corr88=false;
                  

             c = alp.posTag(c).split(" ")[1].split("_")[1];

                  if (c.contains("SF")|c.contains("SM"))
                  {
                      t="«·ÃÊ«» ’ÕÌÕ";
                      corr88 = true;
                      
                  }
          
                  if (!corr88)
                  {
                      t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                     
                  } }
             //////////////////////////////////////////////////////////////
             else if (c1.contains("‰Õ‰")){

             boolean corr89=false;
                  

             c = alp.posTag(c).split(" ")[1].split("_")[1];

                  if (c.contains("PF")|c.contains("PM")|c.contains("PI"))
                  {
                      t="«·ÃÊ«» ’ÕÌÕ";
                      corr89 = true;
                      
                  }
          
                  if (!corr89)
                  {
                      t="«·ÃÊ«» Œ«ÿ∆ Ì—ÃÏ «⁄«œ… «·„Õ«Ê·…";
                     
                  } }

	return t;
	
	
}

}

