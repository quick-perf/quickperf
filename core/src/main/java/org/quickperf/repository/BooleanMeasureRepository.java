/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.repository;

import org.quickperf.WorkingFolder;
import org.quickperf.measure.BooleanMeasure;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BooleanMeasureRepository {

    public static final BooleanMeasureRepository INSTANCE = new BooleanMeasureRepository();

    private BooleanMeasureRepository() { }

    private final ObjectInputStreamBuilder objectInputStreamBuilder = ObjectInputStreamBuilder.INSTANCE;

    private final ObjectOutputStreamBuilder objectOutputStreamBuilder = ObjectOutputStreamBuilder.INSTANCE;

    public void save(BooleanMeasure booleanMeasure, WorkingFolder workingFolder, String fileName) {
        String workingFolderPath = workingFolder.getPath();
        try(ObjectOutputStream objectOutputStream = objectOutputStreamBuilder
                                                   .build(workingFolderPath, fileName)
        ) {
            objectOutputStream.writeBoolean(booleanMeasure.getValue());
            objectOutputStream.flush();
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to save " + fileName, ioe);
        }
    }

    public BooleanMeasure find(WorkingFolder workingFolder, String fileName) {
        boolean booleanValue = retrieveBooleanValueFromFile(workingFolder, fileName);
        return BooleanMeasure.of(booleanValue);
    }

    private boolean retrieveBooleanValueFromFile(WorkingFolder workingFolder, String fileName) {
        String workingFolderPath = workingFolder.getPath();
        try(ObjectInputStream objectInputStream = objectInputStreamBuilder
                                                 .buildObjectInputStream(workingFolderPath, fileName)
        ) {
            return objectInputStream.readBoolean();
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to deserialize.", ioe);
        }
    }

}
