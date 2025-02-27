/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.flywaydb.core.internal.database.postgresql;

import java.sql.Connection;
import java.sql.SQLException;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.database.base.Database;
import org.flywaydb.core.internal.database.base.Table;
import org.flywaydb.core.internal.jdbc.JdbcConnectionFactory;
import org.flywaydb.core.internal.jdbc.StatementInterceptor;
import org.flywaydb.core.internal.util.StringUtils;

public class PostgreSQLDatabase extends Database<PostgreSQLConnection> {
    public PostgreSQLDatabase(
            Configuration configuration,
            JdbcConnectionFactory jdbcConnectionFactory,
            StatementInterceptor statementInterceptor) {
        super(configuration, jdbcConnectionFactory, statementInterceptor);
    }

    @Override
    protected PostgreSQLConnection doGetConnection(Connection connection) {
        return new PostgreSQLConnection(this, connection);
    }

    @Override
    public String getInsertStatement(Table table) {
        return "INSERT INTO " + table
                + " (" + quote("installed_rank")
                + ", " + quote("version")
                + ", " + quote("description")
                + ", " + quote("type")
                + ", " + quote("script")
                + ", " + quote("checksum")
                + ", " + quote("installed_by")
                + ", " + quote("execution_time")
                + ", " + quote("success")
                + ")"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    public void ensureSupported() {
        ensureDatabaseIsRecentEnough("9.0");

        ensureDatabaseNotOlderThanOtherwiseRecommendUpgradeToFlywayEdition(
                "10", org.flywaydb.core.internal.license.Edition.ENTERPRISE);

        recommendFlywayUpgradeIfNecessaryForMajorVersion("15");
    }

    @Override
    public String getRawCreateScript(Table table, boolean baseline) {
        String tablespace =
                configuration.getTablespace() == null ? "" : " TABLESPACE \"" + configuration.getTablespace() + "\"";

        return "CREATE TABLE " + table.getName() + " (\n" + "    \"installed_rank\" INT NOT NULL,\n"
                + "    \"version\" VARCHAR(50),\n"
                + "    \"description\" VARCHAR(200) NOT NULL,\n"
                + "    \"type\" VARCHAR(20) NOT NULL,\n"
                + "    \"script\" VARCHAR(1000) NOT NULL,\n"
                + "    \"checksum\" INTEGER,\n"
                + "    \"installed_by\" VARCHAR(100) NOT NULL,\n"
                + "    \"installed_on\" TIMESTAMP NOT NULL DEFAULT now(),\n"
                + "    \"execution_time\" INTEGER NOT NULL,\n"
                + "    \"success\" BOOLEAN NOT NULL DEFAULT FALSE\n"
                + ")"
                + tablespace + ";\n" + "ALTER TABLE "
                + table.getName() + " ADD CONSTRAINT \"" + table.getName() + "_pk\" PRIMARY KEY (\"installed_rank\");\n"
                + "CREATE INDEX \""
                + table.getName() + "_s_idx\" ON " + table.getName() + " (\"success\");";
    }

    @Override
    protected String doGetCurrentUser() throws SQLException {
        return getMainConnection().getJdbcTemplate().queryForString("SELECT current_user");
    }

    @Override
    public boolean supportsDdlTransactions() {
        return true;
    }

    @Override
    public boolean supportsChangingCurrentSchema() {
        return true;
    }

    @Override
    public String getBooleanTrue() {
        return "TRUE";
    }

    @Override
    public String getBooleanFalse() {
        return "FALSE";
    }

    @Override
    protected String doQuote(String identifier) {
        return pgQuote(identifier);
    }

    static String pgQuote(String identifier) {
        return "\"" + StringUtils.replaceAll(identifier, "\"", "\"\"") + "\"";
    }

    @Override
    public boolean catalogIsSchema() {
        return false;
    }

    @Override
    public boolean useSingleConnection() {
        return true;
    }

    /**
     * This exists to fix this issue: https://github.com/flyway/flyway/issues/2638
     * See https://www.pgpool.net/docs/latest/en/html/runtime-config-load-balancing.html
     */
    @Override
    public String getSelectStatement(Table table) {
        return "/*NO LOAD BALANCE*/\n"
                + "SELECT " + quote("installed_rank")
                + "," + quote("version")
                + "," + quote("description")
                + "," + quote("type")
                + "," + quote("script")
                + "," + quote("checksum")
                + "," + quote("installed_on")
                + "," + quote("installed_by")
                + "," + quote("execution_time")
                + "," + quote("success")
                + " FROM " + table
                + " WHERE " + quote("installed_rank") + " > ?"
                + " ORDER BY " + quote("installed_rank");
    }
}
