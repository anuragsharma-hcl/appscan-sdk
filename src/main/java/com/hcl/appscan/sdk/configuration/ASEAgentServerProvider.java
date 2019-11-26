/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.appscan.sdk.configuration;

import com.hcl.appscan.sdk.CoreConstants;
import com.hcl.appscan.sdk.auth.IASEAuthenticationProvider;
import com.hcl.appscan.sdk.http.HttpResponse;
import com.hcl.appscan.sdk.http.HttpsClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

/**
 *
 * @author anurag-s
 */
public class ASEAgentServerProvider implements IComponent{
    private Map<String, String> m_agentServers;
    private IASEAuthenticationProvider m_authProvider;

    public ASEAgentServerProvider(IASEAuthenticationProvider provider) {
        this.m_authProvider=provider;
    }   
    
    

    @Override
    public Map<String, String> getComponents() {
        if(m_agentServers == null)
		loadAgentServers();
		return m_agentServers;
        }
    

    @Override
    public String getComponentName(String id) {
        return getComponents().get(id);
    }
    
    private void loadAgentServers() {
        if(m_authProvider.isTokenExpired())
			return;
		
		m_agentServers = new HashMap<String, String>();
		//String url =  m_authProvider.getServer() + ASE_APPS + "columns=name&sortBy=%2Bname"; //$NON-NLS-1$
                String url =  m_authProvider.getServer() + CoreConstants.ASE_AGENT_SERVER;
		Map<String, String> headers = m_authProvider.getAuthorizationHeader(true);
		//headers.putAll(Collections.singletonMap("Range", "items=0-999999")); //$NON-NLS-1$ //$NON-NLS-2$
		
		HttpsClient client = new HttpsClient();
		
		try {
			HttpResponse response = client.get(url, headers, null);
			
			if (!response.isSuccess())
				return;
		
			JSONArray array = (JSONArray)response.getResponseBodyAsJSON();
			if(array == null)
				return;
			
			for(int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				String id = object.getString("serverId");
				String path = object.getString("name");
				m_agentServers.put(id, path);
			}
		}
		catch(IOException | JSONException e) {
			m_agentServers = null;
		}
    }
    
}
