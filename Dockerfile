FROM public.ecr.aws/amazoncorretto/amazoncorretto:21-al2023

WORKDIR /app

# Копируем локально собранный JAR
COPY target/*.jar app.jar

# Проверяем, что JAR существует
RUN test -f app.jar || (echo "JAR file not found!" && exit 1)

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]