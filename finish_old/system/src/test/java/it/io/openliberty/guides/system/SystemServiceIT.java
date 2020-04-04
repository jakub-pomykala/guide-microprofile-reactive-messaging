// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.system;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;
import org.microshed.testing.kafka.KafkaConsumerConfig;

import io.openliberty.guides.models.CpuUsage;
import io.openliberty.guides.models.CpuUsage.CpuUsageDeserializer;
import io.openliberty.guides.models.MemoryStatus;
import io.openliberty.guides.models.MemoryStatus.MemoryStatusDeserializer;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
public class SystemServiceIT {

    private static final long POLL_TIMEOUT = 30 * 1000;

    @KafkaConsumerConfig(valueDeserializer = CpuUsageDeserializer.class, 
        groupId = "cpu-status", topics = "cpuStatusTopic", 
        properties = ConsumerConfig.AUTO_OFFSET_RESET_CONFIG + "=earliest")
    public static KafkaConsumer<String, CpuUsage> cpuConsumer;

    @KafkaConsumerConfig(valueDeserializer = MemoryStatusDeserializer.class, 
        groupId = "memory-status", topics = "memoryStatusTopic", 
        properties = ConsumerConfig.AUTO_OFFSET_RESET_CONFIG + "=earliest")
    public static KafkaConsumer<String, MemoryStatus> memoryConsumer;
    
    
    @Test
    public void testCpuStatus() throws IOException, InterruptedException {
        int recordsProcessed = 0;
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        while (recordsProcessed == 0 && elapsedTime < POLL_TIMEOUT) {
            ConsumerRecords<String, CpuUsage> records = cpuConsumer.poll(Duration.ofMillis(3000));
            System.out.println("Polled " + records.count() + " records from Kafka:");
            for (ConsumerRecord<String, CpuUsage> record : records) {
                CpuUsage c = record.value();
                System.out.println(c);
                assertNotNull(c.hostId);
                assertNotNull(c.cpuUsage);
                recordsProcessed++;
            }
            cpuConsumer.commitAsync();
            if (recordsProcessed > 0)
                break;
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        assertTrue(recordsProcessed > 0, "No records processed");
    }

    @Test
    public void testMemoryStatus() throws IOException, InterruptedException {
        int recordsProcessed = 0;
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        while (recordsProcessed == 0 && elapsedTime < POLL_TIMEOUT) {
            ConsumerRecords<String, MemoryStatus> records = memoryConsumer.poll(Duration.ofMillis(3000));
            System.out.println("Polled " + records.count() + " records from Kafka:");
            for (ConsumerRecord<String, MemoryStatus> record : records) {
                MemoryStatus m = record.value();
                System.out.println(m);
                assertNotNull(m.hostId);
                assertNotNull(m.memoryUsed);
                assertNotNull(m.memoryMax);
                recordsProcessed++;
            }
            cpuConsumer.commitAsync();
            if (recordsProcessed > 0)
                break;
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        assertTrue(recordsProcessed > 0, "No records processed");
    }
}