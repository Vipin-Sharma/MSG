#!/bin/bash
# Wait to be sure that SQL Server came up
sleep 30s

# Run the setup script to create the DB and the schema in the DB
echo 'Create sakila db schema'
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password@1 -d master -i sakila_db_schema.sql

echo 'Insert sakila db data'
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password@1 -d master -i sakila_db_data.sql

echo 'Database is ready'
