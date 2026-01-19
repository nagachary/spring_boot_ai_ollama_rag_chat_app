  # Use a base image with Java installed
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
# Assuming your Spring Boot application builds a JAR named target/your-application-name.jar
COPY target/spring_boot_ai_ollama_rag_chat_app-0.0.1-SNAPSHOT.jar spring_boot_ai_ollama_rag_chat_app.jar

# Expose the port your Spring Boot application listens on (default is 8080)
EXPOSE 8087

# Command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "spring_boot_ai_ollama_rag_chat_app.jar"]