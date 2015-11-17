package com.rapid7.graph;

import com.amazonaws.services.dynamodbv2.AbstractAmazonDynamoDB;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import org.apache.commons.configuration.BaseConfiguration;

/**
 * Connection class for connecting to Titan instance.
 */
public class Connetion
{
   /**
    * Sets the backend connection and uses the TitanFactory to create connection.
    */
   public void connect()
   {
      BaseConfiguration baseConfiguration = new BaseConfiguration();
      baseConfiguration.setProperty("storage.backend", "cassandra");
      baseConfiguration.setProperty("storage.hostname", "192.168.1.10");

      TitanGraph titanGraph = TitanFactory.open(baseConfiguration);

   }
}
