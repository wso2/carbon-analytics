/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util;

import java.nio.file.Path;

/**
 * Resolves an untrusted user-specified path against the API's base directory.
 */
public class SecurityUtil {
    /**
     * Resolves an untrusted user-specified path against the API's base directory.
     * Paths that try to escape the base directory are rejected.
     *
     * @param baseDirPath the absolute path of the base directory that all user-specified paths should be within.
     * @param userPath    the untrusted path provided by the API user, expected to be relative to {@code baseDirPath}
     */
    public static Path resolvePath(final Path baseDirPath, final Path userPath) {
        final Path resolvedPath = baseDirPath.resolve(userPath).normalize();
        if (!baseDirPath.isAbsolute()) {
            throw new IllegalArgumentException("Base path must be absolute");
        }
        if (userPath.isAbsolute()) {
            throw new IllegalArgumentException("User path must be relative");
        }
        if (!resolvedPath.startsWith(baseDirPath)) {
            throw new IllegalArgumentException("User path escapes the base path");
        }
        return resolvedPath;
    }
}
