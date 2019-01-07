package th.ac.kmitl.it.crowdalert.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationHelper {
    public static Double distance(Location location1, Location location2){
        Integer radius = 6371;
        Double latDelta = degreesToRadians(location2.getLatitude() - location1.getLatitude());
        Double lonDelta = degreesToRadians(location2.getLongitude() - location1.getLongitude());
        Double a = (Math.sin(latDelta / 2) * Math.sin(latDelta / 2)) +
                (Math.cos(degreesToRadians(location1.getLatitude())) * Math.cos(degreesToRadians(location2.getLatitude())) *
                        Math.sin(lonDelta / 2) * Math.sin(lonDelta / 2));
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius*c;
    }
    public static String getLocationName(Context ctx, Location Location) throws IOException{
        String result = "";
        Geocoder geocoder = new Geocoder(ctx, Locale.forLanguageTag("th_TH"));
        List<Address> addresses = geocoder.getFromLocation(Location.getLatitude(), Location.getLongitude(), 1);
        if (addresses.size() > 0) {
            Address address = addresses.get(0);
            result = String.format("%s, %s", address.getAddressLine(0), address.getAdminArea());
        }
        return result;
    }
    public static String getEstimateTravelTime(LatLng origin, LatLng destination){
        //TODO CHANGE API KEY
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyAUZ_Cv70dn6kYeGEAY7EptvvpmrrjYBrM")
                .build();
        try {
            DistanceMatrix distanceMatrix = DistanceMatrixApi.newRequest(context)
                    .origins(origin)
                    .destinations(destination)
                    .mode(TravelMode.WALKING)
                    .language("th-TH")
                    .await();
            DistanceMatrixRow[] results = distanceMatrix.rows;
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(gson.toJson(results[0].elements[0].duration));
            JsonObject obj = element.getAsJsonObject();
            return obj.get("humanReadable").getAsString();
        }catch (Exception ex){
            Log.e("LocationHelper", ex.getMessage());
            return null;
        }
    }

    private static Double degreesToRadians(Double degrees){
        return (degrees * Math.PI / 180);
    }

    public static String resolveArea(Location currentLocation){
        String nearestLocation = "";
        Double nearestLatLng = null;
        HashMap<String, LatLng> placesLocation = getPlaceLocation();
        for(Map.Entry<String, LatLng> entry: placesLocation.entrySet()){
            Location placeLocation = new Location("");
            placeLocation.setLatitude(entry.getValue().lat);
            placeLocation.setLongitude(entry.getValue().lng);
            if (nearestLatLng == null){
                nearestLatLng = distance(currentLocation, placeLocation);
                nearestLocation = entry.getKey();
            }else if(distance(currentLocation, placeLocation) <= nearestLatLng){
                nearestLatLng = distance(currentLocation, placeLocation);
                nearestLocation = entry.getKey();
            }
        }
        return mapLocation(nearestLocation);
    }

    private static HashMap<String, LatLng> getPlaceLocation(){
        HashMap<String, LatLng> list = new HashMap<>();
        list.put("samsen", new LatLng(13.780662, 100.5075103));
        list.put("huykwang", new LatLng(13.7769844, 100.5677222));
        list.put("dindang", new LatLng(13.7714595, 100.5546483));
        list.put("nangleng", new LatLng(13.757611, 100.5052493));
        list.put("chanasongkram", new LatLng(13.7577189, 100.4972007));
        list.put("makkasan", new LatLng(13.7460775, 100.5814473));
        list.put("bangpo", new LatLng(13.808188, 100.5167323));
        list.put("phayathai", new LatLng(13.7596316, 100.5280784));
        list.put("dusit", new LatLng(13.7774426, 100.5186908));
        list.put("phahonyothin", new LatLng(13.8258213, 100.5676414));
        list.put("bangkhen", new LatLng(13.8750735, 100.5956862));
        list.put("thungsonghong", new LatLng(13.8681447, 100.5690703));
        list.put("khokkhram", new LatLng(13.8361697, 100.6124192));
        list.put("prachachuen", new LatLng(13.8424763, 100.5465516));
        list.put("sutthisan", new LatLng(13.7901052, 100.5720569));
        list.put("bangsue", new LatLng(13.7872815, 100.5453596));
        list.put("saimai", new LatLng(13.9294047, 100.6585283));
        list.put("donmueang", new LatLng(13.9332346, 100.6068719));
        list.put("kannayao", new LatLng(13.853117, 100.6681979));
        list.put("taopoon", new LatLng(13.853107, 100.6003467));
        list.put("minburi", new LatLng(13.8154639, 100.7327085));
        list.put("nongjok", new LatLng(13.856617, 100.8659353));
        list.put("ladkrabang", new LatLng(13.7213099, 100.760678));
        list.put("romklao", new LatLng(13.7658679, 100.7301004));
        list.put("chalongkrung", new LatLng(13.7664318, 100.797073));
        list.put("chorakhenoi", new LatLng(13.718116,100.7888743));
        list.put("nimitmai", new LatLng(13.905634,100.7462293));
        list.put("lamhin", new LatLng(13.8937973,100.8222766));
        list.put("lamphakchi", new LatLng(13.8095121,100.843805));
        list.put("prachasamran", new LatLng(13.8984826,100.8500082));
        list.put("suwinthawong", new LatLng(13.8032948,100.9241866));
        list.put("buengkum", new LatLng(13.7818373,100.6576546));
        list.put("chokchai", new LatLng(13.7952862,100.5912212));
        list.put("wangthonglang", new LatLng(13.7590646,100.598758));
        list.put("prawet", new LatLng(13.7430613,100.6228038));
        list.put("udomsuk", new LatLng(13.6757149,100.6819076));
        list.put("ladprao", new LatLng(13.7642432,100.6330315));
        list.put("bangchan", new LatLng(13.8023444,100.6840183));
        list.put("huamark", new LatLng(13.760302,100.6249833));
        list.put("lumpini", new LatLng(13.7316849,100.5435865));
        list.put("watprayakorn", new LatLng(13.7019173,100.5002716));
        list.put("bangna", new LatLng(13.6734086,100.6406025));
        list.put("bangphongphang", new LatLng(13.7188475,100.5366708));
        list.put("thungmahamek", new LatLng(13.7188475,100.5366708));
        list.put("prakanong", new LatLng(13.7084595,100.5998927));
        list.put("thonglor", new LatLng(13.7359302,100.5814057));
        list.put("khlongton", new LatLng(13.7406934,100.6151352));
        list.put("tharua", new LatLng(13.7091689,100.578643));
        list.put("prarachawang", new LatLng(13.7432061,100.4923173));
        list.put("bangrak", new LatLng(13.7302445,100.5203782));
        list.put("phlapphlachai1", new LatLng(13.7439387,100.5107813));
        list.put("phlapphlachai2", new LatLng(13.7439387,100.5107813));
        list.put("jakkawat", new LatLng(13.7410059,100.5001937));
        list.put("yannawa", new LatLng(13.7410053,100.4848728));
        list.put("samranrat", new LatLng(13.7516741,100.5016191));
        list.put("pathumwan", new LatLng(13.7366273,100.5210334));
        list.put("bangkoknoi", new LatLng(13.76008,100.4693883));
        list.put("bangplad", new LatLng(13.7800948,100.4822272));
        list.put("bangyikhan", new LatLng(13.7800948,100.4822272));
        list.put("saladaeng", new LatLng(13.7515027,100.350957));
        list.put("bawonmongkhon", new LatLng(13.7742529,100.4953214));
        list.put("bangkokyai", new LatLng(13.7742504, 100.4624906));
        list.put("thapra", new LatLng(13.729562,100.4634663));
        list.put("bangsaothong", new LatLng(13.743305,100.4576933));
        list.put("bangkhunnon", new LatLng(13.7703944,100.4662571));
        list.put("talingchan", new LatLng(13.7805316,100.4440024));
        list.put("thammasala", new LatLng(13.7708016,100.3523528));
        list.put("bangyirua", new LatLng(13.7269771,100.4856838));
        list.put("taladplu", new LatLng(13.7177217,100.4738155));
        list.put("buppharam", new LatLng(13.736275,100.4877263));
        list.put("bookkalo", new LatLng(13.7061661,100.4902698));
        list.put("samre", new LatLng(13.7061661,100.4902698));
        list.put("somdejchaopraya", new LatLng(13.7312423,100.5078474));
        list.put("ratburana", new LatLng(13.6841936,100.4961802));
        list.put("thungkru", new LatLng(13.62755,100.5047211));
        list.put("bangmod", new LatLng(13.6738009,100.4528782));
        list.put("pakkhlongsan", new LatLng(13.7313246,100.5079431));
        list.put("bangkholaem", new LatLng(13.6883734,100.4953279));
        list.put("thakham", new LatLng(13.6392555,100.444909));
        list.put("bangkhuntien", new LatLng(13.6658937,100.3620033));
        list.put("phasicharoen", new LatLng(13.7152643,100.4346935));
        list.put("laksong", new LatLng(13.711862,100.3811482));
        list.put("nongkham", new LatLng(13.6766155,100.3433357));
        list.put("phetkasem", new LatLng(13.6959301,100.3211633));
        list.put("nongkhangplu", new LatLng(13.6959104,100.3211632));
        list.put("bangbon", new LatLng(13.6457346,100.3793724));
        list.put("sameadam", new LatLng(13.6378622,100.3936527));
        list.put("thianthale", new LatLng(13.5515441,100.4178809));
        return list;
    }

    private static String mapLocation(String location){
        HashMap<String, String> resolve = new HashMap<>();
        //Division 1
        resolve.put("samsen", "metropolis_division1");
        resolve.put("huykwang", "metropolis_division1");
        resolve.put("dindang", "metropolis_division1");
        resolve.put("nangleng", "metropolis_division1");
        resolve.put("chanasongkram", "metropolis_division1");
        resolve.put("makkasan", "metropolis_division1");
        resolve.put("bangpo", "metropolis_division1");
        resolve.put("phayathai", "metropolis_division1");
        resolve.put("dusit", "metropolis_division1");
        //Division 2
        resolve.put("phahonyothin", "metropolis_division2");
        resolve.put("bangkhen", "metropolis_division2");
        resolve.put("thungsonghong", "metropolis_division2");
        resolve.put("khokkhram", "metropolis_division2");
        resolve.put("prachachuen", "metropolis_division2");
        resolve.put("sutthisan", "metropolis_division2");
        resolve.put("bangsue", "metropolis_division2");
        resolve.put("saimai", "metropolis_division2");
        resolve.put("donmueang", "metropolis_division2");
        resolve.put("kannayao", "metropolis_division2");
        resolve.put("taopoon", "metropolis_division2");
        //Division3
        resolve.put("minburi", "metropolis_division3");
        resolve.put("nongjok", "metropolis_division3");
        resolve.put("ladkrabang", "metropolis_division3");
        resolve.put("romklao", "metropolis_division3");
        resolve.put("chalongkrung", "metropolis_division3");
        resolve.put("chorakhenoi", "metropolis_division3");
        resolve.put("nimitmai", "metropolis_division3");
        resolve.put("lamhin", "metropolis_division3");
        resolve.put("lamphakchi", "metropolis_division3");
        resolve.put("prachasamran", "metropolis_division3");
        resolve.put("suwinthawong", "metropolis_division3");
        //Division4
        resolve.put("buengkum", "metropolis_division4");
        resolve.put("chokchai", "metropolis_division4");
        resolve.put("wangthonglang", "metropolis_division4");
        resolve.put("prawet", "metropolis_division4");
        resolve.put("udomsuk", "metropolis_division4");
        resolve.put("ladprao", "metropolis_division4");
        resolve.put("bangchan", "metropolis_division4");
        resolve.put("huamark", "metropolis_division4");
        //Division5
        resolve.put("lumpini", "metropolis_division5");
        resolve.put("watprayakorn", "metropolis_division5");
        resolve.put("bangna", "metropolis_division5");
        resolve.put("bangphongphang", "metropolis_division65");
        resolve.put("thungmahamek", "metropolis_division5");
        resolve.put("prakanong", "metropolis_division5");
        resolve.put("thonglor", "metropolis_division5");
        resolve.put("khlongton", "metropolis_division5");
        resolve.put("tharua", "metropolis_division5");
        //Division6
        resolve.put("prarachawang", "metropolis_division6");
        resolve.put("bangrak", "metropolis_division6");
        resolve.put("phlapphlachai1", "metropolis_division6");
        resolve.put("phlapphlachai2", "metropolis_division6");
        resolve.put("jakkawat", "metropolis_division6");
        resolve.put("yannawa", "metropolis_division6");
        resolve.put("samranrat", "metropolis_division6");
        resolve.put("pathumwan", "metropolis_division6");
        //Division7
        resolve.put("bangkoknoi", "metropolis_division7");
        resolve.put("bangplad", "metropolis_division7");
        resolve.put("bangyikhan", "metropolis_division7");
        resolve.put("saladaeng", "metropolis_division7");
        resolve.put("bawonmongkhon", "metropolis_division7");
        resolve.put("bangkokyai", "metropolis_division7");
        resolve.put("thapra", "metropolis_division7");
        resolve.put("bangsaothong", "metropolis_division7");
        resolve.put("bangkhunnon", "metropolis_division7");
        resolve.put("talingchan", "metropolis_division7");
        resolve.put("thammasala", "metropolis_division7");
        //Division8
        resolve.put("bangyirua", "metropolis_division8");
        resolve.put("taladplu", "metropolis_division8");
        resolve.put("buppharam", "metropolis_division8");
        resolve.put("bookkalo", "metropolis_division8");
        resolve.put("samre", "metropolis_division8");
        resolve.put("somdejchaopraya", "metropolis_division8");
        resolve.put("ratburana", "metropolis_division8");
        resolve.put("thungkru", "metropolis_division8");
        resolve.put("bangmod", "metropolis_division8");
        resolve.put("pakkhlongsan", "metropolis_division8");
        resolve.put("bangkholaem", "metropolis_division8");
        //Division9
        resolve.put("thakham", "metropolis_division9");
        resolve.put("bangkhuntien", "metropolis_division9");
        resolve.put("phasicharoen", "metropolis_division9");
        resolve.put("laksong", "metropolis_division9");
        resolve.put("nongkham", "metropolis_division9");
        resolve.put("phetkasem", "metropolis_division9");
        resolve.put("nongkhangplu", "metropolis_division9");
        resolve.put("bangbon", "metropolis_division9");
        resolve.put("sameadam", "metropolis_division9");
        resolve.put("thianthale", "metropolis_division9");

        return resolve.get(location);
    }
}
