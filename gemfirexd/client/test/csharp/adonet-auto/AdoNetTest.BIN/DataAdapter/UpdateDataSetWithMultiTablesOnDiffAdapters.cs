/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
 
using System;
using System.Data;
using System.Data.Common;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using Pivotal.Data.GemFireXD;


namespace AdoNetTest.BIN.DataAdapter
{
    /// <summary>
    /// Performs GFXDDataAdapter updates on multiple tables within a data set. Each data set's 
    /// table is populated by a different adapter.
    /// </summary>
    class UpdateDataSetWithMultiTablesOnDiffAdapters : GFXDTest
    {
        public UpdateDataSetWithMultiTablesOnDiffAdapters(ManualResetEvent resetEvent)
            : base(resetEvent)
        {
        }

        public override void Run(object context)
        {
            int dsSize = 10;
            DataSet dataset = new DataSet();
            String[] tableNames = new String[dsSize];
            GFXDCommand[] commands = new GFXDCommand[dsSize];
            GFXDDataAdapter[] adapters = new GFXDDataAdapter[dsSize];

            try
            {
                for (int i = 0; i < dsSize; i++)
                {
                    tableNames[i] = DbRandom.BuildRandomTable(5);
                    commands[i] = Connection.CreateCommand();
                    commands[i].CommandText = String.Format(
                        "SELECT * FROM {0} ORDER BY COL_ID ASC ", tableNames[i]);

                    adapters[i] = commands[i].CreateDataAdapter();
                    adapters[i].Fill(dataset, tableNames[i]);                    
                }

                ParseDataSet(dataset);

                IList<object> data = DbRandom.GetRandomRowData();

                for (int i = 0; i < dsSize; i++)
                {
                    CommandBuilder = new GFXDCommandBuilder(adapters[i]);

                    for (int j = 0; j < dataset.Tables[tableNames[i]].Rows.Count; j++)
                        for (int k = 1; k < dataset.Tables[tableNames[i]].Columns.Count; k++) // do not update identity column
                            dataset.Tables[tableNames[i]].Rows[j][k] = data[k];

                    if (adapters[i].Update(dataset, tableNames[i])
                        != dataset.Tables[tableNames[i]].Rows.Count)
                        Fail(String.Format(
                            "Failed to update all changed rows in table {0}", tableNames[i]));
                }

                dataset.Clear();

                for (int i = 0; i < dsSize; i++)
                    adapters[i].Fill(dataset, tableNames[i]);

                ParseDataSet(dataset);

                foreach (DataTable table in dataset.Tables)
                {
                    foreach (DataRow row in table.Rows)
                    {
                        for (int i = 1; i < row.Table.Columns.Count; i++)
                        {
                            if (!DbRandom.Compare(data[i], row, i))
                            {
                                Fail(String.Format(
                                    "Inconsistent updated data in table [{0}] at row [{1}] column [{2}]. "
                                    + "Expected [{3}]; Actual [{4}]",
                                    table.TableName,
                                    row[0].ToString(), row.Table.Columns[i].ColumnName,
                                    data[i].ToString(), row[i].ToString()));
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {                
                Fail(e);
            }
            finally
            {
                try
                {
                    foreach(String tableName in tableNames)
                        DbRandom.DropTable(tableName);
                }
                catch (Exception e)
                {
                    Fail(e);
                }

                base.Run(context);
            }
        }
    }
}
