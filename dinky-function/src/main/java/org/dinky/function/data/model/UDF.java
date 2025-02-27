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

package org.dinky.function.data.model;

import org.apache.flink.table.catalog.FunctionLanguage;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/** @since 0.6.8 */
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UDF {

    /** 函数名 */
    String name;
    /** 类名 */
    String className;
    /** udf 代码语言 */
    FunctionLanguage functionLanguage;
    /** udf源代码 */
    String code;
    /**
     * en: Compile the artifact path <br />
     * zh: 编译产物路径
     */
    String compilePackagePath;
}
