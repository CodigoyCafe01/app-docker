FROM openjdk:23-oraclelinux8
LABEL authors="codigo&cafe"

COPY ./target/codigo-cafe-0.0.1-SNAPSHOT.jar /opt/app/codigo-cafe.jar
WORKDIR /opt/app
RUN sh -c 'touch codigo-cafe.jar'
EXPOSE 8716
ENTRYPOINT ["java", "-Dspring.output.ansi.enabled=ALWAYS", "-jar","codigo-cafe.jar"]