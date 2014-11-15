package com.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONSerializer;

import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.log4j.Logger;

import com.seal.expand.Entity;
import com.seal.expand.EntityList;
import com.seal.expand.Seal;
import com.seal.util.Helper;

@SuppressWarnings("serial")
public class ExpandServlet extends HttpServlet {
	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		ToAnalysis.parse("Hello");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}

	private void process(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");

		String input = req.getParameter("input");
		String[] querySeeds = input.split("\n");
		PrintWriter out = resp.getWriter();
		out.print(JSONSerializer.toJSON(newWordsToMap(querySeeds)));
	}

	private static Map<String, List<String>> newWordsToMap(String[] querySeeds) {
		Logger log = Logger.getLogger(Seal.class);
		File seedFile = new File(querySeeds[0]);
		String[] seedArr;
		String hint = null;
		if (seedFile.exists()) {
			seedArr = Helper.readFile(seedFile).split("\n");
			if (querySeeds.length >= 2) {
				File hintFile = Helper.toFileOrDie(querySeeds[1]);
				hint = Helper.readFile(hintFile).replaceAll("[\r\n]+", " ");
			}
		} else {
			for (int i = 0; i < querySeeds.length; i++)
				querySeeds[i] = querySeeds[i].replace('_', ' ');
			seedArr = querySeeds;
		}
		EntityList seeds = new EntityList();
		for (String s : seedArr) {
			seeds.add(Entity.parseEntity(s));
		}
		Seal seal = new Seal();
		seal.expand(seeds, seeds, hint);
		seal.save();//Results();

		log.info(seal.getEntityList().toDetails(100, seal.getFeature()));

		List<String> value = new ArrayList<String>();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (Entity n : seal.getEntities()) {
			String tmp = n.getName().toString();
			if(n.getScore()>0.00)
				value.add(tmp);
		}
		map.put("ExpandWords", value);
		return map;
	} 
}
