package com.example.bootifulcassandra;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dstx.astra.sdk.AstraClient;
import com.dstx.astra.sdk.devops.ApiDevopsClient;
import com.dstx.astra.sdk.devops.CloudProviderType;
import com.dstx.astra.sdk.devops.DatabaseStatusType;
import com.dstx.astra.sdk.devops.DatabaseTierType;
import com.dstx.astra.sdk.devops.req.DatabaseCreationRequest;
import com.dstx.astra.sdk.devops.res.Database;
import com.dstx.astra.sdk.devops.res.DatabaseInfo;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@TestPropertySource(locations="/application.properties")
public class AstraClientTest {

    private CloudProviderType cloudProvider = CloudProviderType.AWS;
    
    @Value("${astra.databaseId}")
    private String databaseid;
    
    @Value("${astra.cloudRegion}")
    private String cloudProviderRegion;
    
    @Value("${astra.applicationToken}")
    private String applicationToken;
    
    @Value("${astra.keyspace}")
    private String keyspace;
    
    @Test
    public void should_list_available_db() throws InterruptedException {
        
        /** 
         * Astra Provides a devops API
         * 
         * The SDK provide a client with class `ApiDevopsClient` only need a JWT token
         */
        ApiDevopsClient devopsClient = new ApiDevopsClient(applicationToken);
        
        /**
         * The client expose all methods with fluent approach
         * devopsClient.findAllAvailableRegions()
         * devopsClient.findAllDatabases()
         * devopsClient.findAllDatabasesNonTerminated()
         * devopsClient.findDatabaseById(dbId)
         * devopsClient.createKeyspace(dbId, keyspace);
         * devopsClient.createNamespace(dbId, namespace);
         * devopsClient.databaseExist(dbId)
         * devopsClient.downloadSecureConnectBundle(dbId, destination);
         * 
         * Here we are listing existing DB for a user 
         */
        devopsClient.findAllDatabasesNonTerminated()
                    .map(Database::getInfo)
                    .map(DatabaseInfo::getName)
                    .forEach(System.out::println);
    }
   
    @Test
    public void should_create_new_serverless_db() throws InterruptedException {
        
        /**
         * AstraClient is the main and only class to interact with ASTRA.
         * 
         * It is wapping multiple APIs. It will initialized it expected parameters are provided
         */
        AstraClient astraClient =  AstraClient.builder()
                .appToken(applicationToken).build();
        
        /**
         * Devops API make no exeption
         */
        astraClient.apiDevops()
                   .findAllDatabasesNonTerminated()
                   .map(Database::getInfo)
                   .map(DatabaseInfo::getName)
                   .forEach(System.out::println);
        
        /**
         * We create a new serverless database
         */
        String dbId = astraClient.apiDevops().createDatabase(DatabaseCreationRequest.builder()
                .tier(DatabaseTierType.serverless)
                .cloudProvider(cloudProvider)
                .cloudRegion(cloudProviderRegion)
                .name("josh_db")
                .keyspace("josh_db")
                .username("josh")
                .password("joshlong1")
                .build());
        
        /**
         * Instance creation take about 3min
         */
        System.out.println("Starting new instance '" + dbId + "' (about 3min)");
        while(DatabaseStatusType.ACTIVE != astraClient.apiDevops().findDatabaseById(dbId).get().getStatus()) {
            Thread.sleep(1000);
            System.out.println("+ Initializing....");
        }
        System.out.println("Ready");
    }
    
    @Test
    public void should_create_cqlSession() throws InterruptedException {
        
        /**
         * With an Instance we can do more.
         *
         * Astra works with regular Cassandra drivers.
         * AstraClient simplify configuration of the CqlSession
         */
        AstraClient astraClientFull =  AstraClient.builder()
                .appToken(applicationToken)
                .databaseId(databaseid)
                .cloudProviderRegion(cloudProviderRegion)
                .build();
      
        /**
         * Let's use CqlSession as is.
         */
        System.out.println("dataCenter:" + astraClientFull.cqlSession()
                .execute("SELECT data_center from system.local")
                .one()
                .getString("data_center"));
    }
    
    
    @Test
    public void should_use_documentAPI() throws InterruptedException {
        
        /**
         * With an Instance we can do more.
         *
         * Astra works with regular Cassandra drivers.
         * AstraClient simplify configuration of the CqlSession
         */
        AstraClient astraClientFull =  AstraClient.builder()
                .appToken(applicationToken)
                .databaseId(databaseid)
                .cloudProviderRegion(cloudProviderRegion)
                .build();
        /**
         * Let's use Document API
         */
        
        // Creating new collections
        astraClientFull.apiDocument()
                       .namespace(yourNamespace)
                       .collection("videos")
                       .create();
        
        // Inserting a document
        String documentId = astraClientFull.apiDocument()
                       .namespace(yourNamespace)
                       .collection("videos")
                       .save(new Order());
        System.out.println("Document created:" + documentId);
        
        // Reading a document from its id
        Optional<OrderSchemaless> p = astraClientFull.apiDocument()
                        .namespace(yourNamespace)
                        .collection("videos")
                        .document(documentId).find(Person.class);
        System.out.println(p.get().getFirstname());
    }
    */
    
}
