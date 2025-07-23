# loadbalancer/Dockerfile

FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /app

RUN apt-get update \
 && apt-get install -y maven \
 && rm -rf /var/lib/apt/lists/*

# Copy the root parent POM
COPY pom.xml ./

# Copy *both* modules so Maven sees them
COPY loadbalancer ./loadbalancer
COPY isa-onlybuns ./isa-onlybuns


# Now you can pre‑fetch and build just the loadbalancer module:
RUN mvn dependency:go-offline -pl loadbalancer
RUN mvn clean package        \
    -pl loadbalancer         \
    -am                       \
    -DskipTests

FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/loadbalancer/target/loadbalancer-*.jar lb.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/lb.jar"]
