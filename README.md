# RentScope

RentScope is an Android application developed as a final-year Computer Engineering project. The platform helps users identify the best locations to live by combining housing prices with quality-of-life indicators such as education, healthcare, and safety.

The application processes public datasets, calculates a personalized score for each municipality, and presents the results through an interactive choropleth map, allowing users to compare regions and make informed decisions.

<table align="center">
  <tr>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/8efd1381-cec3-4a1c-a4f0-86349d550d7c" width="250"/><br>
      <b>Home Screen</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/b746e431-0381-4db1-9db1-937ec9f74827" width="250"/><br>
      <b>AI Assistant</b>
    </td>
    <td align="center">
      <img src="https://github.com/user-attachments/assets/6b315387-9f52-4cb4-9970-c22ae78001fc" width="250"/><br>
      <b>Interactive Map</b>
    </td>
  </tr>
</table>

## Live Demo

Backend API:
https://rentscope-api-w1b7.onrender.com

Swagger Documentation:
https://rentscope-api-w1b7.onrender.com/docs


## Features

- Interactive choropleth map visualization
- Municipality ranking system
- Personalized filtering system
- Historical housing price analysis
- Favorites management
- User authentication and account management
- AI-powered assistant for location insights
- Multi-language support
- Responsive Material Design 3 interface

## Technology Stack

### Mobile Application
- Kotlin
- Jetpack Compose
- Material Design 3
- Navigation Compose
- Retrofit
- Moshi
- Google Maps Compose

### Backend
- FastAPI
- Python
- Uvicorn
- JWT Authentication

### Database
- PostgreSQL
- Supabase

### Data Processing
- Python
- Pandas
- ETL Pipelines

## Project Architecture

The project follows a client-server architecture where the Android application communicates with a FastAPI backend through REST APIs. The backend is responsible for business logic, authentication, score calculation, and data aggregation, while PostgreSQL hosted on Supabase stores all application data.

## Data Sources

The application uses publicly available datasets, including:

- Housing rental prices
- Educational institutions
- Healthcare facilities
- Crime statistics
- Administrative geographic boundaries (CAOP)

All datasets are processed and normalized through ETL pipelines before being stored in the database.

## Score Calculation

RentScope generates a municipality score based on multiple indicators selected by the user.

The scoring system combines:

- Housing affordability
- Education availability
- Healthcare availability
- Safety indicators

All values are normalized before being combined, ensuring fair comparisons between municipalities of different sizes and preventing larger cities from dominating the rankings solely due to population or infrastructure volume.

## Authentication & Security

The platform implements:

- Password hashing
- JWT authentication
- Email verification
- Secure API communication
- Protected endpoints

Only the minimum amount of user information required for system functionality is collected and stored.

## AI Assistant

RentScope includes an AI assistant capable of providing contextual information about municipalities based on available data and user-selected locations.

The assistant can answer questions regarding:

- Housing costs
- Safety indicators
- Educational infrastructure
- General app insights

## Deployment

### Backend
- Render
- Supabase PostgreSQL

### Mobile Application
- Android Studio
- APK Distribution

## Future Improvements

- Additional countries and datasets
- Advanced comparison tools
- Recommendation system
- Real-time data updates
- Enhanced AI capabilities

## Author

Júlia Santos  
Bachelor's Degree in Computer Engineering  
Universidade Lusófona – Centro Universitário do Porto

## License

This project was developed for academic purposes as part of a final-year Computer Engineering project.
