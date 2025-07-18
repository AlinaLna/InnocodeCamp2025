    package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class LocationService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public String getCoordinatesFromAddress(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = NOMINATIM_URL + "?q=" + encodedAddress + "&format=json&limit=1";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Yêu cầu User-Agent hợp lệ
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java NominatimService)");

            // Đọc phản hồi
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonResult = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResult.append(line);
            }
            reader.close();

            // Parse JSON
            Gson gson = new Gson();
            JsonArray results = gson.fromJson(jsonResult.toString(), JsonArray.class);
            if (results.size() > 0) {
                JsonObject location = results.get(0).getAsJsonObject();
                String lat = location.get("lat").getAsString();
                String lon = location.get("lon").getAsString();
                return lat + "," + lon;
            } else {
                return "Location not found!";
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "Connecting error";
        }
    }

    public boolean isAddressFound(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlStr = NOMINATIM_URL + "?q=" + encodedAddress + "&format=json&limit=1";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java NominatimService)");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonResult = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResult.append(line);
            }
            reader.close();
            Gson gson = new Gson();
            JsonArray results = gson.fromJson(jsonResult.toString(), JsonArray.class);
            return results.size() > 0;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean isWithinRadius(String gpscv, String gpsjd, double radiusKm) {
        try {
            String[] parts1 = gpscv.split(",");
            String[] parts2 = gpsjd.split(",");
            if (parts1.length != 2 || parts2.length != 2) return false;
            double lat1 = Double.parseDouble(parts1[0].trim());
            double lon1 = Double.parseDouble(parts1[1].trim());
            double lat2 = Double.parseDouble(parts2[0].trim());
            double lon2 = Double.parseDouble(parts2[1].trim());
            double distance = haversine(lat1, lon1, lat2, lon2);
            return distance <= radiusKm;
        } catch (Exception e) {
            return false;
        }
    }

    // Công thức Haversine tính khoảng cách giữa hai điểm GPS (km)
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính Trái Đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static void main(String[] args) {
        LocationService service = new LocationService();
        String testAddress = "Đà Nẵng";
        boolean found = service.isAddressFound(testAddress);
        System.out.println("Address found: " + found);
        String coords = service.getCoordinatesFromAddress(testAddress);
        System.out.println("Coordinates: " + coords);
    }
}
