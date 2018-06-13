# Entity Landscape Explorer - TinkerPop
This project targets at providing an easily configurable web-frontend to explore a TinkerPop enabled graph database.

## Configuration
Update the configuration `janusgraph/src/main/resources/application.yaml` according to your requirements:
* `server.port` defines the port on which the server will listen (default: 8080)
* `graph.type` defines the graph type (i.e., janusgraph)
* `graph.janusgraph.config` specifies the janusgraph configuration resource. 
   This project contains two example configuration files: 
   `elex-janusgraph/embedded.properties` (default selection) and `elex-janusgraph/cassandra-elasticsearch.properties`
   Further details about the JanusGrapf configuration can be found in the 
   [official documentation](https://docs.janusgraph.org/0.2.0/configuration.html).
* `graph.display.nodes` configures the visual properties of different node types in the graph display:
   * type
   * labelAttribute
   * weightAttribute
   * color
   * icon
   * explore
   * hasDetails
* `graph.display.links` configures the visual properties of different link types in the graph display:
   * type
   * labelAttribute
   * weightAttribute
   * color
   * explore
   * isDirected
   * hasDetails

## Building/running the backend from source
 * Build backend from source
   ```bash
   mvn clean install
   ```
 * Create a sample database and run the web server
   ```
   mvn --projects janusgraph spring-boot:run -Drun.arguments="--createExampleGraph"
   ```
   THe database can be dropped using
   ```bash
   rm -rf data
   ```
 * Running the web server
   ```bash
   mvn --projects janusgraph spring-boot:run
   ```

## Building frontend from source
In order to build the frontend, we use [Docker](https://www.docker.com/):

 * Make sure you checked-out a (current) version of the frontend submodule
   ```bash
   git submodule update --init frontend
   ```
 * Build the frontend sources and export the resulting javascript/html files in order to be deployed with the core module
   ```bash
   bash ./build_frontend.sh
   ```

## License
The Entity Landscape Explorer is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE) for the full license text.

