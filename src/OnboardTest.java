import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import com.servicemesh.agility.api.Assetlist;
import com.servicemesh.agility.api.Instance;
import com.servicemesh.agility.api.Asset;
import com.servicemesh.agility.api.Template;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;


public class OnboardTest {
	
	protected Client _client = null;
    public static final String API_NS = "http://servicemesh.com/agility/api";
	
	protected final String getUrlBase() {
		String agilityHost = "192.168.76.101";
		return "https://"+agilityHost+":8443/agility/api/v2.1";
	}
	
	protected final String getAgilityUserName() {
		String username = "admin"; //This should pull the correct user name from the junit.properties
		return username;	
	}
	
	protected final String getAgilityPassword() {
		String pwd = "M3sh@dmin!";
		return pwd;	
	}
	
	public Client getClient() { return _client; }	
	
	public void setClient(Client client) { _client = client; }
	
	public Client getAdminClient() {
		return getClient(getAgilityUserName(), getAgilityPassword());
	}
	
	public Client getClient(String username, String password) { 
		try {
 	        Client client = null;
			TrustManager easyTrustManager = new X509TrustManager() {

	            public void checkClientTrusted(
	                    X509Certificate[] chain,
	                    String authType) throws CertificateException {
	                // Oh, I am easy!
	            }

	            public void checkServerTrusted(
	                    X509Certificate[] chain,
	                    String authType) throws CertificateException {
	                // Oh, I am easy!
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }           
	        };

	        SSLContext sslcontext = SSLContext.getInstance("TLS");
	        sslcontext.init(null, new TrustManager[] { easyTrustManager }, null);

			HTTPSProperties props = null;
			props = new HTTPSProperties(new PermissiveHostnameVerifier(), sslcontext);			
			ClientConfig config=new DefaultClientConfig();
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, props);
			client = Client.create(config);
			
			HTTPBasicAuthFilter filter = new HTTPBasicAuthFilter(username, password);
			client.addFilter(filter);
			return client;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}		
	}
	
	private static class PermissiveHostnameVerifier implements HostnameVerifier {
	    public boolean verify(String hostname, SSLSession session) {
	        return true;
	    }
	}

	public void setUp() throws Exception
	{
		try {
 	        _client = this.getClient(getAgilityUserName(), getAgilityPassword());
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
	}
	
	public void onboardSearch()
	{
        Client client = getClient();
        WebResource resource = client.resource(getUrlBase() + "/onboard/search");           
   
        // all instances should be unmanaged, having unknown stack
        resource = resource.path("" + 4);
        resource = resource.queryParam("fields", "id,name,cloud,stack");
        resource = resource.queryParam("limit", "305");
        Assetlist instList = resource.get(Assetlist.class);
        // all instances should be unmanaged, having unknown stack
        for (Asset asset : instList.getAssets()) {
        	Instance inst = getInstance(asset.getId());
        	
        	if(inst.getName().equals("wind2k3joel"))
        	{
        		stopInstance(inst.getId());
        	}
        }
	}
	
	public void getTemplate(int id)
	{
		Client client = getClient();
        WebResource resource = client.resource(getUrlBase() + "/template");
        resource = resource.path("" + id);
        Template inst = resource.get(Template.class);
        
	}

	public Instance getInstance(int id)
	{
		Client client = getClient();
        WebResource resource = client.resource(getUrlBase() + "/compute");
        resource = resource.path("" + id);
        Instance inst = resource.get(Instance.class);
        
        return inst;
	}
	
	public void stopInstance(int id)
	{
		Client client = getClient();
        WebResource resource = client.resource(getUrlBase() + "/compute/" + id + "/release");
        resource.post();
	}

	
	public static void main(String[] args) throws Exception
	{
		OnboardTest test= new OnboardTest();
		
		test.setUp();
		//test.getTemplate(2125);
		test.onboardSearch();
	}

}
