package com.rapid7.diskstorage.dynamodb;
/**
 * Created by root on 11/17/15.
 */
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.thinkaurelius.titan.diskstorage.configuration.BasicConfiguration;
import com.thinkaurelius.titan.diskstorage.configuration.BasicConfiguration.Restriction;
import com.thinkaurelius.titan.diskstorage.configuration.Configuration;
import com.thinkaurelius.titan.diskstorage.configuration.backend.CommonsConfiguration;
import com.thinkaurelius.titan.graphdb.configuration.GraphDatabaseConfiguration;

/**
 *
 * @author Matthew Sowders
 * @author Alexander Patrikalakis
 */
public class ClientTest {
   private Client client;

   private Configuration titanConfig;

   @Before
   public void setUp() {
      titanConfig = new BasicConfiguration(GraphDatabaseConfiguration.ROOT_NS,
         new CommonsConfiguration(TestGraphUtil.loadProperties()),
         Restriction.NONE);
      client = new Client(titanConfig);
   }

   @Test
   public void shutdown() throws Exception {
      Client client = TestGraphUtil.createClient();
      client.delegate().shutdown();
   }

   @AfterClass
   public static void cleanUpTables() throws Exception {
      TestGraphUtil.cleanUpTables();
   }

   @Test
   public void client() throws Exception {
      AmazonDynamoDB dynamoDBAsyncClient = client.delegate().client();
      assertNotNull(dynamoDBAsyncClient);
   }
}
