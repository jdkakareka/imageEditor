import com.google.gson.*;
import netscape.javascript.JSObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.JSON;


public class WeatherForecast {
    public static void main(String[] args) throws IOException {
        String latitude = "39.165325";
        String longitude = "-86.526382";
        String unit = "F";
        for(int i = 0;i < args.length;i++){
            if(args[i].equals("--latitude")){
                try{
                    Double d = Double.parseDouble(args[i+1]);
                    latitude = args[i+1];
                }catch(NumberFormatException e){
                    throw new NumberFormatException();
                };
                i++;
            }else if(args[i].equals("--longitude")){
                try{
                    Double d = Double.parseDouble(args[i+1]);
                    longitude = args[i+1];
                }catch(NumberFormatException e){
                    throw new NumberFormatException();
                };
                i++;
            }else if(args[i].equals("--unit")){
                if(args[i+1].equals("F") || args[i+1].equals("C")){
                    unit = args[i+1];
                }else{
                    throw new IOException();
                }
                i++;
            }
        }

        String website = "https://api.open-meteo.com/v1/forecast?latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m&temperature_unit=";
        if(unit.equals("C")){
            website += "celsius" + "&timezone=EST";
        }else{
            website += "fahrenheit" + "&timezone=EST";
        }
        URL url = new URL(website);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        String code = con.getRequestMethod();
        if (con.getResponseCode() != 200) {
            throw new IOException("Connection Failed");
        }


        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line = br.readLine();
        StringBuilder build = new StringBuilder();
        while (line != null) {
            build.append(line);
            line = br.readLine();
        }
        System.out.println(build);

        JsonElement elem = JsonParser.parseString(build.toString());
        JsonObject jo = elem.getAsJsonObject();
        JsonArray times = jo.get("hourly").getAsJsonObject().get("time").getAsJsonArray();
        JsonArray temperature = jo.get("hourly").getAsJsonObject().get("temperature_2m").getAsJsonArray();

        System.out.println("7-Day Forecast in Fahrenheit:");
        JsonElement date = times.get(0);
        for (int i = 0; i < times.size(); i += 3) {
            if (!times.get(i).getAsString().contains(date.getAsString().substring(0,date.getAsString().indexOf("T")))) {
                date = times.get(i);
                System.out.println("Forecast for " + date.getAsString().substring(0,date.getAsString().indexOf("T")) + " :");
            }
            String hours = times.get(i).getAsString();
            hours = hours.substring(hours.indexOf("T") + 1);
            if (times.contains(date)) {
                System.out.print(hours + ": ");
                System.out.print(temperature.get(i).getAsString());
                if(unit.equals("C")){
                    System.out.println("°C");
                }else{
                    System.out.println("°F");
                }
            }
        }
    }
}
