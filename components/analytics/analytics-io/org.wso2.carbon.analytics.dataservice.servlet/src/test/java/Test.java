/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    public static void main(String[] args) {
//        Map<String, AnalyticsSchema.ColumnType> cols = new HashMap<>();
//        cols.put("a", AnalyticsSchema.ColumnType.STRING);
//        cols.put("b", AnalyticsSchema.ColumnType.STRING);
//        cols.put("k", AnalyticsSchema.ColumnType.STRING);
//        List<String> prim = new ArrayList<>();
//        prim.add("a");
//        prim.add("b");
//        AnalyticsSchema schema = new AnalyticsSchema(cols,prim);
//        List<String> schema = new ArrayList<>();
//        schema.add("xxxxx");
//        schema.add("yyyyy");
//        schema.add("11111");
//        String json = new GsonBuilder().create().toJson(schema);
//        System.out.println(json);
//
//        Type listType = new TypeToken<List<String>>() {
//        }.getType();
//        List<String> yourClassList = new Gson().fromJson(json, listType);
//        System.out.println(json);
    }
}
