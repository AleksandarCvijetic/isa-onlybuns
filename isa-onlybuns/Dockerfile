# --- build stage -------------------------------------------------
FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /src

# copy only this module + parent POMs to speed up rebuilds
COPY pom.xml .
COPY isa-onlybuns/pom.xml isa-onlybuns/
COPY loadbalancer/pom.xml loadbalancer/
RUN mvn -q -pl isa-onlybuns -am dependency:go-offline

# copy sources and build the right jar
COPY . .
RUN mvn -pl isa-onlybuns clean package -DskipTests

# --- run stage ---------------------------------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/isa-onlybuns/target/onlybuns-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
