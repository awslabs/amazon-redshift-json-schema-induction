# Description

This library provide an API and Cli utility to perform schema induction on JSON data. 
In addition , it can generate DDL for Redshift external tables to allow user to perform PartiQL queries over the data in the table.
Last , the library can generate a json tree to represent the learned schema of the data and annotate it with types from the formal 
schema of the data (if one exists)

# CLI Usage

        java -jar aws-json-schema-induction.jar 
                                [-ah] [-c=<s3>] [-d=<outDdlFile>] -i=<inputFile> 
                                [-l=<tableLocation>] [-r=<region>] [-root=<rootDefinition>]
                                [-s=<outSchemaFile>] [--stats=<outStatsFile>]
                                [-t=<tableName>]
              -a, --array               is the document a json array
              -c, --cred=<s3>           which type of s3 credentials to use (ec2|profile)
              -d, --ddl=<outDdlFile>    An output ddl file for Redshift
              -h, --help                display a help message
              -i, --input=<inputFile>   An input json file path.
              -l, --location=<tableLocation>
                                        table location to use when creating DDL
              -r, --region=<region>     s3 region to use
                  -root, --root=<rootDefinition>
                                        s3 region to use
              -s, --schema=<outSchemaFile>
                                        An output schema file json file path
                  --stats=<outStatsFile>
                                        An output stats file
              -t, --table=<tableName>   table name to use when creating DDL 
## Author

AWS Professional Services