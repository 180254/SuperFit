package pl.lodz.p.fit;

import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class FakeAndroid {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private static final String[] cities = {"Lodz", "Warszawa", "Poznan"};
    private static final int[] distances = {1, 3, 4, 7, 10, 50, 55, 60, 100};
    private static final int[] delays = {0, 0, 0, 0, 0, 0, 0, 5, 10, 15, 30, 60, 75, 100, 150, 200, 300, 500};

    private static final int SLEEP_SECONDS = 5;
    private static final String DEFAULT_GATEWAY = "http://localhost:8080/entry";

    public static void main(String[] args) throws InterruptedException {
        String gateway = args.length > 0
                ? args[0]
                : DEFAULT_GATEWAY;

        Random random = new Random();
        Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();
        Logger logger = LoggerFactory.getLogger(FakeAndroid.class);

        while (true) {
            String city = cities[random.nextInt(cities.length)];
            long time = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() * 1000;
            long late = delays[random.nextInt(delays.length)] * 1000;
            int distance = distances[random.nextInt(distances.length)];

            FitEntry fitEntry = new FitEntry(city, time - late, distance);
            String payload = gson.toJson(fitEntry);

            try {
                Request request = new Request.Builder()
                        .url(gateway)
                        .post(RequestBody.create(JSON_MEDIA_TYPE, payload))
                        .addHeader("content-type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();

                logger.info("OK | {} | {}", late / 1000, payload);

            } catch (IOException e) {
                logger.info("FAIL | {} | {}", late / 1000, payload);
            }

            Thread.sleep(TimeUnit.SECONDS.toMillis(SLEEP_SECONDS));
        }
    }
}
