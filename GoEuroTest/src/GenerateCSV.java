import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;


public class GenerateCSV {

	private static final String API_URL = "http://api.goeuro.com/api/v2/position/suggest/en/";
	private static final String FILE_HEADER = "_id,name,type,latitude,longitude";
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";

	public static void main(String[] args) {
		try {
			if (args.length == 0) {
	            System.out.println("Please enter City Name as an argument");
	            return;
	        }
			
			String response = callGetAPI(API_URL, args[0]);
			List<List<String>> cityInfoList = parseStringToJson(response);
			if(!cityInfoList.isEmpty())
				generateCSV("cityInfo.csv", cityInfoList);
			else
				System.out.println("Unable to generate CSV as data is not available");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static String callGetAPI(String apiURL, String cityName){
		Client restClient = Client.create();
		System.out.println("Going to call API...");
		WebResource resource = restClient.resource(apiURL + cityName);
		String response = resource.get(String.class);
		return response;
	}

	public static List<List<String>> parseStringToJson(String jsonResp) throws JSONException{
		List<List<String>> cityInfoList = new ArrayList<List<String>>();
		JSONArray jsonArrayResponse = new JSONArray(jsonResp);
		System.out.println("Parsing the response...");
		for (int i = 0; i < jsonArrayResponse.length(); i++) {
			List<String> locationInfoList = new ArrayList<String>();
			JSONObject cityInfoObject = jsonArrayResponse.getJSONObject(i);
			locationInfoList.add(cityInfoObject.get("_id").toString());
			locationInfoList.add(cityInfoObject.get("name").toString());
			locationInfoList.add(cityInfoObject.get("type").toString());
			if(cityInfoObject.has("geo_position")){
				JSONObject geoPositionObj = (JSONObject) cityInfoObject.get("geo_position");
				locationInfoList.add(geoPositionObj.get("latitude").toString());
				locationInfoList.add(geoPositionObj.get("longitude").toString());
			}
			cityInfoList.add(locationInfoList);

		}
		return cityInfoList;
	}

	public static void generateCSV(String csvName, List<List<String>> cityInfoList){
		FileWriter fileWriter = null;
		System.out.println("CSV file is creating, please wait...");
		try {
			fileWriter = new FileWriter(csvName);

			//Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);
			
			//Write a City Info list to the CSV file
			for (List<String> cityInfo : cityInfoList) {
				for(int i = 0; i < cityInfo.size(); i++){
					fileWriter.append(cityInfo.get(i));
					fileWriter.append(COMMA_DELIMITER);
				}
				fileWriter.append(NEW_LINE_SEPARATOR);
			}
			
			System.out.println("CSV file was created successfully !!!");
			
		} catch (Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
			}
			
		}
	}

}
