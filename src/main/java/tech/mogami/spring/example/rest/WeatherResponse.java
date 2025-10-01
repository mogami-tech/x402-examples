package tech.mogami.spring.example.rest;

public record WeatherResponse(Report report) {
    public record Report(String weather, int temperature) {
    }
}

