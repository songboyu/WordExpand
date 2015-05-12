package com.test;

import java.io.IOException;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;

public class Test_chineseFan2Jan {
	public static void main(String[] args) throws IOException {
		String fanText="中華人民共和國";
		//获得繁体-简体转换器
		ChineseJF chinesdJF = CJFBeanFactory.getChineseJF();
		
		String janText = chinesdJF.chineseFan2Jan(fanText);
		
		System.out.println("繁体：\n"+fanText+"\n转换后简体：\n"+janText);
	}
}
	