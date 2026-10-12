# Netflix Movie Recommendation System

Spring Boot + Spring AI + OpenRouter semantic movie search, with a static HTML frontend.

## Important: rotate the exposed API key

The uploaded project contained a literal OpenRouter API key in `application.properties`.
Treat that key as compromised: revoke it in OpenRouter and create a new one. Do not commit the new key.

## Requirements

- Java 25 (the `pom.xml` is configured for Java 25)
- Maven 3.9+
- A valid OpenRouter API key with access to `openai/text-embedding-3-small`

## Run on macOS

From the project root:

```bash
export OPENROUTER_API_KEY='YOUR_NEW_OPENROUTER_API_KEY'
mvn spring-boot:run
```

The ZIP does not include a Maven wrapper executable, so use an installed Maven 3.9+.

The application is configured for port **8081** because port 8080 was occupied by Jenkins on the machine in the reported logs.

Open the UI:

```text
http://127.0.0.1:8081/
```

Check the app is running without calling the embedding provider:

```bash
curl -i http://127.0.0.1:8081/api/health
```

Search movies (the first search generates embeddings for all movie descriptions):

```bash
curl -i 'http://127.0.0.1:8081/movies/search?query=comedy'
```

The frontend is in `src/main/resources/static/index.html`. It calls `/movies/search` using a relative URL, so the browser automatically uses the same host and port as the page.

## Troubleshooting

- `Connection refused`: the app is not listening on that port; check the startup log and `lsof -nP -iTCP:8081 -sTCP:LISTEN`.
- Jenkins HTML / `403 Authentication required` on port 8080: that request reached Jenkins, not this Spring Boot app.
- `/api/health` works but movie search returns `503`: the server is up, but OpenRouter embeddings failed. Check `OPENROUTER_API_KEY`, account quota, model access, and the Spring Boot logs.
- Do not open `index.html` directly as a `file://` URL; open it through Spring Boot so its relative API URL works.
