package com.rapid7.diskstorage.dynamodb;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.Iterator;

/**
 * Created by root on 11/17/15.
 */
public class FactoryOfSomeShit
{

private static String testing = "testing";

   private static String testing2 = "testing";
   private static String testing3 = "testinginginging";

   public static void process(TitanGraph graph) {
      Vertex comicBookVertex = get(graph, testing, "Testing");
      if (null == comicBookVertex) {
         comicBookVertex = graph.addVertex(null);
         comicBookVertex.setProperty(testing, "Testing");
      }
      Vertex characterVertex = get(graph, testing, "Testing");
      if (null == characterVertex) {
         characterVertex = graph.addVertex(null);
         characterVertex.setProperty(testing2, "Testing2");
      }
      characterVertex.addEdge(testing3, comicBookVertex);
      graph.commit();
   }

   private static Vertex get(TitanGraph graph, String key, String value) {
      Iterator<Vertex> it = graph.getVertices(key, value).iterator();
      Vertex vertex = null;
      if (it.hasNext()) {
         vertex = it.next();
      }
      return vertex;

   }
}
