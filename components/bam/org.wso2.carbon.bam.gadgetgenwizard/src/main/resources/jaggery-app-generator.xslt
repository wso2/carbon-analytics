<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
  ~  Copyright (c) 2005-2010, WSO2 Inc. (http://wso2.com) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  ~
  -->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
                xmlns:m0="http://services.samples"
                xmlns:gg="http://wso2.com/bam/gadgetgen"
                exclude-result-prefixes="gg m0 fn">
    <xsl:output method="text" omit-xml-declaration="yes" indent="yes"/>

    <xsl:template match="/gg:gadgetgen">


        <xsl:text disable-output-escaping="yes">&#60;</xsl:text><xsl:text>&#37;</xsl:text>
        var config = {};
        
        var db = null;
        var result = null;
        
        try {
           db = new Database("<xsl:value-of select="gg:jdbcurl" />",
            "<xsl:value-of select="gg:username"/>", "<xsl:value-of select="gg:password"/>", config);
    	   result = db.query("<xsl:value-of select="gg:sql"/>");
        } finally {
           if (db != null) {
               db.close();
           }
        }
        
        var respJson = null;

        <xsl:choose>
            <xsl:when test="gg:BarChart">
                <xsl:call-template name="BarChart"/>
            </xsl:when>
            <xsl:when test="gg:Table">
                <xsl:call-template name="Table"/>
            </xsl:when>
        </xsl:choose>

        print(respJson);
   		 <xsl:text>&#37;</xsl:text><xsl:text disable-output-escaping="yes">&#62;</xsl:text>

        </xsl:template>

    <xsl:template name="BarChart">
        var colx = "<xsl:value-of select="gg:bar-xcolumn" />";
    	var coly = "<xsl:value-of select="gg:bar-ycolumn" />";

    	function convertDBResult(result, colx, coly) {
			var array = [];
			for (var i = 0; i <xsl:text disable-output-escaping="yes">&#60;</xsl:text> result.length; i++) {
				// print("The " + i + "th result is : " + result[i]);
				array.push([result[i][colx],parseFloat(result[i][coly])]);
			}

			return array;
        };
        respJson = convertDBResult(result, colx, coly);
    </xsl:template>

    <xsl:template name="Table">
        function createTableJSON(result) {
            var len = result.length;
            var rows = [];
            var columnNames = []
            for (var i = 0; i <xsl:text disable-output-escaping="yes">&#60;</xsl:text> len; i++) {
                var obj = result[i];
                var row = []
                for(var k in obj) {
                    if ({}.hasOwnProperty.call(obj, k)) {
                        if (i == 0) {
                            columnNames.push(k);
                        }
                        row.push(obj[k]);
                    }
                }
                rows.push(row);
            }
            return {Rows: rows, ColumnNames : columnNames};
        }

        respJson = createTableJSON(result);
    </xsl:template>

</xsl:stylesheet>