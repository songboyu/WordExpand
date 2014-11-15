package com.baike;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @ClassName: Explain
 * @Description: Explain Word
 * @author leo108
 * @date 2011-11-1 09:25:04
 */
public class fetch01 {
	private static String newsurl;
	public static void main(String[] args) {
		String result = getExplain("法布雷加斯");
		System.out.println(result);
	}
	private static String getUrl(String word) {
		newsurl = "";
		Pattern pattern = Pattern.compile("<font size=\"3\"><a href=\"(.*)\" target=\"_blank\">",
				Pattern.CASE_INSENSITIVE);
		URLConnection uc;
		try {
			URL url = new URL(
					"http://baike.baidu.com/w?ct=17&lm=0&tn=baiduWikiSearch&pn=0&rn=10&word="
							+ word + "&submit=search");
			uc = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					uc.getInputStream())); // 获取源文件
			while (true) {
				String temp = br.readLine();
				if (temp == null)
				{
					break;
				}
				Matcher matcher = pattern.matcher(temp);
				if (matcher.find()) {
					newsurl = "http://baike.baidu.com"
							+ matcher.group(1);
					break;
				}
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newsurl;
	}

	public static String getExplain(String word) {
		String str="";
		try {
			str = URLEncoder.encode(word, "GBK");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(str.equals(""))
		{
			return "操作失败";
		}
		String urlstr = getUrl(str);
		String content = "";
		if (urlstr.equals("")) {
			return "未找到对应词条";
		}
		Pattern pattern = Pattern.compile("<div class=\"card-summary-content\">([\\s\\S]+?)</p>",
				Pattern.MULTILINE); //此处为关键正则表达式，各位可以自行修改
		URLConnection uc;
		String c = "";
		try {
			URL url = new URL(urlstr);
			uc = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					uc.getInputStream(),"gbk")); // 获取源文件
			while (true) {
				String temp = br.readLine();
				if (temp == null)
				{
					break;
				}
				c += temp;
			}
			br.close();
			
			Matcher matcher = pattern.matcher(c);
			if (matcher.find()) {
				content = matcher.group();
			}
			content = content.replaceAll("<.+?>", "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
}