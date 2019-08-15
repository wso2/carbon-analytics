# WSO2 Streaming Integrator Studio Docker Artifacts

Docker artifacts in WSO2 Streaming Integrator Studio can be used to build Docker containers with Siddhi files.

## Directory Structure

In WSO2 Streaming Integrator Studio, Docker artifacts can be created for either **Editor** profile or **Worker** profile. Directory structure of the ZIP file is as follows.

```
.
├── README.md
├── docker-compose.yml
└── workspace
    ├── <SIDDHI_FILE_1>.siddhi
    └── <SIDDHI_FILE_2>.siddhi
```


Purpose of each file in the above archive is as follows.

- **README.md**: This readme file.
- **docker-compose.yml**: Docker Compose file which contains Docker configurations to build and run the Docker container.
- **siddhi-files**: Directory which contains Siddhi files.

## How to Run?

To run this archive, following applications are required.

- Docker
- Docker Compose

Once the above prerequisites are installed in your environment, follow the steps mentioned below to create and run the docker image.

1. Unzip the docker-artifact.zip archive file. The extracted directory will be referred as `<DOCKER_HOME>` within this document.

2. Go to `<DOCKER_HOME>` directory.

3. Run the following command to start the Docker container.

```
docker-compose up
```


