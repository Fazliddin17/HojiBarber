FROM openjdk:17
ADD target/HojiBarber.jar app.jar
VOLUME /simple.app
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "/app.jar"]