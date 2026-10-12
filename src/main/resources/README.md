<div align="center">

# 🎬 Netflix Movie Recommendation System

### Your next favorite movie is one good query away.

A Netflix-inspired movie discovery experience powered by **Spring Boot**, **Java**, and **AI embeddings via OpenRouter**.

<br/>

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.0-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![OpenRouter](https://img.shields.io/badge/Embeddings-OpenRouter-7C3AED?style=for-the-badge)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)

<br/>

**Discover by meaning—not just matching words.**

</div>

---

## 🍿 The idea

Movie discovery should feel less like filling out a database query and more like describing the mood you're in.

This project is a Netflix-inspired movie recommendation application. It exposes a Spring Boot API that uses text embeddings to compare a search query with movie descriptions and return relevant titles. A lightweight frontend lives in Spring Boot's static resources, so the API and interface can be served from the same application.

> **Example:** Search for `comedy` and the current dataset returns titles such as *The Truman Show*, *La La Land*, *Pulp Fiction*, *The Prestige*, and *Andhadhun*. The exact order depends on the embedding data and similarity ranking.

## ✨ What it does

- **Semantic movie search** — represents the query and movie descriptions as vectors, then ranks movies by similarity.
- **OpenRouter embeddings** — calls an OpenAI-compatible embeddings API using the `text-embedding-3-small` model.
- **Spring Boot REST API** — serves health and movie-search endpoints.
- **Netflix-inspired frontend** — static UI assets are served from `src/main/resources/static/`.
- **Local-first development** — run the application on your machine with Maven; no separate frontend server is required for the static page.

## 🧭 How a search travels through the app

```text
You type a mood, theme, or genre
             │
             ▼
     Static frontend (index.html)
             │  GET /movies/search?query=...
             ▼
       MovieController
             │
             ▼
        MovieService
             │
             ├── Loads movie metadata from movies.json
             │
             ├── Requests an embedding for the search query
             │
             ├── Compares query and movie vectors
             │
             └── Returns the top matching movies
             ▼
       JSON response → movie cards
```

The diagram describes the intended flow. Exact initialization and caching behaviour depends on the current implementation in `MovieService.java`.

## 🧰 Tech stack

| Technology | Role in this project |
|---|---|
| Java | Application language |
| Spring Boot | Web application and REST endpoints |
| Spring AI OpenAI integration | Embedding client configuration |
| OpenRouter | Embedding API provider |
| `text-embedding-3-small` | Text embedding model |
| Maven | Dependency management and build |
| HTML, CSS, JavaScript | Static frontend |
| `movies.json` | Movie metadata source |

## 🗂️ Project structure

```text
Netflix_Movie_Recommendation_System/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── Main.java
│   │   │   ├── MovieController.java
│   │   │   ├── MovieService.java
│   │   │   └── model/
│   │   │       ├── Movie.java
│   │   │       └── MovieData.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── movies.json
│   │       └── static/
│   │           ├── index.html
│   │           ├── recommendation ui.png
│   │           └── recommendation ui 2.heic
│   └── test/
└── README.md
```

The image filenames above reflect the assets visible in the project tree; rename them if you later standardize asset naming.

## 🚀 Get started

### Prerequisites

- JDK compatible with the project's configured Java version (**the current build uses Java 25**).
- Maven.
- An OpenRouter API key with access to the configured embeddings model.
- Internet connectivity for embedding requests.

Check your installed tools:

```bash
java -version
mvn -version
```

### 1. Clone the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd Netflix_Movie_Recommendation_System
```

Replace `<YOUR_REPOSITORY_URL>` with the actual Git repository URL.

### 2. Configure your API key

Create or generate an API key in your OpenRouter account. Set it as an environment variable in the same terminal used to launch the app:

**macOS / Linux (zsh or bash)**

```bash
export OPENROUTER_API_KEY='your-api-key-here'
```

Verify that it is set without printing the secret:

```bash
echo ${OPENROUTER_API_KEY:+API_KEY_IS_SET}
```

Expected output:

```text
API_KEY_IS_SET
```

> 🔐 **Keep credentials private.** Do not hardcode your key in `application.properties`, screenshots, commits, issue reports, or chat messages. If a key is accidentally exposed, revoke it and create a replacement.

### 3. Review application configuration

The current local configuration uses port `8081` and the OpenRouter embeddings endpoint. The relevant settings in `src/main/resources/application.properties` should be consistent with the Spring AI version and client used by the project.

A representative configuration is:

```properties
spring.application.name=Netflix_Movie_Recommendation_System

spring.ai.openai.base-url=https://openrouter.ai/api
spring.ai.openai.api-key=${OPENROUTER_API_KEY}

spring.ai.openai.embedding.base-url=https://openrouter.ai/api
spring.ai.openai.embedding.embeddings-path=/v1/embeddings
spring.ai.openai.embedding.options.model=text-embedding-3-small

server.port=8081
logging.level.org.example=INFO
```

**Configuration note:** Spring AI property support can vary by version. If embedding requests fail, check the actual dependency version and startup logs before changing properties. The environment variable must be present in the process that starts Spring Boot.

### 4. Build and run

Compile first:

```bash
mvn clean compile
```

Start the application:

```bash
mvn spring-boot:run
```

Keep that terminal running. The app should be available at:

**http://localhost:8081/**

Spring Boot serves `src/main/resources/static/index.html` at the root path when the file is present.

## 🔌 API quick reference

### Health check

```http
GET /api/health
```

Try it locally:

```bash
curl -i http://localhost:8081/api/health
```

A successful response currently looks like:

```json
{
  "application": "Netflix Movie Recommendation System",
  "status": "UP"
}
```

### Search movies

```http
GET /movies/search?query=comedy
```

Try it locally:

```bash
curl -i "http://localhost:8081/movies/search?query=comedy"
```

A successful response is a JSON array of movie objects:

```json
[
  {
    "title": "The Truman Show",
    "description": "A man begins to suspect that his seemingly ordinary life is an elaborate television production."
  },
  {
    "title": "La La Land",
    "description": "An aspiring actress and a jazz musician pursue their dreams while falling in love."
  }
]
```

The example is shortened for readability; the live endpoint may return more results. The current observed search returned five items.

### Try different searches

```bash
curl -G "http://localhost:8081/movies/search" --data-urlencode "query=artificial intelligence"
curl -G "http://localhost:8081/movies/search" --data-urlencode "query=romance and ambition"
curl -G "http://localhost:8081/movies/search" --data-urlencode "query=crime thriller"
```

Use `--data-urlencode` for phrases containing spaces or special characters.

## 🧪 Troubleshooting

### `OPENROUTER_API_KEY` is missing

Symptoms may include an unresolved placeholder or an authentication failure.

```bash
echo ${OPENROUTER_API_KEY:+API_KEY_IS_SET}
```

If nothing is printed, export the key in the terminal that launches the application and restart Spring Boot.

### Health works, but movie search returns `503`

The web server can be healthy while the embedding service is unavailable. Check the Spring Boot logs for the underlying exception and verify:

1. The API key is valid and has not been revoked.
2. The embedding model identifier is available through OpenRouter.
3. The account has sufficient quota/credits and is not rate-limited.
4. The base URL, embeddings path, and model property match the Spring AI client configuration.
5. The machine can reach OpenRouter.

Test the provider independently without exposing the key:

```bash
curl -sS https://openrouter.ai/api/v1/embeddings \
  -H "Authorization: Bearer $OPENROUTER_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "text-embedding-3-small",
    "input": "A comedy about friendship"
  }'
```

Do not paste the authorization header or API key into logs or bug reports. If the provider call succeeds but the app fails, investigate the Spring AI configuration and exception details.

### Port `8081` is already occupied

Stop the existing application or change `server.port` in `application.properties`, then use the new port in all URLs.

### Frontend loads, but results do not update

- Confirm the backend is running.
- Open the browser developer tools and inspect the Console and Network tabs.
- Check that the frontend requests `/movies/search?query=...` on the same origin.
- Confirm the response is valid JSON and handle non-2xx responses in the UI.

## 🛣️ Roadmap ideas

These are possible future improvements—not claims that they are already implemented.

- [ ] Add a larger, curated movie dataset with genres, release years, posters, and ratings.
- [ ] Add explicit similarity scores and tune ranking quality.
- [ ] Cache movie embeddings so application restarts do not require regenerating them.
- [ ] Add recommendation filters for genre, decade, language, and runtime.
- [ ] Create a movie details view with richer metadata.
- [ ] Add tests for the controller, embedding failures, and ranking logic.
- [ ] Add graceful handling for empty queries, provider timeouts, and quota limits.
- [ ] Add Docker support and deployment documentation.
- [ ] Improve accessibility, mobile layouts, loading states, and reduced-motion support.

## 🔒 Security notes

- Keep `OPENROUTER_API_KEY` outside source control.
- Ensure local environment files containing secrets are ignored by Git.
- Never log authorization headers or full credential-bearing request objects.
- Apply sensible request limits and timeouts before deploying publicly.
- Review API usage and provider costs before exposing the application to unrestricted traffic.

## 🤝 Contributing

1. Fork the repository.
2. Create a focused branch: `git checkout -b feat/your-improvement`.
3. Make the change and run `mvn clean compile`.
4. Add or update tests where appropriate.
5. Open a pull request describing the change and how it was verified.

---

<div align="center">

<sub>Netflix-inspired fan project. Not affiliated with or endorsed by Netflix.</sub>

</div>
