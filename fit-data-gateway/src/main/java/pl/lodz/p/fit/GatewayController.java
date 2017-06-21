package pl.lodz.p.fit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@SpringBootApplication
@Controller
public class GatewayController {

    private final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    private final Producer<String, String> producer;
    private final ObjectMapper objectMapper;

    private final String[] cities = {"Lodz", "Warszawa", "Poznan"};
    private final Set<Long> timestamps = Collections.synchronizedSet(new TreeSet<>());
    private final Map<FitKey, FitEntry> fitEntries = new ConcurrentHashMap<>();

    @Autowired
    public GatewayController(Environment env) {
        Properties kafkaCfg = new Properties();
        kafkaCfg.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, env.getProperty("kafka.server"));
        kafkaCfg.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
        kafkaCfg.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(kafkaCfg);
        objectMapper = new ObjectMapper();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String home() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-30s %15s %15s %15s\n", "Czas", "Lodz", "Warszawa", "Poznan"));

        for (Long timestamp : timestamps) {
            sb.append(String.format("%-30s",
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
            ));

            for (String city : cities) {
                sb.append(String.format(" %15s",
                        fitEntries.getOrDefault(new FitKey(city, timestamp), new FitEntry()).getDistance()
                ));
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    @RequestMapping(value = "/entry", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity updateEntry(@RequestBody FitEntry entry) throws JsonProcessingException {
        String payload = objectMapper.writer().writeValueAsString(entry);
        ProducerRecord<String, String> rec = new ProducerRecord<>("kafka", payload);
        Future<RecordMetadata> send = producer.send(rec);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/result", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity updateResult(@RequestBody FitEntry entry) {
        fitEntries.put(new FitKey(entry), entry);
        timestamps.add(entry.getTime());
        logger.info("{}", entry);
        return ResponseEntity.ok().build();

    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayController.class, args);
    }
}
