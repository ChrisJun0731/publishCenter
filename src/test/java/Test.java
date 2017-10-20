import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Administrator on 2017/9/30.
 */
public class Test {
	@org.junit.Test
	public void connectionTest() {
		String urlStr = "http://192.168.30.36:3300/api/test?fileName=test.mp4";
		BufferedReader reader = null;
		String result = "";
		try {
			URL url = new URL(urlStr);
			URLConnection connection = url.openConnection();
//			connection.setRequestProperty("accept", "*/*");
//			connection.setRequestProperty("connection", "Keep-Alive");
//			connection.setRequestProperty("user-agent",  "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

			connection.connect();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = "";
			while((line = reader.readLine()) != null){
				result += line;
			}
			System.out.println(result);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
