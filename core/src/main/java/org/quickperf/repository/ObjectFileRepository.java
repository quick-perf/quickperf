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
package org.quickperf.repository;

import org.quickperf.WorkingFolder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectFileRepository {

    public static final ObjectFileRepository INSTANCE = new ObjectFileRepository();

    private final ObjectInputStreamBuilder objectInputStreamBuilder = ObjectInputStreamBuilder.INSTANCE;

    private final ObjectOutputStreamBuilder objectOutputStreamBuilder = ObjectOutputStreamBuilder.INSTANCE;

    public static ObjectFileRepository getInstance() {
        return INSTANCE;
    }

    public void save(WorkingFolder workingFolder, String fileName, Object object) {
        save(workingFolder.getPath(), fileName, object);
    }

    public void save(String workingFolderPath, String fileName, Object object) {
        try(ObjectOutputStream objectOutputStream = objectOutputStreamBuilder
                                                   .build(workingFolderPath, fileName)) {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw buildSerializationException(e);
        }
    }

    private IllegalStateException buildSerializationException(Exception e) {
        return new IllegalStateException("Unable to save failures.", e);
    }

    public Object find(String workingFolderPath, String fileName) {
        try(ObjectInputStream ois = objectInputStreamBuilder
                                   .buildObjectInputStream(workingFolderPath, fileName)) {
            return ois.readObject();
        } catch (IOException|ClassNotFoundException e) {
            throw buildDeserializationException(e);
        }
    }

    private IllegalStateException buildDeserializationException(Exception e) {
        return new IllegalStateException("Unable to deserialize failures.", e);
    }

}
