package com.saison.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpOutputMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.saison.dao.AdminDao;

@Controller
public class SaisonController {

	@Autowired
	AdminDao adminDao;
	final String uri = "https://lookup.binlist.net/";

	@RequestMapping("/test")
	public void abc(HttpServletRequest reques, HttpServletResponse response) {
		System.out.println("running");
	}

	@RequestMapping("/verify/{number}")
	public void verifyCardnumber(HttpServletRequest request, HttpServletResponse response,
			@PathVariable(value = "number") String id) throws IOException {
		String url = uri + id;
		RestTemplate restTemplate = new RestTemplate();
		String result = restTemplate.getForObject(url, String.class);

		System.out.println("id:" + id + "  result:" + result);

		JSONObject updateDb = adminDao.saveCard(result, id);
		JSONObject payload = new JSONObject();
		payload.put("scheme", updateDb.get("scheme"));
		payload.put("type", updateDb.get("type"));
		payload.put("bank", updateDb.get("bank"));

		JSONObject responseObj = new JSONObject();

		if (updateDb.get("status").equals(1)) {
			responseObj.put("success", true);
			responseObj.put("payload", payload);

		} else {
			responseObj.put("success", false);
		}
		response.setContentType("application/json");
		PrintWriter pw = response.getWriter();
		pw.print(responseObj);
		pw.flush();
		pw.close();
	}

	@RequestMapping("stats")
	public void showStats(HttpServletRequest request, HttpServletResponse response, @RequestParam Integer start,
			@RequestParam Integer limit) throws IOException {
		System.out.println(limit);
		System.out.println(start);
		int totalStats = adminDao.totalStats();
		JSONObject getData = adminDao.getStats(start, limit);
		JSONObject responseObj = new JSONObject();

		if (getData.get("status").equals(1)) {
			responseObj.put("success", true);
			responseObj.put("start", start);
			responseObj.put("limit", limit);
			responseObj.put("size", totalStats);
			responseObj.put("payload", getData.get("payload"));

		} else {
			responseObj.put("success", false);
		}
		response.setContentType("application/json");
		PrintWriter pw = response.getWriter();
		pw.print(responseObj);
		pw.flush();
		pw.close();
	}
}
