package com.example.bootifulcassandra.conf;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.dstx.astra.sdk.AstraClient;
import com.example.bootifulcassandra.order.Order;
import com.example.bootifulcassandra.order.OrderPrimaryKey;
import com.example.bootifulcassandra.order.OrderRepository;

@Configuration
public class BootifulConfiguration {
    
    @Autowired
    AstraClient astraClient;
    
    @Bean
    ApplicationListener<ApplicationReadyEvent> ready(OrderRepository repository) {
        return event -> {
            var orderId = UUID.randomUUID();
            var listOfOrders = new ArrayList<Order>();
            for (var i = 0; i < 1000; i++) {
                var productId = UUID.randomUUID();
                listOfOrders.add(new Order(new OrderPrimaryKey(orderId, productId), 10,
                        "Product#" + productId, (float) (Math.random() * 100f), Instant.now()));
            }

            repository
                    .deleteAll()
                    .thenMany(repository.saveAll(listOfOrders))
                    .thenMany(repository.findAll())
                    .subscribe(System.out::println);

        };
    }
}
