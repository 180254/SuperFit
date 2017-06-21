package pl.lodz.p.fit;

import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.StreamingOptions;

interface ProjectOptions extends StreamingOptions {

    // @formatter:off

    @Default.String("localhost:9092")
    String getKafkaBootstrapServer();
    void setKafkaBootstrapServer(String value);

    @Default.String("kafka")
    String getKafkaTopic();
    void setKafkaTopic(String value);

    @Default.Integer(60)
    int getWindowDurationSeconds();
    void setWindowDurationSeconds(int value);

    @Default.Integer(180)
    int getAllowedLatenessSeconds();
    void setAllowedLatenessSeconds(int value);

    @Default.Double(0.2)
    double getEarlyFiringPercentage();
    void setEarlyFiringPercentage(double value);

    @Default.Double(0.9)
    double getLateFiringPercentage();
    void setLateFiringPercentage(double value);

    // @formatter:on
}

