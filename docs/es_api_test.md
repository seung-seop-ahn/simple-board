### Elastic Search API Test

```curl
# Set index
PUT /article
{
  "settings": {
    "analysis": {
      "tokenizer": {
        "nori_tokenizer": {
          "type": "nori_tokenizer"
        }
      },
      "analyzer": {
        "nori_analyzer": {
          "type": "custom",
          "tokenizer": "nori_tokenizer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "long"
      },
      "contents": {
        "type": "text",
        "analyzer": "nori_analyzer"
      },
      "created_date": {
        "type": "date",
        "format": "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
      },
      "title": {
        "type": "text",
        "analyzer": "nori_analyzer"
      },
      "updated_date": {
        "type": "date",
        "format": "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
      },
      "author_id": {
        "type": "long"
      },
      "author_name": {
        "type": "text",
        "analyzer": "nori_analyzer"
      },
      "board_id": {
        "type": "long"
      },
      "is_deleted": {
        "type": "boolean"
      }
    }
  }
}

# Status: yello (Check "Stack Management > Index Management")
GET /_cat/indices 

# Status is yello because it's single node cluster
PUT /article/_settings
{
  "index": {
    "number_of_replicas": 0
  }
}

# Status: green
GET /_cat/indices 

# Insert Data
PUT /article/_doc/1
{
  "contents": "testing",
  "created_date": "2024-09-11T18:40:45.858322",
  "title": "testing",
  "updated_date": "2024-09-12T11:48:08.531508",
  "author_name": "username",
  "author_id": 1,
  "board_id": 1,
  "is_deleted": true
}

PUT /article/_doc/2
{
  "contents": "test",
  "created_date": "2024-09-11T18:40:45.858322",
  "title": "test",
  "updated_date": "2024-09-12T11:48:08.531508",
  "author_name": "username",
  "author_id": 1,
  "board_id": 1,
  "is_deleted": true
}

PUT /article/_doc/3
{
  "contents": "테스트1",
  "created_date": "2024-09-11T18:40:45.858322",
  "title": "테스트",
  "updated_date": "2024-09-12T11:48:08.531508",
  "author_name": "username",
  "author_id": 1,
  "board_id": 1,
  "is_deleted": true
}

PUT /article/_doc/4
{
  "contents": "테스트2",
  "created_date": "2024-09-11T18:40:45.858322",
  "title": "테스트",
  "updated_date": "2024-09-12T11:48:08.531508",
  "author_name": "username",
  "author_id": 1,
  "board_id": 1,
  "is_deleted": true
}

# Get inserted data
GET /article/_doc/1

# Search all data
GET /article/_search

# Search data
GET /article/_search
{
  "query": {
    "match": {
      "contents": "테스트"
    }
  }
}

# Nori-analyze
GET /article/_analyze
{
  "analyzer": "nori_analyzer",
  "text": "테스트"
}

DELETE /article
```