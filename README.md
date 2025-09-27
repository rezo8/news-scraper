# NewsScraper Project

## Problem Statement
The goal of this project is to build a system that can:
1. Fetch news articles from various online sources.
2. Publish the fetched articles to a Kafka topic for distributed processing.
3. Consume the articles from Kafka and process them to extract meaningful insights.

## Objectives
- **News Scraping**: Implement a scraper to fetch news articles from multiple sources.
- **Kafka Integration**: Set up a Kafka producer to publish the scraped articles and a Kafka consumer to process them.
- **Data Processing**: Analyze the consumed articles to extract insights such as sentiment analysis, keyword extraction, or topic categorization.

## Deliverables
1. A working Scala application that:
   - Scrapes news articles.
   - Publishes articles to Kafka.
   - Consumes and processes articles from Kafka.
2. Documentation explaining the design and implementation.
3. Unit tests to ensure the correctness of the implementation.

## Suggested Steps
1. **Set Up Kafka**:
   - Install and configure Kafka locally or use a managed service.
   - Create a topic for publishing news articles.

2. **Implement News Scraper**:
   - Use libraries like `jsoup` to scrape articles from websites.
   - Structure the data (e.g., title, content, source, timestamp).

3. **Integrate Kafka Producer**:
   - Serialize the scraped data and publish it to the Kafka topic.

4. **Integrate Kafka Consumer**:
   - Consume messages from the Kafka topic.
   - Deserialize and process the data.

5. **Data Processing**:
   - Implement basic processing logic (e.g., sentiment analysis, keyword extraction).

6. **Testing and Validation**:
   - Write unit tests for each component.
   - Validate the end-to-end workflow.

## Evaluation Criteria
- Correctness: Does the application meet the objectives?
- Code Quality: Is the code clean, modular, and well-documented?
- Scalability: Can the system handle a large volume of articles?
- Testing: Are there sufficient tests to ensure reliability?


## How to Run
1. Ensure you have `sbt` installed.
2. Run the application using:
   ```
   sbt run
   ```

## Future Enhancements
- Add functionality to scrape news articles.
- Integrate Kafka for publishing and consuming messages.
- Implement processing logic for analyzing news content.
