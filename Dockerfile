FROM mcr.microsoft.com/mssql/server:2022-latest

# Switch to root to install dependencies
USER root

# Install curl for downloading files
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create directory for Sakila scripts
RUN mkdir -p /sakila

# Download Sakila database files during build
RUN echo "📥 Downloading Sakila database files..." && \
    curl -sL -o /sakila/sql-server-sakila-schema.sql \
        "https://raw.githubusercontent.com/ivanceras/sakila/master/sql-server-sakila-db/sql-server-sakila-schema.sql" && \
    curl -sL -o /sakila/sql-server-sakila-insert-data.sql \
        "https://raw.githubusercontent.com/ivanceras/sakila/master/sql-server-sakila-db/sql-server-sakila-insert-data.sql" && \
    echo "✅ Sakila files downloaded successfully"

# Create initialization script
RUN echo '#!/bin/bash\n\
set -e\n\
echo "🚀 Starting SQL Server with Sakila database setup..."\n\
\n\
# Start SQL Server in background\n\
/opt/mssql/bin/sqlservr &\n\
SQLSERVER_PID=$!\n\
\n\
# Wait for SQL Server to be ready\n\
echo "⏳ Waiting for SQL Server to be ready..."\n\
for i in {1..30}; do\n\
    if /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "$MSSQL_SA_PASSWORD" -C -Q "SELECT 1" > /dev/null 2>&1; then\n\
        echo "✅ SQL Server is ready!"\n\
        break\n\
    fi\n\
    echo "⏳ Attempt $i/30: SQL Server not ready yet..."\n\
    sleep 5\n\
done\n\
\n\
# Check if Sakila database exists\n\
echo "🔍 Checking if Sakila database exists..."\n\
DB_EXISTS=$(/opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "$MSSQL_SA_PASSWORD" -C -h -1 -Q "SELECT COUNT(*) FROM sys.databases WHERE name = '\''sakila'\''" 2>/dev/null | tr -d " \\t\\n\\r" || echo "0")\n\
\n\
if [ "$DB_EXISTS" = "1" ]; then\n\
    echo "✅ Sakila database already exists - skipping setup"\n\
else\n\
    echo "🗄️ Setting up Sakila database..."\n\
    echo "📋 Creating Sakila database schema..."\n\
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "$MSSQL_SA_PASSWORD" -C -i /sakila/sql-server-sakila-schema.sql\n\
    echo "📊 Inserting Sakila sample data (this may take a few minutes)..."\n\
    /opt/mssql-tools18/bin/sqlcmd -S localhost -U SA -P "$MSSQL_SA_PASSWORD" -C -i /sakila/sql-server-sakila-insert-data.sql\n\
    echo "🎉 Sakila database setup completed!"\n\
fi\n\
\n\
echo ""\n\
echo "📋 Connection Details:"\n\
echo "   Server: localhost:1433"\n\
echo "   Database: sakila"\n\
echo "   Username: sa"\n\
echo "   Password: $MSSQL_SA_PASSWORD"\n\
echo ""\n\
echo "🎯 MSG is ready to generate microservices with rich Sakila sample data!"\n\
echo "   - 599 customers"\n\
echo "   - 1000 films"\n\
echo "   - 16,000+ rental transactions"\n\
echo ""\n\
\n\
# Keep SQL Server running\n\
wait $SQLSERVER_PID\n\
' > /usr/local/bin/init-sakila-db.sh && chmod +x /usr/local/bin/init-sakila-db.sh

# Switch back to mssql user
USER mssql

# Set the custom initialization script as entrypoint
ENTRYPOINT ["/usr/local/bin/init-sakila-db.sh"]