/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */

 define(function() {
     var TemplateConfigBlocks = function () {
        this.sampleDatasourceConfig =  'dataSources: \n' +
           '  - name: SIDDHI_TEST_DB \n' +
           '    description: The datasource used for test database \n' +
           '    jndiConfig: \n' +
           '      name: jdbc/SIDDHI_TEST_DB \n' +
           '    definition: \n' +
           '      type: RDBMS \n' +
           '      configuration: \n' +
           '        jdbcUrl: jdbc:mysql://hostname:port/testdb \n' +
           '        username: root \n' +
           '        password: root \n' +
           '        driverClassName: com.mysql.jdbc.Driver \n' +
           '        maxPoolSize: 10 \n' +
           '        idleTimeout: 60000 \n' +
           '        connectionTestQuery: SELECT 1 \n' +
           '        validationTimeout: 30000 \n' +
           '        isAutoCommit: false \n';
        this.sampleMetricsConfig =  'metrics: \n' +
           '    # The root level configured for Metrics \n' +
           '    rootLevel: INFO \n' +
           '    levels: \n' +
           '      jvm.buffers: \'OFF\' \n' +
           '      jvm.class-loading: INFO \n' +
           '      jvm.gc: DEBUG \n' +
           '      jvm.memory: INFO \n';
        this.sampleExtensionsConfig =  'extensions: \n' +
           '  - \n' +
           '    extension: \n' +
           '      name: extension_name \n' +
           '      namespace: extension_namespace \n' +
           '      properties: \n' +
           '        key: value \n';
        this.sampleRefsConfig = 'refs: \n' +
           '  - \n' +
           '    ref: \n' +
           '      name: \'name\' \n' +
           '      type: \'type\' \n' +
           '      properties: \n' +
           '        property1: value1 \n' +
           '        property2: value2 \n';
        this.sampleTransportConfig =  'wso2.carbon: \n' +
           '  id: siddhi-runner \n' +
           '  name: Siddhi Runner Distribution \n' +
           '  hostnameVerificationEnabled: true \n';
     }
     TemplateConfigBlocks.prototype.constructor = TemplateConfigBlocks;
     TemplateConfigBlocks.prototype.getTemplatedConfig = function () {
        var self = this;
        return {
            "sampleDatasourceConfig": self.sampleDatasourceConfig,
            "sampleMetricsConfig": self.sampleMetricsConfig,
            "sampleExtensionsConfig": self.sampleExtensionsConfig,
            "sampleRefsConfig": self.sampleRefsConfig,
            "sampleTransportConfig": self.sampleTransportConfig
        }
     }
     return TemplateConfigBlocks;
 });