/*
 * Copyright 2011 The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yourbase.jdbc.ddl;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.yourbase.jdbc.TestBase;

public class CreateHBaseTableTest extends TestBase {
    @Test
    public void run() throws Exception {
        createTableSQL("CREATE HBASE TABLE IF NOT EXISTS CreateHBaseTableTest (" //
                //此OPTIONS对应org.apache.hadoop.hbase.HTableDescriptor的参数选项
                + "OPTIONS(DEFERRED_LOG_FLUSH='false'), "

                //COLUMN FAMILY中的OPTIONS对应org.apache.hadoop.hbase.HColumnDescriptor的参数选项
                + "COLUMN FAMILY cf1 (" + //
                		"OPTIONS(MIN_VERSIONS=2, KEEP_DELETED_CELLS=true), " + //
                		"f1 int, " + //
                		"f2 varchar, " + //
                		"f3 date" //
                + ")," //

                + "COLUMN FAMILY cf2 (" + //
                		"OPTIONS(MIN_VERSIONS=2, KEEP_DELETED_CELLS=true)" //
                + ")," //
                
                + "COLUMN FAMILY cf3" //
        + ")");
        
        stmt.executeUpdate("INSERT INTO CreateHBaseTableTest(_rowkey_, f1, f2, f3) VALUES('01', 10, 'b', CURRENT_DATE)");
        //cf2.f1是动态字段
        stmt.executeUpdate("INSERT INTO CreateHBaseTableTest(_rowkey_, f1, f2, f3, cf2.f1) "
                + "VALUES('01', 10, 'b', CURRENT_DATE, CURRENT_TIME)");

        //cf2.f1是动态字段，虽然在cf2中未定义它的类型，但是前面第一次insert时用了CURRENT_TIME，所以就确定为time类型
        //这次的'invalid time'是字符串，所以是非法的。
        try {
            stmt.executeUpdate("INSERT INTO CreateHBaseTableTest(_rowkey_, f1, f2, f3, cf2.f1) "
                    + "VALUES('01', 10, 'b', CURRENT_DATE, 'invalid time')");
            Assert.fail("not throw SQLException");
        } catch (SQLException e) {
            assertTrue(e.getMessage().contains("invalid time"));
        }
        
        String db = org.h2.util.HBaseUtils.HBASE_DB_NAME;
        sql = "SELECT " + db + ".public.CreateHBaseTableTest.cf1.f1 FROM CreateHBaseTableTest";
        sql = "SELECT public.CreateHBaseTableTest.cf1.f1 FROM CreateHBaseTableTest";
        sql = "SELECT CreateHBaseTableTest.cf1.f1 FROM CreateHBaseTableTest";
        sql = "SELECT cf1.f1 FROM CreateHBaseTableTest";
        sql = "SELECT f1 FROM CreateHBaseTableTest";
        sql = "SELECT * FROM CreateHBaseTableTest";
        printResultSet();
    }
}
