package processor.freebase;

/*
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;
import java.io.FileInputStream;
import java.util.Properties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
*/
public class GoogleApiAdapter {
	/*
	public static Properties properties = new Properties();

	public static void fetch(String query) {
		try {
			properties.load(new FileInputStream("freebase.properties"));
			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport
					.createRequestFactory();
			JSONParser parser = new JSONParser();
			GenericUrl url = new GenericUrl(
					"https://www.googleapis.com/freebase/v1/search");
			url.put("query", query);
			url.put("limit", "10");
			url.put("indent", "true");
			url.put("key", properties.get("AIzaSyDjkq6Ve2Z1ct_ejyYj6isLbL3mKof8F1A"));
			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse httpResponse = request.execute();
			JSONObject response = (JSONObject) parser.parse(httpResponse
					.parseAsString());
			JSONArray results = (JSONArray) response.get("result");
			for (Object result : results) {
				System.out.println(JsonPath.read(result, "$.name").toString());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	*/
}