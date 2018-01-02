package com.github.binitabharati.kafkapoc.case1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author binita
 * 
 * 
 * https://en.wikipedia.org/w/api.php?action=query&titles=San_Francisco&prop=revisions&rvprop=content&format=json
 * 
 * https://en.wikipedia.org/w/api.php?action=opensearch&search=cook&limit=10&namespace=0&format=json
 *
 */

public class WikiRestClient {
	
	final static Logger logger = LoggerFactory.getLogger(WikiRestClient.class);
	
	public static Map<String, Object> searchGoogleApi(String searchString) throws Exception {
		Map<String, Object> ret = null;
		
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("https://en.wikipedia.org/w/api.php?action=opensearch&search=" + searchString + "&limit=100&namespace=0&format=json");
		HttpResponse response = client.execute(httpGet);
		
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			Gson gson = new GsonBuilder().create();
			Type genericResultsMapType = new TypeToken<Map<String, Object>>(){}.getType();
			ret = gson.fromJson(br, genericResultsMapType);			
		}
        
		return ret;		
	}
	
	/**
	 * Get the wikipidea titles of the pages matching the search string.
	 * @param searchString
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static List<String> getWikiTitles(String searchString) {
		List<String> retTitles = new ArrayList<>();
		HttpClient client = null;
		HttpGet httpGet = null;
		try {
			client = HttpClientBuilder.create().build();
			httpGet = new HttpGet("https://en.wikipedia.org/w/api.php?action=opensearch&search=" + searchString + "&limit=1000&namespace=0&format=json");
			HttpResponse response = client.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				Gson gson = new GsonBuilder().create();
				Type genericResultsMapType = new TypeToken<List<Object>>(){}.getType();
				List<Object> searchList = gson.fromJson(br, genericResultsMapType);
				System.out.println(searchList);
				if (searchList.size() > 3) {
					List<String> urlList = (List<String>)searchList.get(3);
					for (String matchedUrl : urlList) {
						retTitles.add(matchedUrl.substring(matchedUrl.lastIndexOf("/") + 1));
					}
					
				}
				
							
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (httpGet != null) {
				httpGet.releaseConnection();
				
			}
			
		}
		
		
		return retTitles;
	}
	
	public static String getWikiContent(String wikiTitle) throws Exception {
		
		HttpClient client = null;
		HttpGet httpGet = null;
		try {
			client = HttpClientBuilder.create().build();
			httpGet = new HttpGet("https://en.wikipedia.org/w/api.php?action=query&titles=" + wikiTitle + "&prop=revisions&rvprop=content&format=json");
			HttpResponse response = client.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String sCurrentLine;
				StringBuffer sb = new StringBuffer();
				while ((sCurrentLine = br.readLine()) != null) {
					sb.append(sCurrentLine);
					sb.append("\n");
				}
				//Remove last new line
				String ret = sb.substring(0, sb.lastIndexOf("\n"));
				logger.info("getWikiContent: produced msg = "+ret);
				System.out.println(ret.getBytes().length);
				return ret;
							
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (httpGet != null) {
				httpGet.releaseConnection();
				
			}
			
		}
			
		return null;
		
	}
	
	public static void main(String[] args) throws Exception {
		List<String> wikiTitles = getWikiTitles("api");
		System.out.println(wikiTitles);
		for (String eachTitle : wikiTitles) {
			String wikiContent = getWikiContent(eachTitle);
			System.out.println(wikiContent.getBytes().length);
			System.out.println(wikiContent);
		}
		
	}

}
