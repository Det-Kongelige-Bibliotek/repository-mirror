package dk.kb.dbStuff;

import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.*;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.*;

import org.apache.http.impl.auth.AuthSchemeBase;
import org.apache.http.auth.Credentials;

public class ApiClient {

    private Credentials credentials = null;
    private String user = "";
    private String pwd  = "";

    public ApiClient() {}

    private Credentials getCred() {
	return this.credentials;
    }

    public void setLogin(String user, String password) {
	this.user = user;
	this.pwd  = pwd;
    }

    private void credentials(CloseableHttpClient client, String realm) {
	this.credentials = new org.apache.http.auth.UsernamePasswordCredentials(this.user, this.pwd);
        client.
	    getCredentialsProvider().
	    setCredentials(new AuthScope("localhost", 443),
    						       new UsernamePasswordCredentials("username", "password"));



    }

    public String restGet(String URI) {
	String contents = "";
	try {
	    HttpGet request = new HttpGet(URI);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    if(credentials != null) {}
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = EntityUtils.toString(entity);
	} catch(java.io.IOException e) {

	}
	return contents;
    }

    public String restHead(String URI) {
	String contents = "";
	try {
	    HttpHead request = new HttpHead(URI);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = contents + response.toString();
	    //	    contents = contents + EntityUtils.toString(entity);
	} catch(java.io.IOException e) {

	}
	return contents;
    }

    public String restDelete(String URI) {
	String contents = "";
	try {
	    HttpDelete request = new HttpDelete(URI);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);
	    HttpEntity entity = response.getEntity();
	    contents = EntityUtils.toString(entity);
	} catch(java.io.IOException e) {

	}
	return contents;
    }

    public String restPut(String text, String URI) {
	String contents = "";
	try {
	    HttpPut request = new HttpPut(URI);
	    AbstractHttpEntity entity = new StringEntity(text);
	    entity.setContentType("text/xml");
            entity.setContentEncoding("UTF-8");
	    request.setEntity(entity);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);

	} catch(java.io.IOException e) {

	}
	return contents;
    }

    public String restPost(String text, String URI) {
	String contents = "";
	try {
	    HttpPost request = new HttpPost(URI);
	    HttpEntity entity = new StringEntity(text);
	    request.setEntity(entity);
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    CloseableHttpResponse response = httpClient.execute(request);

	} catch(java.io.IOException e) {

	}
	return contents;
    }

}