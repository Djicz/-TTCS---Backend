# Bước 1: Sử dụng Maven để build dự án
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Bước 2: Sử dụng JDK siêu nhẹ để chạy ứng dụng
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
# Copy file jar từ bước build sang (nhớ kiểm tra tên file jar trong thư mục target)
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]