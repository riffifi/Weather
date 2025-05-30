# Weather App

An Android weather application that provides current weather conditions and forecasts using the OpenWeather API.

## Setup Instructions

### API Key Configuration

This app uses the OpenWeather API which requires an API key. For security reasons, the API key is not included in the repository.

To set up the project with your own API key:

1. Copy the template file:
   ```
   cp gradle.properties.template gradle.properties
   ```

2. Edit the `gradle.properties` file and replace `your_api_key_here` with your actual OpenWeather API key:
   ```
   OPEN_WEATHER_API_KEY=your_actual_api_key_here
   ```

3. The app will automatically use this API key during build time.

### Building the Project

After configuring your API key:

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run the application

## Features

- Current weather conditions
- 5-day weather forecast
- Location search
- Unit conversion

## Note for Contributors

- Never commit your `gradle.properties` file as it contains sensitive API keys
- Always use the BuildConfig field to access the API key in code
- Follow the existing code style and architecture patterns
