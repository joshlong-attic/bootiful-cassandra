package com.example.bc1;

import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.dstx.astra.sdk.AstraClient;
import com.dstx.stargate.sdk.rest.TableClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.ReactiveSession;
import org.springframework.data.cassandra.ReactiveSessionFactory;
import org.springframework.data.cassandra.core.cql.*;
import org.springframework.data.cassandra.core.mapping.*;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@SpringBootApplication
public class Bc1Application {

	public static void main(String[] args) {
		SpringApplication.run(Bc1Application.class, args);
	}

	@Bean
	ReactiveCqlTemplate reactiveCqlTemplate(ReactiveSessionFactory sessionFactory) {
		return new ReactiveCqlTemplate(sessionFactory);
	}

	private final String dua = "Dua";
	private final String patrick = "Patrick";
	private final String mia = "Mia";
	private final String josh = "Josh";


	@Bean
	ApplicationRunner runner(AstraClient ac,
																										ReactiveCqlTemplate template,
																										CustomerOrdersRepository repository) {
		return event -> {

			Mono<Void> delete = repository.deleteAll();

			Flux<CustomerOrders> writes = Flux
				.fromIterable(List.of(this.dua, this.mia, this.josh, this.patrick))
				.flatMap(name -> addOrders(repository, name));

			Flux<CustomerOrders> cql = this.cqlTemplate(template);

			Flux<CustomerOrders> repoById = writes
				.map(CustomerOrders::getCustomerId)
				.take(1)
				.flatMap(repository::findByCustomerId);

			delete
				.thenMany(writes)
				.thenMany(repoById.doOnNext(co -> System.out.println("BY ID: " + co.toString())))
				.thenMany(cql.doOnNext(co -> System.out.println("CQL: " + co.toString())))
				.subscribe();
		};
	}


	Flux<CustomerOrders> addOrders(CustomerOrdersRepository repository, String name) {

		var customerId = UUID.randomUUID();
		var list = new ArrayList<CustomerOrders>();

		for (var i = 0; i < (Math.random() * 10); i++)
			list.add(new CustomerOrders(customerId, UUID.randomUUID(), name));

		return repository.saveAll(list);

	}


	Flux<CustomerOrders> cqlTemplate(ReactiveCqlTemplate template) {
		return template
			.query("select * from orders_by_customer ", (row, i) ->
				new CustomerOrders(row.getUuid("customer_id"), row.getUuid("order_id"), row.getString("customer_name")));
	}

	private void documents(AstraClient ac) {
		ac
			.apiDocument()
			.namespace("crm")
			.collection("orders")
			.create();
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


interface CustomerOrdersRepository extends ReactiveCassandraRepository<CustomerOrders, CustomerOrdersPrimaryKey> {


	Flux<CustomerOrders> findByName(String name);

	Flux<CustomerOrders> findByCustomerId(UUID customerId);
}