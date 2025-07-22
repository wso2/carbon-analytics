# Siddhi Kubernetes Artifacts

Exported Kubernetes artifacts from Siddhi Tooling can be used to deploy exported Siddhi apps in a Kubernetes cluster.


## Directory Structure

The directory structure of the exported kubernetes artifacts zip file is as follows.

```
.
├── bundles
│   ├── <BUNDLE_FILE_1>.jar
│   └── <BUNDLE_FILE_2>.jar
├── configurations.yaml
├── Dockerfile
├── jars
│   └── <JAR_FILE_1>.jar
├── README.md
├── siddhi-process.yaml
└── siddhi-files
    ├── <SIDDHI_FILE_1>.siddhi
    └── <SIDDHI_FILE_2>.siddhi
```


Purpose of each file in the above archive is as follows.

- **README.md**: This readme file.
- **Dockerfile**: Docker image build script which contains all commands to assemble Streaming Integrator image. 
- **siddhi-files**: Directory which contains Siddhi files. (Note: These Siddhi files are already added in `siddhi-process.yaml`)
- **siddhi-process.yaml**: `SiddhiProcess` YAML that used to deploy Siddhi apps directly in a Kubernetes cluster using Kubernetes custom resource definition.
- **bundles**: Directory maintained for OSGI bundles which needs to be copied to Streaming Integrator image during the build phase.
- **jars**: Directory maintained for Jar files which may not have their corresponding OSGi bundle implementation. These Jars will be converted as OSGI bundles and copied to Streaming Integrator image during the build phase.

## Steps to Run

### Install Siddhi Operator

```sh
kubectl apply -f ./k8-prerequisites.yaml
kubectl apply -f ./siddhi-operator.yaml
```

Note: Replace <SIDDHI-OPERATOR-VERSION> with the version of the Siddhi operator that you want to use. Refer [Siddhi Operator releases](https://github.com/siddhi-io/siddhi-operator/releases) for more details about Siddhi operator releases.

### Install the Siddhi App
 
Use the following command to install exported SiddhiProcess.

```sh
kubectl apply -f siddhi-process.yaml
```

### Sample Output

After the installation, you can see the deployed Kubernetes artifacts as below. Note that this may change according to your use case and K8s deployment type.

```sh
$ kubectl get SiddhiProcesses
NAME          AGE
monitor-app   2m

$ kubectl get deployment
NAME              DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
monitor-app       1         1         1            1           1m
siddhi-operator   1         1         1            1           1m

$ kubectl get service
NAME              TYPE           CLUSTER-IP       EXTERNAL-IP   PORT(S)          AGE
kubernetes        ClusterIP      <IP>             <none>        443/TCP          10d
monitor-app       ClusterIP      <IP>             <none>        8280/TCP         1m
siddhi-operator   ClusterIP      <IP>             <none>        8383/TCP         1m

$ kubectl get ingress
NAME      HOSTS     ADDRESS     PORTS     AGE
siddhi    siddhi    10.0.2.15   80, 443   1m
```

For more details refer to the [Siddhi documentation on running as a kubernetes service](https://siddhi.io/en/v5.1/docs/siddhi-as-a-kubernetes-microservice/).
