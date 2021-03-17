# Getting Started with DataStax Astra for Cassandra 


## 1. Setup ASTRA

- [Create an Astra Instance](https://docs.datastax.com/en/astra/docs/creating-your-astra-database.html)

- Create the Table

Once the database is ready go to CQLConsole and create the table

```sql
CREATE TABLE IF NOT EXISTS starter_orders (
  order_id uuid,
  product_id uuid,
  product_quantity int,
  product_name text,
  product_price decimal,
  added_to_order_at timestamp,
  PRIMARY KEY ((order_id), product_id)
 ) WITH CLUSTERING ORDER BY (product_id DESC);
```

- [Create an token](https://docs.datastax.com/en/astra/docs/manage-application-tokens.html) with role `Databse administrator`

## 2. Build ASTRA SDK


```bash
git clone https://github.com/clun/astra-sdk-java.git
cd astra-sdk-java
mvn clean install -Dmaven.test.skip=true

```

Then add the SDK client to the pom.xmlL

```
<dependency>
    <groupId>com.datastax.astra</groupId>
    <artifactId>astra-spring-boot-starter</artifactId>
    <version>2021.1-SNAPSHOT</version>
</dependency>

```

## 3. Setup your application

- Copy the db id, the cloud region and the apptoken in Application.properties

```properties
astra.cloudRegion=us-east-1
astra.databaseId=de9d6c10-c8e6-43f0-95dc-72517cf801ec
astra.applicationToken=AstraCS:lAkKZnycDWcAxCyOiBMdaoie:025dbbb5904xxx
astra.keyspace=bootiful
```

## 4. Run TESTS

- Open this application in an IDE

- Open `pom.xml` and check reference to the spring boot starter

```xml
<dependency>
  <groupId>com.datastax.astra</groupId>
  <artifactId>astra-spring-boot-starter</artifactId>
  <version>2021.1-SNAPSHOT</version>
</dependency>
```

- Start working with `AstraClientTest`

```java
should_list_available_db()
should_create_new_serverless_db()
should_create_cqlSession()
```

- Back to the Astra UI create a new KEYSPACE `namespace1`

- Update code to provide namespace name 
```java
should_use_documentAPI()
```

- Run the Application

```bash
mvn spring-boot:run
```

## 5. Check in the DB

