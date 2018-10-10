package http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class Httppost {

	public Httppost() {
		// TODO Auto-generated constructor stub
	}

	public static String sendData(String path, Map<String, Object> map,
			String encode) {
		String result = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(path);
		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
		try {
			if (map != null && !map.isEmpty()) {
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String name = entry.getKey();
					String value = entry.getValue().toString();
					BasicNameValuePair nameValuePair = new BasicNameValuePair(
							name, value);
					list.add(nameValuePair);
				}
			}
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(
					list, encode);
			httpPost.setEntity(urlEncodedFormEntity);
			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity(), encode);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}
}
