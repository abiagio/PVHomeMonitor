
/**
 * @author Andrea Biagini
 * 
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
 
//import javax.net.ssl.HttpsURLConnection;
 
public class PvOutSend {
 
    private final String USER_AGENT = "PVOutput/1.4.5.1";
	
	
 
	// HTTP POST request
	void sendPost(String data,String sid, String key) throws Exception {
		
		
	//	String url = "http://pvoutput.org/service/r2/addoutput.jsp";
		String url = "http://pvoutput.org/service/r2/addbatchstatus.jsp";
	// Stringa da passare esempio :	
	// curl -d "data=20110112,4:15,-1,-1,2000,210" -H "X-Pvoutput-Apikey: Your-API-Key" -H "X-Pvoutput-SystemId: Your-System-Id" http://pvoutput.org/service/r2/addbatchstatus.jsp	
	//Send a single status with Consumption Energy 2000Wh, Consumption Power 210W			
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		//add request header
		con.setRequestMethod("POST");
	    con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("X-Pvoutput-SystemId", sid);
		con.setRequestProperty("X-Pvoutput-Apikey", key);
		//String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
	
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(data);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + data);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		//print result
		System.out.println(response.toString());
		con.disconnect();
 
	}
 
}




