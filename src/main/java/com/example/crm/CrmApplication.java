package com.example.crm;

import com.datastax.oss.driver.api.core.DriverException;
import com.datastax.oss.driver.api.core.cql.Row;
import com.dstx.astra.sdk.AstraClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Version;
import org.springframework.data.cassandra.ReactiveSessionFactory;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.cql.ReactiveCqlTemplate;
import org.springframework.data.cassandra.core.cql.RowMapper;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SpringBootApplication
public class CrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrmApplication.class, args);
	}

	private final String patrick = "Patrick", josh = "Josh", madhura = "Madhura", yuxin = "Yuxin";

	@Bean
	ReactiveCqlTemplate reactiveCqlTemplate(ReactiveSessionFactory sessionFactory) {
		return new ReactiveCqlTemplate(sessionFactory);
	}

	@Bean
	ApplicationListener <ApplicationReadyEvent> astraClient(AstraClient astraClient){
		return  event -> {

			astraClient.apiDocument().

		} ;
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> springData(ReactiveCqlTemplate template,
																																																		CustomerRepository repository) {
		return event -> {


			Mono<Void> delete = repository.deleteAll();

			Flux<CustomerOrders> writes = Flux
				.just(this.josh, this.madhura, this.patrick, this.yuxin)
				.flatMap(name -> addOrdersFor(repository, name));

			Flux<CustomerOrders> all = repository.findAll();

			Flux<CustomerOrders> byId = writes
				.take(1)
				.map(CustomerOrders::getCustomerId)
				.flatMap(repository::findByCustomerId);

			Flux<CustomerOrders> cql = template.query(
				"select  * from orders_by_customer",
						(row, i) -> new CustomerOrders(row.getUuid("customer_id"), row.getUuid("order_id"), row.getString("customer_name")));

			delete
				.thenMany(writes)
				.thenMany(all.doOnNext(co -> System.out.println("all: " + co.toString())))
				.thenMany(byId.doOnNext(co -> System.out.println("by ID: " + co.toString())))
				.thenMany(cql.doOnNext(co -> System.out.println("CQL: " + co.toString())))
				.subscribe();

		};
	}

	private Flux<CustomerOrders> addOrdersFor(CustomerRepository repository, String name) {

		var customerId = UUID.randomUUID();
		var list = new ArrayList<CustomerOrders>();
		for (var i = 0; i < (Math.random() * 100); i++) {
			list.add(new CustomerOrders(customerId, UUID.randomUUID(), name));
		}

		return repository.saveAll(list);

	}
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
	private String customerName;

// lightweight transaction
//	@Version
//	private Integer version;

}

@PrimaryKeyClass
class CustomerOrdersPrimaryKey {

	@PrimaryKeyColumn(name = "customer_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID customerId;

	@PrimaryKeyColumn(name = "order_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private UUID orderId;

}

interface CustomerRepository extends ReactiveCassandraRepository<CustomerOrders, CustomerOrdersPrimaryKey> {
	Flux<CustomerOrders> findByCustomerId(UUID uuid);
}
