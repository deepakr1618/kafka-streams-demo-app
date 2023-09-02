package com.demo.wordcount;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.omg.SendingContext.RunTime;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

public class WordCountApp {

    public static void main(String[] args) {
        Properties props = new Properties();
        // Consumer group
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-counter");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        /**
         * Topology : Consume the sentences sent to the topic "sentences"
         * 1. Split the sentences based on space and convert it to lower case
         * 2. Group the array based on values
         * 3. Count the occurrence od each of the values that are grouped
         * 4. Convert the word:count to a stream and publish to a topic named word-count
         */
        streamsBuilder.<String, String>stream("sentences")
                .flatMapValues((readOnlyKey, value) -> Arrays.asList(value.toLowerCase(Locale.ROOT).split(" ")))
                .groupBy(( key, value ) -> value)
                .count(Materialized.with(Serdes.String(), Serdes.Long()))
                .toStream()
                .to("word-count", Produced.with(Serdes.String(), Serdes.Long()));

        // plug the topology to the kafka stream
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), props);

        kafkaStreams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }
}
