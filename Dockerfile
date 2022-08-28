FROM maven:3.5-jdk-8-alpine as builder

COPY . /app/src

RUN mvn -f /app/src/pom.xml clean package

FROM ibmjava:8-sdk

COPY --from=builder /app/src/charset/target/opensmpp-charset-3.0.3-SNAPSHOT.jar /app/charset.jar
COPY --from=builder /app/src/client/target/opensmpp-client-3.0.3-SNAPSHOT.jar /app/client.jar
COPY --from=builder /app/src/core/target/opensmpp-core-3.0.3-SNAPSHOT.jar /app/core.jar
COPY --from=builder /app/src/sim/target/opensmpp-sim-3.0.3-SNAPSHOT.jar /app/sim.jar
COPY sim/users.txt /app/etc/users.txt

WORKDIR /app

EXPOSE 2775

ENV CLASSPATH=".:charset.jar:client.jar:core.jar:sim.jar"

ENTRYPOINT ["java", "org.smpp.smscsim.Simulator"]