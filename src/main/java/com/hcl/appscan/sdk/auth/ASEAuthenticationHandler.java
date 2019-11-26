/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.auth;

import com.hcl.appscan.sdk.CoreConstants;
import static com.hcl.appscan.sdk.CoreConstants.API_BLUEMIX_LOGIN;
import static com.hcl.appscan.sdk.CoreConstants.API_IBM_LOGIN;
import static com.hcl.appscan.sdk.CoreConstants.API_KEY_LOGIN;
import static com.hcl.appscan.sdk.CoreConstants.API_SCANS;
import static com.hcl.appscan.sdk.CoreConstants.BINDING_ID;
import static com.hcl.appscan.sdk.CoreConstants.CHARSET;
import static com.hcl.appscan.sdk.CoreConstants.CONTENT_TYPE;
import static com.hcl.appscan.sdk.CoreConstants.KEY_ID;
import static com.hcl.appscan.sdk.CoreConstants.KEY_SECRET;
import static com.hcl.appscan.sdk.CoreConstants.PASSWORD;
import static com.hcl.appscan.sdk.CoreConstants.TOKEN;
import static com.hcl.appscan.sdk.CoreConstants.USERNAME;
import static com.hcl.appscan.sdk.CoreConstants.UTF8;
import com.hcl.appscan.sdk.Messages;
import com.hcl.appscan.sdk.error.HttpException;
import com.hcl.appscan.sdk.http.HttpClient;
//import com.hcl.appscan.sdk.http.HttpClient;
import com.hcl.appscan.sdk.http.HttpResponse;
import com.hcl.appscan.sdk.http.HttpsClient;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

/**
 *
 * @author anurag-s
 */
public class ASEAuthenticationHandler implements CoreConstants{
    private IASEAuthenticationProvider m_authProvider;
    private List<String> cookies ;
    
    
	
	public ASEAuthenticationHandler(IASEAuthenticationProvider provider) {
		m_authProvider = provider;
	}

	/**
	 * Authenticates a user using the given LoginType.
	 * @param username The username.
	 * @param password The password.
	 * @param persist True to persist the credentials.
	 * @param type The LoginType.
	 * @return True if successful.
	 * @throws IOException If an error occurs.
	 * @throws JSONException If an error occurs.
	 */
	public boolean login(String username, String password, boolean persist,String url) throws IOException, JSONException {
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CONTENT_TYPE, "application/json; utf-8"); //$NON-NLS-1$
		headers.put(CHARSET, UTF8);
                headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		Map<String, String> params = new HashMap<String, String>();
                params.put(ASE_KEY_ID, username);
		params.put(ASE_KEY_SECRET, password);
                url=url+"/api/keylogin/apikeylogin";

		          HttpsClient client = new HttpsClient();
	    HttpResponse response = client.postForm(url, headers, params);
            cookies=response.getResponseHeaders().get("Set-Cookie");
	    
		if(response.getResponseCode() == HttpsURLConnection.HTTP_OK || response.getResponseCode() == HttpsURLConnection.HTTP_CREATED) {
			if(persist) {
				JSONObject object = (JSONObject)response.getResponseBodyAsJSON();
				String token = object.getString("sessionId");
                                List<String> cookies =response.getResponseHeaders().get("Set-Cookie");
                                       
				m_authProvider.saveConnection(token);
                                m_authProvider.setCookies(cookies);
			}
			return true;
		}
		else {
			String reason = response.getResponseBodyAsString() == null ? Messages.getMessage("message.unknown") : response.getResponseBodyAsString(); //$NON-NLS-1$
			throw new HttpException(response.getResponseCode(), reason);
		}
	}
        
        
        public List<String> getCookies(){
            return cookies;
        }
	
	public boolean isTokenExpired() {
		boolean isExpired;
		String request_url = m_authProvider.getServer() + ASE_API;
		
		Map<String, String> headers = m_authProvider.getAuthorizationHeader(false);
		headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		headers.put(CHARSET, UTF8);
                headers.put("Accept", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		
		HttpsClient httpClient = new HttpsClient();
		HttpResponse httpResponse;
		try {
			httpResponse = httpClient.get(request_url, headers, null);
			isExpired = httpResponse.getResponseCode() != HttpsURLConnection.HTTP_OK;
		} catch (IOException e) {
			isExpired = true;
		}
		return isExpired;
	}
    
}