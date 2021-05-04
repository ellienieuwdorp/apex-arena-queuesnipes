FROM adoptopenjdk/openjdk11:latest
RUN mkdir /opt/app
COPY build/libs/*.jar /opt/app/app.jar
COPY build/libs/*.wav /opt/app/
CMD ["java", "-jar", "/opt/app/app.jar"]