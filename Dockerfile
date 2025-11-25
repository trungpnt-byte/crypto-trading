FROM gradle:8.7-jdk21-jammy AS builder

# Set the working directory
WORKDIR /app
ARG JAR_FILE=/build/libs/cryto-trading-service.jar
COPY ${JAR_FILE} build/libs/app.jar
# Copy the Gradle wrapper files and source code
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle.kts .
## Copy all source files
#COPY src src

# Make the wrapper executable
#RUN chmod +x gradlew

# Run the build command.
# We use the 'bootJar' task to create the executable fat JAR.
#RUN ./gradlew clean bootJar -x test

ENV JAVA_MODULES="java.base,java.logging,java.management,java.naming,java.net.http,java.sql,java.security.jgss,java.xml"

#RUN #$JAVA_HOME/bin/jlink \
#    --module-path build/libs/app.jar:build/libs \
#    --add-modules ${JAVA_MODULES} \
#    --compress=2 \
#    --no-header-files \
#    --no-man-pages \
#    --output custom-jre \
RUN jlink \
    --module-path $JAVA_HOME/jmods \
    --add-modules java.base,java.naming,java.instrument,java.logging,java.management,java.rmi,jdk.jfr,jdk.unsupported,java.sql,java.desktop,jdk.crypto.ec,java.security.sasl,jdk.security.auth,jdk.security.jgss \
    --output /opt/custom-jre \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2


# Stage 2: Application image
FROM alpine:3.16

# Copy the custom JRE from the previous stage
COPY --from=builder /opt/custom-jre /opt/custom-jre

# Set environment variables to use the custom JRE
ENV JAVA_HOME=/opt/custom-jre
ENV PATH="$JAVA_HOME/bin:$PATH"

ARG JAR_FILE=/app/build/libs/*.jar
COPY --from=builder ${JAR_FILE} app.jar

RUN adduser -D springuser
USER springuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]