/*
 * Copyright (C) 2020 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.klinker.messenger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Before;
import org.robolectric.RuntimeEnvironment;

import xyz.klinker.messenger.shared.data.DataSource;
import xyz.klinker.messenger.shared.data.DatabaseSQLiteHelper;
import xyz.klinker.messenger.shared.util.FixtureLoader;

import static org.mockito.Mockito.spy;

public abstract class MessengerRealDataSuite extends MessengerRobolectricSuite {

    public DataSource source;
    protected Context context = spy(RuntimeEnvironment.application);

    @Before
    public void setUp() throws Exception {
        SQLiteDatabase database = SQLiteDatabase.create(null);
        DatabaseSQLiteHelper helper = new DatabaseSQLiteHelper(RuntimeEnvironment.application);
        helper.onCreate(database);

        source = DataSource.INSTANCE;
        source.set_database(database);
        insertData();
    }

    private void insertData() throws Exception {
        SQLiteDatabase database = source.get_database();
        FixtureLoader loader = new FixtureLoader();
        loader.loadFixturesToDatabase(database);
    }

}
