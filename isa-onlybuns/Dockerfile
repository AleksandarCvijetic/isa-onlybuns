# isa-onlybuns/Dockerfile

FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /app

RUN apt-get update \
 && apt-get install -y maven \
 && rm -rf /var/lib/apt/lists/*

COPY pom.xml ./
COPY isa-onlybuns ./isa-onlybuns
COPY loadbalancer ./loadbalancer

RUN mvn dependency:go-offline -pl isa-onlybuns
RUN mvn clean package -pl isa-onlybuns -am -DskipTests

FROM eclipse-temurin:17-jre-focal
WORKDIR /app

COPY --from=builder /app/isa-onlybuns/target/onlybuns-*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
