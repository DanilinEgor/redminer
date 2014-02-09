package redminer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.User;

public class API {
	
	private static String getFormParams(String html, String num, String rel) throws UnsupportedEncodingException {
		Document doc = Jsoup.parse(html);

		Element relform = doc.getElementById("new-relation-form");
		Elements inputElements = relform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		paramList.add("relation[relation_type]=" + URLEncoder.encode(rel, "UTF-8"));
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("relation[issue_to_id]"))
				value = num;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		StringBuilder result = new StringBuilder();
		for (String param : paramList)
			if (result.length() == 0)
				result.append(param);
			else
				result.append("&" + param);
		return result.toString();
	}
	
	private static String getFormParams1(String html) throws UnsupportedEncodingException {
		Document doc = Jsoup.parse(html);

		Element relform = doc.getElementById("issue-form");
		Elements inputElements = relform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("issue[parent_issue_id]"))
				value = "";
			if (key.equals("issue[is_private]"))
				value = "0";
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		StringBuilder result = new StringBuilder();
		for (String param : paramList)
			if (result.length() == 0)
				result.append(param);
			else
				result.append("&" + param);
		return result.toString();
	}
	
	private static String GetPageContent(String apiAccessKey, String login, String password, String url, HttpURLConnection conn) throws Exception {
		URL obj = new URL(url);
		conn = (HttpURLConnection) obj.openConnection();

		conn.setRequestMethod("GET");
		String encoding = null;
		if (login != "")
			encoding = Base64.encodeBase64String(apiAccessKey.getBytes());
		else
			encoding = Base64.encodeBase64String((login+":"+password).getBytes());
		conn.setRequestProperty("Authorization", "Basic " + encoding);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);
		in.close();

		return response.toString();
	}
	
	public static void deleteRelation(String redmineHost, String apiAccessKey, String login, String password, int issue, int relationID) throws Exception {
		String urltopost = redmineHost+"/relations/"+relationID+".json";
		String charset = "UTF-8";		
		String postParams = "_method=delete";

		String encoding = Base64.encodeBase64String(apiAccessKey.getBytes());
		
		HttpURLConnection conn = (HttpURLConnection) new URL(urltopost).openConnection();
		System.out.println(urltopost);

		conn.setRequestProperty("Authorization", "Basic " + encoding);
//		conn.setRequestProperty("Referer", redmineHost+"/issues/"+issue);
		conn.setDoOutput(true);

		OutputStream wr = conn.getOutputStream();
		wr.write(postParams.getBytes(charset));
		wr.flush();
		wr.close();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);
		in.close();
	}

	public static void createRelation(String redmineHost, String apiAccessKey, String login, String password, int issue, int issueTo, String rel) throws Exception {
		HttpURLConnection conn = null;
		String issueToStr = String.valueOf(issueTo);
		String urltopost = redmineHost+"/issues/"+issue;
		String result = GetPageContent(apiAccessKey, login, password, urltopost, conn);
		String postParams = getFormParams(result, issueToStr, rel);

		String encoding = Base64.encodeBase64String(apiAccessKey.getBytes());
		URL obj = new URL(urltopost+"/relations.json");
		conn = (HttpURLConnection) obj.openConnection();

		System.out.println(conn.getURL());
		
		conn.setRequestMethod("POST");	
		conn.setRequestProperty("Authorization", "Basic " + encoding);
		conn.setDoOutput(true);
		conn.setDoInput(true);

		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);
		in.close();
	}
	
	public static void deleteParent(String redmineHost, String apiAccessKey, String login, String password, int issue) throws Exception {
		HttpURLConnection conn = null;
		String urltopost = redmineHost+"/issues/"+issue;
		String result = GetPageContent(apiAccessKey, login, password, urltopost, conn);
		String postParams = getFormParams1(result);

		String encoding = Base64.encodeBase64String(apiAccessKey.getBytes());
		URL obj = new URL(urltopost);
		conn = (HttpURLConnection) obj.openConnection();

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Authorization", "Basic " + encoding);
		conn.setRequestProperty("Referer", redmineHost+"/issues/"+issue);
		conn.setDoOutput(true);
		conn.setDoInput(true);

		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);
		in.close();
	}

	public static ArrayList<User> getUsers(String redmineHost, String apiAccessKey) throws Exception {
		 
		String url = redmineHost+"/users.json";
		System.out.println(url);
		HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
 
		String encoding = Base64.encodeBase64String(apiAccessKey.getBytes());
		
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Basic " + encoding);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		String res = response.toString();
		
		System.out.println(res);
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(res);
		JSONObject jsonObj = (JSONObject) obj;
		JSONArray users = (JSONArray)jsonObj.get("users");
		
		Gson gson = new Gson();
		System.out.println(users.toJSONString());
		System.out.println(users.toString());
		ArrayList<User> u = gson.fromJson(users.toJSONString(), new TypeToken<ArrayList<User>>(){}.getType());
		for (User user : u) {
			System.out.println(user.getCreatedOn());
			System.out.println(user.getLastLoginOn());
		}
		
		/*ArrayList<String> result = new ArrayList<String>();
		for (Object object : users) {
			JSONObject user = (JSONObject) object;
			StringBuilder sb = new StringBuilder();
			sb.append(user.get("firstname")).append(" ").append(user.get("lastname"));
			result.add(sb.toString());
		}
		*/
		return u;
	}
	
	public static ArrayList<Issue> getIssues(String redmineHost, String apiAccessKey) throws Exception {
		 
		String url = redmineHost+"/issues.xml?include=relations";
 
		HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
 
		String encoding = Base64.encodeBase64String(apiAccessKey.getBytes());
		
		con.setRequestMethod("GET");
		con.setRequestProperty("Authorization", "Basic " + encoding);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		System.out.println(response);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		org.w3c.dom.Document doc = db.parse(new InputSource(new ByteArrayInputStream(response.toString().getBytes("UTF-8"))));
		
		doc.getDocumentElement().normalize();
		
		NodeList issues = doc.getElementsByTagName("issue");
		ArrayList<Issue> res = new ArrayList<Issue>();
		for (int i = 0; i < issues.getLength(); ++i) {
			org.w3c.dom.Element issue = (org.w3c.dom.Element) issues.item(i);
			Issue is = new Issue();
//			is.setAuthor(((org.w3c.dom.Element)(issue.getElementsByTagName("author").item(0))).getAttribute("name"));
			is.setSubject(((org.w3c.dom.Element)(issue.getElementsByTagName("subject").item(0))).getTextContent());
			is.setPriorityId(Integer.valueOf(((org.w3c.dom.Element)(issue.getElementsByTagName("priority").item(0))).getAttribute("id")));
			is.setId(Integer.valueOf(((org.w3c.dom.Element)(issue.getElementsByTagName("id").item(0))).getTextContent()));
			res.add(is);
		}
		
		
		/*
		String res = response.toString();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(res);
		JSONObject jsonObj = (JSONObject) obj;
		JSONArray issues = (JSONArray)jsonObj.get("issues");
		
		Gson gson = new Gson();
		ArrayList<Issue> result = gson.fromJson(issues.toJSONString(), new TypeToken<ArrayList<Issue>>(){}.getType());
		*/
		return res;
	}
}
