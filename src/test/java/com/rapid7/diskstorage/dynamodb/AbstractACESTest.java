package com.rapid7.diskstorage.dynamodb;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.tinkerpop.blueprints.Vertex;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class AbstractACESTest
{
   public static final String CHARACTER = "character";
   @Test
   public void AcesQuery() {
      final TitanGraph graph = getGraph();

      FactoryOfSomeShit.process(graph);




      graph.getVertex("testing");
     // Vertex results = graph.getVertex("ACES");
      //results.getId();
   }


   protected abstract TitanGraph getGraph();
}
