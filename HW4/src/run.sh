#!/bin/bash


rm build/*
rm -rf output
mkdir output
hdfs dfs -rm -R /output
hdfs dfs -mkdir /output
javac -classpath `hadoop classpath` -d build TextAnalyzer.java -Xlint
jar -cvf TextAnalyzer.jar -C build/ .
hadoop jar TextAnalyzer.jar TextAnalyzer /input output
hdfs dfs -copyToLocal output

