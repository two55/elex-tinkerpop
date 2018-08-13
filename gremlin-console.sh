#!/usr/bin/env bash

# extract the script's location
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

# valid modes
INTERACTIVE_MODE="interactive"
EXECUTE_MODE="execute"

print_usage() {
  echo $1
  echo "Usage:"
  echo "    $0"
  echo "                 ...just runs the Gremlin console interactively with no Groovy script being executed in advance."
  echo "    $0 [--${INTERACTIVE_MODE}|--${EXECUTE_MODE}] <groovy-script> [<groovy-script-parameters>]"
  echo "                 ...executes the passed Groovy script with (--${EXECUTE_MODE}) or without (--${INTERACTIVE_MODE}) "
  echo "                    exiting the console after the script is done."
  echo ""
  echo "    $0 --help"
  echo "                 ...prints this usage information."
}

# special parameter handling for printing the usage
if [[ "$1" == "--help" ]]; then
  print_usage "This script executes the Gremlin console."
  exit 0
fi

# parsing the script parameters
while [[ $# -gt 0 ]]; do
case $1 in
  -cp|--classpath)
    # --classpath enables the setting of additional libraries which shall be available in the shell
    classpath_ext="$2:"; shift; shift; ;;
  -s|--script)
    # use --script for specifying a Groovy script that shall run within the shell
    groovy_script="$2"; shift; shift; ;;
  -m|--mode)
    # defining the available modes (interactive vs execute)
    if [[ "$2" != "${INTERACTIVE_MODE}" ]] && [[ "$2" != "${EXECUTE_MODE}" ]]; then
      # wrong mode specified
      print_usage "Invalid execution mode specified: '$2'"
      exit 1
    fi

    execution_mode="--$2"; shift; shift; ;;
  *)    # unknown option
    # any other parameter value will be considered a parameter in the passed script and passed forward
    groovy_script_parameters="${groovy_script_parameters} $1"; shift; ;;
esac
done

if ([[ "$groovy_script" == "" ]] && [[ "${execution_mode}" != "" ]]) \
    || ([[ "$groovy_script" != "" ]] && [[ "${execution_mode}" == "" ]]); then
  print_usage "Either --script and --mode or none of them has to be set."
  exit 1
fi

# we don't specify the version in the filename to be independent of the Maven build version
jar_filepath="${script_dir}/janusgraph/target/elex-janusgraph-*-jar-with-dependencies.jar"

# create the jar-with-dependencies JAR if it is not present, yet
if [[ "$(ls ${jar_filepath} 2> /dev/null | wc -l)" != "1" ]]; then
  mvn -f ${script_dir}/pom.xml -T1C -P fatjar -pl janusgraph -am package -DskipTests
fi

# run the console
java -cp ${classpath_ext}${jar_filepath} org.apache.tinkerpop.gremlin.console.Console --quiet ${execution_mode} ${groovy_script} ${groovy_script_parameters}
