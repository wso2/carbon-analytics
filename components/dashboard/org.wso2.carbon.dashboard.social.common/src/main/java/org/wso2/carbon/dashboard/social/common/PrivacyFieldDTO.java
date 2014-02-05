
/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.dashboard.social.common;

/*
*    Class to  represent the privacy values of user profile fields
*
* */
public class PrivacyFieldDTO {


    private String fieldName;
       private String visibilityValue;

       public String getFieldName() {
           return fieldName;
       }

       public void setFieldName(String fieldName) {
           this.fieldName = fieldName;
       }

       public String getVisibilityValue() {
           return visibilityValue;
       }

       public void setVisibilityValue(String visibilityValue) {
           this.visibilityValue = visibilityValue;
       }

    

}
