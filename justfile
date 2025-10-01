# Development ==========================================================================================================
run_tests:
    mvn clean install

run_application:
    mvn spring-boot:run -Dspring-boot.run.profiles=development

# Docker ===============================================================================================================
build_docker_image:
    mvn spring-boot:build-image -P release
