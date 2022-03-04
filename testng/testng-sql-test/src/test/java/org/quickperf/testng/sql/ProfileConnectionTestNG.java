/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2022 the original author or authors.
 */
package org.quickperf.testng.sql;

import org.quickperf.sql.annotation.ProfileConnection;
import org.quickperf.sql.connection.Level;
import org.quickperf.writer.WriterFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProfileConnectionTestNG {

    static final String FILE_PATH = findTargetPath() + File.separator + "testng-connection-profiling.txt";

    private static String findTargetPath() {
        Path targetDirectory = Paths.get("target");
        return targetDirectory.toFile().getAbsolutePath();
    }

    public static class ProfileConnectionClass extends SqlTestBaseTestNG {
        @ProfileConnection(level = Level.TRACE
                          , displayStackTrace = true
                          , writerFactory = FileWriterBuilder.class
                          )
        @Test
        public void test() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
                statement.executeQuery();
            }
        }
    }

    public static class FileWriterBuilder implements WriterFactory {

        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(FILE_PATH);
        }

    }

}
