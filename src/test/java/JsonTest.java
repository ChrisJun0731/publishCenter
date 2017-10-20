import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by Administrator on 2017/10/12.
 */
public class JsonTest {
	public static void main(String[] args) {
		String json = "{commands:[{\"id\":[1,2],\"ip\":\"192.168.1.102\",\"cmd\":\"0x31\",\"data\":[{\"program\":{\"stayTime\":100,\"units\":[{\"video\":{\"x\":0,\"y\":0,\"h\":192,\"w\":360,\"filename\":\"1.avi\"}}]}}]}], resources:[]}";
		JSONObject obj = JSONObject.fromObject(json);
		JSONArray array = (JSONArray)obj.get("commands");

	}
}
