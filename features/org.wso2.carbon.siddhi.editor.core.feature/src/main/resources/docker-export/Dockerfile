# ------------------------------------------------------------------------
#
# Copyright 2019 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

# use streaming-integrator-base-image
FROM {{SIDDHI_RUNNER_BASE_IMAGE}}
MAINTAINER WSO2 Docker Maintainers "dev@wso2.org"

ARG STREAMING_INTEGRATOR_HOME={{RUNTIME_PATH}}
ARG HOST_BUNDLES_DIR=./.bundles
ARG HOST_JARS_DIR=./.jars
ARG HOST_APPS_DIR=./siddhi-files
ARG JARS=${STREAMING_INTEGRATOR_HOME}/.jars
ARG BUNDLES=${STREAMING_INTEGRATOR_HOME}/.bundles
ARG APPS=${STREAMING_INTEGRATOR_HOME}/wso2/server/deployment/siddhi-files
ARG CONFIG_FILE=./configurations.yaml
ARG CONFIG_FILE_PATH=${HOME}/configurations.yaml

# copy bundles & jars to the siddhi-runner distribution
{{APPS_BLOCK}}
{{JARS_BLOCK}}
{{BUNDLES_BLOCK}}
{{CONFIGURATION_BLOCK}}

{{ENV_BLOCK}}

# expose ports
EXPOSE 9090 9443 9712 9612 7711 7611 7070 7443

STOPSIGNAL SIGINT

ENTRYPOINT ["{{RUNTIME_PATH}}/bin/server.sh" {{CONFIGURATION_PARAMETER_BLOCK}}]
