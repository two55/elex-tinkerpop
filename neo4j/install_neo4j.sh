#!/usr/bin/env bash

# select community edition version 3.4.1
neo4j_version=neo4j-community-3.4.1
#wget "https://neo4j.com/artifact.php\?name\=${neo4j_version}-unix.tar.gz" -O "${neo4j_version}-unix.tar.gz"
wget "http://dist.neo4j.org/${neo4j_version}-unix.tar.gz" -O "${neo4j_version}-unix.tar.gz"
mkdir -p data/neo4j/
tar -xf "${neo4j_version}-unix.tar.gz" -C data/neo4j/