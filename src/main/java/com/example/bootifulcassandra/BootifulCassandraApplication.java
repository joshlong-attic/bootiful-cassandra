package com.example.bootifulcassandra;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@SpringBootApplication
public class BootifulCassandraApplication {




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

	public static void main(String[] args) {
		SpringApplication.run(BootifulCassandraApplication.class, args);
	}


	@Bean
	CqlSessionBuilderCustomizer cqlSessionBuilderCustomizer(@Value("${cassandra.secure-connect-bundle}") File file) {
		Assert.isTrue(file.exists() , ()-> "specify a file path for the cassandra.secure-connect-bundle property. It should point to the ");
		return cqlSessionBuilder -> cqlSessionBuilder.withCloudSecureConnectBundle(file.toPath());
	}

}




@Table(value = "starter_orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
class Order implements Serializable {

	@PrimaryKey
	private OrderPrimaryKey key;

	@Column("product_quantity")
	@CassandraType(type = CassandraType.Name.INT)
	private Integer productQuantity;

	@Column("product_name")
	@CassandraType(type = CassandraType.Name.TEXT)
	private String productName;

	@CassandraType(type = CassandraType.Name.DECIMAL)
	@Column("product_price")
	private Float productPrice;

	@CassandraType(type = CassandraType.Name.TIMESTAMP)
	@Column("added_to_order_at")
	private Instant addedToOrderTimestamp;

}


@Repository
interface OrderRepository extends ReactiveCassandraRepository<Order, OrderPrimaryKey> {
}


@PrimaryKeyClass
@Data
@AllArgsConstructor
@NoArgsConstructor
class OrderPrimaryKey implements Serializable {

	@PrimaryKeyColumn(name = "order_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private UUID orderId;

	@PrimaryKeyColumn(name = "product_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	private UUID productId;
}
