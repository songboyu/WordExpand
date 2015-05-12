package com.test;

import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;


public class Test_isNounWords {
	public static boolean isNounWords(String s){
		boolean isNoun = true;
		List<Term> parse = ToAnalysis.parse(s);
		System.out.println(parse);
		for(Term t : parse){
			if(!t.getNatureStr().contains("n") 
					&& !t.getNatureStr().equals("j"))
				isNoun = false;
		}
		return isNoun==true? true:false;
	}
	public static void main(String[] args) {
		System.out.println(isNounWords("QQ炫舞"));
	}
}
