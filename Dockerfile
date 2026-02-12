# 使用预装 Maven 和 JDK21 的镜像
FROM maven:3.9-amazoncorretto-21
WORKDIR /app

# Copy only the necessary source code and configuration files
COPY pom.xml .
COPY src ./src

# Perform packaging with Maven
RUN mvn clean package -DskipTests

# Expose the application port
EXPOSE 8123

# Start the app with the production configuration
CMD ["java", "-jar", "/app/target/yu-ai-agent-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]