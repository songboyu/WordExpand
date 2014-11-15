package com.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test01 {
	public static void main(String[] args) {
		int count = 0;
		String regEx = "[\\u4e00-\\u9fa5]";
		//System.out.println(regEx); 
		String str = "中文含有非汉字";
		//System.out.println(str); 
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		while (m.find()) {
			for (int i = 0; i <= m.groupCount(); i++) {
				count = count + 1;
			}
		}
		if(count==str.length())
			System.out.println("都是汉字");
		else
			System.out.println("含有非汉字");
	}
}
