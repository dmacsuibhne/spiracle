/*
 *  Copyright 2014 Waratek Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.waratek.spiracle.performance;

import com.waratek.spiracle.file.AbstractFileServlet;
import com.waratek.spiracle.sql.util.SelectUtil;
import com.waratek.spiracle.sql.util.UpdateUtil;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 */
@WebServlet("/DBPerf")
public class DBPerf extends AbstractFileServlet
{
    private HttpServletRequest request;
    private ServletContext application;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public DBPerf()
    {
        super();
    }

    protected void executeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        this.request = request;
        application = request.getServletContext();
        final String table1 = request.getParameter("table1");
        final String table2 = request.getParameter("table2");
        createPerfTable(table1);
        createPerfTable(table2);
        insertAndSelectData(table1, table2);
    }

    private void insertAndSelectData(String table1, String table2)
    {
        String sqlInsertSeed = "INSERT INTO " + table1 + " VALUES(0, TO_DATE('1989-12-09','YYYY-MM-DD'), 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb', 0, TO_DATE('1989-12-09','YYYY-MM-DD'), 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb', 0, TO_DATE('1989-12-09','YYYY-MM-DD'), 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb')";
        try
        {
            UpdateUtil.executeUpdateWithoutNewPage(sqlInsertSeed, application, request);
            long counter = 0;
            ArrayList<Object> selectedData = selectData(table1, counter);
            long startTime = System.currentTimeMillis();
            StringBuilder builder = new StringBuilder();
            while (true)
            {
                counter++;
                //Modify data (table1)
                modifySelectedData(selectedData);
                //insert data (table2)
                insertData(table2, selectedData);
                //select new data (table2)
                selectedData = selectData(table2, counter);

                counter++;
                //Modify data (table2)
                modifySelectedData(selectedData);
                //insert data (table1)
                insertData(table1, selectedData);
                //select new data (table1)
                selectedData = selectData(table1, counter);

                // StringBuilder performance experiment
                final String varchar2Value = (String) selectedData.get(2);
                builder.append(varchar2Value);
                if (builder.length() >= 32000)
                {
                    logger.info("Builder length: " + builder.length());
                    builder = new StringBuilder();
                }





//                if (counter % 2000 == 0)
                if (builder.length() == 0)
                {
                    final long timeElapsed = System.currentTimeMillis() - startTime;
                    final BigDecimal rowCount = countRows(table2);
                    logger.info("Rows: " + rowCount + ", Elapsed time=" + timeElapsed + "millis, Builder length=" + builder.length() + ", Selected data: " + selectedData);
                    startTime = System.currentTimeMillis();
                }
            }
        }
        catch (SQLException | IOException e)
        {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private BigDecimal countRows(String tableName) throws SQLException, IOException
    {
        final String sqlSelect = "SELECT COUNT(*) FROM " + tableName;
        ArrayList<ArrayList<Object>> resultList;
        resultList = SelectUtil.executeQueryWithoutNewPage(sqlSelect, application, request);

        //        logger.info("Selection: " + resultList);
        return (BigDecimal) resultList.get(0).get(0);
    }

    private void insertData(String tableName, ArrayList<Object> data)
            throws SQLException
    {
        final int numRowsPerInsert = 100;
        final String valueRow = "(" + data.get(0) + ", TO_DATE('" + data.get(1) + "','YYYY-MM-DD'), '" + data.get(2) + "', '" + data.get(3) + "', "  + data.get(4) + ", TO_DATE('" + data.get(5) + "','YYYY-MM-DD'), '" + data.get(6) + "', '" + data.get(7) + "', "  + data.get(8) + ", TO_DATE('" + data.get(9) + "','YYYY-MM-DD'), '" + data.get(10) + "', '" + data.get(11) + "')";
        final String valueRowWithCommaAndNewline = valueRow + ",\n";
        final StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO " + tableName + " VALUES\n");
        for (int i = 0; i < numRowsPerInsert - 1; i++)
        {
            builder.append(valueRowWithCommaAndNewline);
        }
        builder.append(valueRow);
        final String sqlInsert = builder.toString();

        UpdateUtil.executeUpdateWithoutNewPage(sqlInsert, application, request);
    }

    private void modifySelectedData(ArrayList<Object> data)
    {
        BigDecimal col0Value = ((BigDecimal) data.get(0)).add(new BigDecimal(1));
        data.set(0, col0Value);
        String col1Value = data.get(1).toString().substring(0, 10); //cut off time data so that it remain a valid date to insert
        data.set(1, col1Value);
        String col2Value = data.get(2).toString().substring(0, 20) + data.get(2).toString().substring(0, 20);
        data.set(2, col2Value);
        String col3Value = data.get(3).toString().substring(0, 20) + data.get(3).toString().substring(0, 20);
        data.set(3, col3Value);

        BigDecimal col4Value = ((BigDecimal) data.get(4)).add(new BigDecimal(1));
        data.set(4, col4Value);
        String col5Value = data.get(5).toString().substring(0, 10); //cut off time data so that it remain a valid date to insert
        data.set(5, col5Value);
        String col6Value = data.get(6).toString().substring(0, 20) + data.get(6).toString().substring(0, 20);
        data.set(6, col6Value);
        String col7Value = data.get(7).toString().substring(0, 20) + data.get(7).toString().substring(0, 20);
        data.set(7, col7Value);

        BigDecimal col8Value = ((BigDecimal) data.get(8)).add(new BigDecimal(1));
        data.set(8, col8Value);
        String col9Value = data.get(9).toString().substring(0, 10); //cut off time data so that it remain a valid date to insert
        data.set(9, col9Value);
        String col10Value = data.get(10).toString().substring(0, 20) + data.get(10).toString().substring(0, 20);
        data.set(10, col10Value);
        String col11Value = data.get(11).toString().substring(0, 20) + data.get(11).toString().substring(0, 20);
        data.set(11, col11Value);
    }

    private ArrayList<Object> selectData(String tableName, long row)
            throws SQLException, IOException
    {
        final String sqlSelect = "SELECT * FROM " + tableName + " WHERE col0=" + row;
        ArrayList<ArrayList<Object>> resultList;
        resultList = SelectUtil.executeQueryWithoutNewPage(sqlSelect, application, request);

        //        logger.info("Selection: " + resultList);
        return resultList.get(0);
    }

    private void dropPerfTableIfExists(String tableName)
    {
        final String sqlDropTable = "DROP TABLE " + tableName;
        try
        {
            UpdateUtil.executeUpdateWithoutNewPage(sqlDropTable, request.getServletContext(), request);
        }
        catch (SQLException e)
        {
            logger.info("'" + sqlDropTable + "' failed, probably the table doesn't exist. Error msg = " + e.getMessage());
        }
    }

    private void createPerfTable(String tableName)
    {
        logger.info("Dropping " + tableName + " table if it exists already");
        dropPerfTableIfExists(tableName);
        final String sqlCreateTable = "CREATE TABLE " + tableName + " (col0 INT, col1 DATE, col2 VARCHAR2(240), col3 VARCHAR2(60), col4 INT, col5 DATE, col6 VARCHAR2(240), col7 VARCHAR2(60), col8 INT, col9 DATE, col10 VARCHAR2(240), col11 VARCHAR2(60))";

        try
        {
            logger.info("Creating " + tableName + " table");
            UpdateUtil.executeUpdateWithoutNewPage(sqlCreateTable, application, request);
        }
        catch (SQLException e)
        {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}