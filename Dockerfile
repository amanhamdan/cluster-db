FROM openjdk:8-jdk-alpine
WORKDIR . .
ADD target/ClusterDB-Node-1.jar ClusterDB-Node-1.jar
VOLUME ./Databases
VOLUME ./DatabaseUsers
VOLUME ./Schemas
EXPOSE 8080
COPY ./src/main/resources/application.properties ./src/main/resources/application.properties
COPY ./Databases ./Databases
COPY ./DatabaseUsers ./DatabaseUsers
COPY ./Schemas ./Schemas
ENTRYPOINT ["java","-jar","ClusterDB-Node-1.jar"]

