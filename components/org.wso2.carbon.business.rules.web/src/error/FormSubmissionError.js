/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * The Error which is thrown when a Form for a business rule is submitted with erroneous properties
 */
export default class FormSubmissionError extends Error {
    /**
     * Constructs a FormSubmissionError with the given states of the fields after the error,
     * and the remaining params including the message
     * @param {object} fieldErrorStates     Object containing States of the form fields after the error
     * @param {params} params               Rest of the params, including the error message
     */
    constructor(fieldErrorStates, ...params) {
        // Pass remaining arguments to the parent constructor
        super(...params);

        // Maintains proper stack trace for where this error was thrown
        if (Error.captureStackTrace) {
            Error.captureStackTrace(this, FormSubmissionError);
        }
        this.fieldErrorStates = fieldErrorStates;
    }
}
