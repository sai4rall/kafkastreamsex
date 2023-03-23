package org.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        String inputTopic = "inputTopic";
        System.out.println("Hello world!");
        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-live-test");
         String bootstrapServers = "localhost:9092";
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        Path stateDirectory = Files.createTempDirectory("kafka-streams");
        streamsConfiguration.put(
                StreamsConfig.STATE_DIR_CONFIG, stateDirectory.toAbsolutePath().toString());
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(inputTopic);

        Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);KStream<String,String> splited=textLines.flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())));
        KTable<String, Long> wordCounts = splited
                .groupBy((key, word) -> word)
                .count();
        wordCounts.toStream()
                .foreach((word, count) -> System.out.println("word: " + word + " -> " + count));
        String outputTopic = "outputTopic";
        wordCounts.toStream()
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));

        Topology topology = builder.build();
        KafkaStreams streams = new KafkaStreams(topology, streamsConfiguration);
        streams.start();

//        Thread.sleep(30000);
//        streams.close();
    }
}