package pl.lodz.p.mgr;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FakeAndroid {

    private static final int SLEEP_SECONDS = 5;

    private static final String GATEWAY = "http://localhost:8080/entry";
    private static final String[] cities = {"Lodz", "Warszawa", "Poznan"};
    private static final int[] distances = {1, 3, 4, 7, 10, 50, 55, 60, 100};
    private static final int[] delays = {0, 0, 0, 0, 0, 0, 0, 5, 10, 15, 30, 60, 75, 100, 150, 200, 300, 500};

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();

        while (true) {
            String city = cities[random.nextInt(cities.length)];
            long time = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() * 1000;
            long late = delays[random.nextInt(delays.length)] * 1000;
            int distance = distances[random.nextInt(distances.length)];

            FitEntry fitEntry = new FitEntry(city, time - late, distance);
            String payload = gson.toJson(fitEntry);

            try {
                Request request = new Request.Builder()
                        .url(GATEWAY)
                        .post(RequestBody.create(JSON_MEDIA_TYPE, payload))
                        .addHeader("content-type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();

                System.out.println("OK | " + late/1000 + " | " + payload);
            } catch (IOException e) {
                System.out.println("FAIL | " + late/1000 + " | " + payload);
            }


            Thread.sleep(TimeUnit.SECONDS.toMillis(SLEEP_SECONDS));
        }
    }
}
