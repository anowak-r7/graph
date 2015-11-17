package com.rapid7.diskstorage.dynamodb;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.diskstorage.BackendException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Created by root on 11/17/15.
 */
public class SingleACESTest extends AbstractACESTest
{
   private static TitanGraph GRAPH;

   @BeforeClass
   public static void setUpGraph() throws Exception {
      GRAPH = TestGraphUtil.instance().openGraph(BackendDataModel.MULTI);
      //AbstractMarvelTest.loadData(GRAPH, 100 /* Number of lines to read from marvel.csv */);
   }

   @AfterClass
   public static void tearDownGraph() throws BackendException
   {
    //  TestGraphUtil.tearDownGraph(GRAPH);
   }

   @Override
   protected TitanGraph getGraph()
   {
      return GRAPH;
   }
}
