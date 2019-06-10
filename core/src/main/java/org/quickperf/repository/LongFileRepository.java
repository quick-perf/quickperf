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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.repository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LongFileRepository implements LongRepository {

    private final LongMemoryRepository longMemoryRepository = new LongMemoryRepository();

    private final ObjectInputStreamBuilder objectInputStreamBuilder = ObjectInputStreamBuilder.INSTANCE;

    private final ObjectOutputStreamBuilder objectOutputStreamBuilder = ObjectOutputStreamBuilder.INSTANCE;

    @Override
    public void save(long longToSave, String workingFolderPath, String fileName) {

        ObjectOutputStream objectOutputStream = objectOutputStreamBuilder
                                                .build(workingFolderPath, fileName);
        try {
            objectOutputStream.writeLong(longToSave);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to save " + "", ioe);
        }

    }

    @Override
    public Long find(String workingFolderPath, String fileName) {

        Long longValueFromMemory = longMemoryRepository.find(workingFolderPath, fileName);

        if(longValueFromMemory == null) {
            Long longValueFromFile = retrieveLongValueFromFile(workingFolderPath, fileName);
            longMemoryRepository.save(longValueFromFile, workingFolderPath, fileName);
            return longValueFromFile;
        }

        return longValueFromMemory;
    }

    private Long retrieveLongValueFromFile(String workingFolderPath, String fileName) {
        ObjectInputStream objectInputStream = objectInputStreamBuilder.buildObjectInputStream(workingFolderPath, fileName);

        try {
            return objectInputStream.readLong();
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to deserialize.", ioe);
        }
    }

}
