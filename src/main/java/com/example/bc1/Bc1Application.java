package com.example.bc1;

import com.dstx.astra.sdk.AstraClient;
import com.dstx.stargate.sdk.rest.TableClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Log4j2
@SpringBootApplication
public class Bc1Application {

	public static void main(String[] args) {
		SpringApplication.run(Bc1Application.class, args);
	}

	@Bean
	ApplicationRunner runner(AstraClient ac, CustomerOrdersRepository repository) {
		return event -> {

			TableClient table = ac.apiRest().keyspace("crm").table("orders_by_customer");
			 

//			springData(repository, "Jane");
//			documents(ac);


		};
	}

	private void documents(AstraClient ac) {
		ac
			.apiDocument()
			.namespace("crm")
			.collection("orders")
			.create();
	}


	void springData(CustomerOrdersRepository repository, String name) {

		var customerId = UUID.randomUUID();
		var list = List.of(
			new CustomerOrders(
				customerId, UUID.randomUUID(), name),
			new CustomerOrders(
				customerId, UUID.randomUUID(), name),
			new CustomerOrders(
				customerId, UUID.randomUUID(), name)
		);

		/*repository
			.saveAll(list)
			.thenMany(repository.findAll())
			.subscribe(System.out::println);
		*/

		repository
			.saveAll(list)
			.thenMany(repository.findByCustomerId(customerId))
			.subscribe(System.out::println);


	}

}

/*
@Data
@AllArgsConstructor
@NoArgsConstructor
class Order {
	private String uuid;
}

@Data
@AllArgsConstructor
@RequiredArgsConstructor
class Customer {
	private String uuid;
	private String name;
	private Set<Order> orders = new HashSet<>();
}
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@PrimaryKeyClass
class CustomerOrdersPrimaryKey {

	@PrimaryKeyColumn(name = "customer_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID customerId;

	@PrimaryKeyColumn(name = "order_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private UUID orderId;

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "orders_by_customer")
class CustomerOrders {

	@PrimaryKeyColumn(name = "customer_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID customerId;

	@PrimaryKeyColumn(name = "order_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private UUID orderId;

	@Column("customer_name")
	private String name;

}

/*@PrimaryKeyClass
@Data
@AllArgsConstructor
@NoArgsConstructor
class OrderPrimaryKey {

	@PrimaryKeyColumn(name = "order_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID orderId;

	@PrimaryKeyColumn(name = "product_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private UUID productId;
}*/

interface CustomerOrdersRepository extends ReactiveCassandraRepository<CustomerOrders, CustomerOrdersPrimaryKey> {

	//	@Query("")
	Flux<CustomerOrders> findByCustomerId(UUID customerId);
}