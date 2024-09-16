### Elastic Search API Test Re-indexing

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

PUT /article/_settings
{
  "index": {
    "number_of_replicas": 0
  }
}

# Insert Data with id
POST /article/_bulk
{ "index": { "_id": 1 } }
{ "id": 1, "contents": "testing", "created_date": "2024-09-11T18:40:45.858322", "title": "testing", "updated_date": "2024-09-12T11:48:08.531508", "author_name": "username", "author_id": 1, "board_id": 1, "is_deleted": true }
{ "index": { "_id": 2 } }
{ "id": 2, "contents": "test", "created_date": "2024-09-11T18:40:45.858322", "title": "test", "updated_date": "2024-09-12T11:48:08.531508", "author_name": "username", "author_id": 1, "board_id": 1, "is_deleted": true }
{ "index": { "_id": 3 } }
{ "id": 3, "contents": "테스트1", "created_date": "2024-09-11T18:40:45.858322", "title": "테스트", "updated_date": "2024-09-12T11:48:08.531508", "author_name": "username", "author_id": 1, "board_id": 1, "is_deleted": true }
{ "index": { "_id": 4 } }
{ "id": 4, "contents": "테스트2", "created_date": "2024-09-11T18:40:45.858322", "title": "테스트", "updated_date": "2024-09-12T11:48:08.531508", "author_name": "username", "author_id": 1, "board_id": 1, "is_deleted": true }

GET /article/_search

# Set new index without id
PUT /article_new
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

PUT /article_new/_settings
{
  "index": {
    "number_of_replicas": 0
  }
}

# Health check
GET /_cat/indices

# Remove id and re-index
POST /_reindex
{
  "source": {
    "index": "article"
  },
  "dest": {
    "index": "article_new"
  },
  "script": {
    "source": "ctx._source.remove('id')"
  }
}

GET /article_new/_search
DELETE /article

POST /_aliases
{
  "actions": [
    {
      "add": {
        "index": "article_new",
        "alias": "article"
      }
    }
  ]
}

GET /article/_search
```

- The result will point to "article_new" index because alias api does not changes index name and just provides virtual name.
- To change index name, reindex to "article".