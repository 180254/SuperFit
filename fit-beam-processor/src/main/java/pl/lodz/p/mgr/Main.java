package pl.lodz.p.mgr;


import okhttp3.*;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.StreamSupport;


public class Main {

    public static final String SERVICE_URL = "http://localhost:8080/result";

    public static byte[] aa(InputStream is, int size) throws IOException {
        byte[] buffer = new byte[size];
        int read = is.read(buffer);
        return buffer;
    }

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);

        Options options = PipelineOptionsFactory
                .fromArgs(args)
                .withValidation()
                .as(Options.class);
        options.setStreaming(true);

        Pipeline pipeline = Pipeline.create(options);

        int durationSec = options.getWindowDurationSeconds();
        int latenessSec = options.getAllowedLatenessSeconds();
        int earlyFire = (int) (durationSec * options.getEarlyFiringPercentage());
        int lateFire = (int) (durationSec * options.getLateFiringPercentage());

        pipeline.getCoderRegistry().registerCoderForClass(Key.class, new NiceCoder<>());
        pipeline.getCoderRegistry().registerCoderForClass(FitEntry.class, new NiceCoder<>());

        pipeline
                .apply("fetch data",
                        KafkaIO.<Long, String>read()
                                .withBootstrapServers(options.getKafkaBootstrapServer())
                                .withTopic(options.getKafkaTopic())
                                .withKeyDeserializer(LongDeserializer.class)
                                .withValueDeserializer(StringDeserializer.class)

                )
                .apply("deserialize",
                        ParDo.of(new DoFn<KafkaRecord<Long, String>, KV<Key, FitEntry>>() {

                            @Override
                            public Duration getAllowedTimestampSkew() {
                                return Duration.millis(Long.MAX_VALUE);
                            }

                            @ProcessElement
                            public void processElement(ProcessContext c) {
                                try {
                                    String json = c.element().getKV().getValue();
                                    FitEntry fitEntry = new ObjectMapper().readValue(json, FitEntry.class);
                                    Key key = new Key(fitEntry);
                                    Instant timestamp = new Instant(fitEntry.getTime());

                                    KV<Key, FitEntry> kv = KV.of(key, fitEntry);
                                    c.outputWithTimestamp(kv, timestamp);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                )
                .apply("windowing, triggering",
                        Window.<KV<Key, FitEntry>>into(
                                FixedWindows.of(Duration.standardSeconds(durationSec)))
                                .triggering(AfterWatermark
                                        .pastEndOfWindow()
                                        .withEarlyFirings(AfterProcessingTime
                                                .pastFirstElementInPane()
                                                .plusDelayOf(Duration.standardSeconds(earlyFire)))
                                        .withLateFirings(AfterProcessingTime
                                                .pastFirstElementInPane()
                                                .plusDelayOf(Duration.standardSeconds(lateFire))))
                                .withAllowedLateness(Duration.standardSeconds(latenessSec))
                                .accumulatingFiredPanes()

                )
                .apply("group by key",
                        GroupByKey.create()
                )
                .apply("compute results",
                        ParDo.of(new DoFn<KV<Key, Iterable<FitEntry>>, FitEntry>() {
                            @ProcessElement
                            public void processElement(ProcessContext c) {
                                KV<Key, Iterable<FitEntry>> kv = c.element();
                                String city = kv.getKey().getCity();
                                Iterable<FitEntry> valueEntries = kv.getValue();

                                long time = c.timestamp().getMillis();
                                int distance = StreamSupport.stream(valueEntries.spliterator(), false)
                                        .mapToInt(FitEntry::getDistance)
                                        .sum();

                                c.output(new FitEntry(city, time, distance));
                            }
                        })
                )
                .apply("send results",
                        ParDo.of(new DoFn<FitEntry, Void>() {
                            @ProcessElement
                            public void processElement(ProcessContext c) {
                                try {
                                    Request request = new Request.Builder()
                                            .url(SERVICE_URL)
                                            .post(RequestBody.create(MediaType.parse("application/json"), new ObjectMapper().writeValueAsString(c.element())))
                                            .addHeader("content-type", "application/json")
                                            .build();
                                    Response response = new OkHttpClient().newCall(request).execute();
                                    System.out.println("OK: " + c.element());
                                } catch (IOException e) {
                                    System.out.println("FAIL: " + c.element());
                                }

                            }
                        })
                );

        pipeline.run().waitUntilFinish();

    }
}
