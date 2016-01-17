# CsvToAvro

There is used gradle build system to manage dependencies.
To compile the code and do the testing run
gradle clean test

To build 'fat jar' (with dependencies included) which later can be used with java -jar command run
gradle fatJar

Fat jar is included with the source code, in the jars directory.

You can run CsvToAvro in two ways:
- passing input .csv fila and specifying output avro file, which is going to be created:
java -jar jars/csvtoavro-all-1.0.jar CsvToAvro --schema products.avsc --input input_csv_file --output output_avro_file
- or by reading csv input from STDIN and writting output to STDOUT (notice -c parameter informing program to read/write from/to stdin/stdout)
e.g.
java -jar jars/csvtoavro-all-1.0.jar CsvToAvro --schema products.avsc -c < ../1gb_data.csv > test/1gb_stream.avro

