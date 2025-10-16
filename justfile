# Development ==========================================================================================================
run_tests:
    mvn clean install

run_application:
    mvn spring-boot:run

run_tests_local_facilitator:
    mvn clean install -Dspring.profiles.active=development

run_application_local_facilitator:
    mvn spring-boot:run -Dspring-boot.run.profiles=development

# Docker ===============================================================================================================
build_docker_image:
    mvn spring-boot:build-image -P release

# Release ==============================================================================================================
start_release:
    git remote set-url origin git@github.com:mogami-tech/x402-examples .git
    git checkout development
    git pull
    git status
    mvn gitflow:release-start

finish_release:
    mvn gitflow:release-finish -DskipTests
