package com.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test05 {
	public static void main(String[] args) {
		String str = " href=\"//www.baidu.com/link?url=UcSqO7vCF9wLLfq1cLSp8Lj-dT3pvnJUmaUO-BeLTdK\"";
		Pattern snippetPat = Pattern.compile("(?s)href=\"(.*?)\"");
		Matcher matcher = snippetPat.matcher(str);
		System.out.println(matcher.find());
	}
}
