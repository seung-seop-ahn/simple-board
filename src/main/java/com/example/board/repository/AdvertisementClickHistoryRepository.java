package com.example.board.repository;

import com.example.board.entity.AdvertisementClickHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementClickHistoryRepository extends MongoRepository<AdvertisementClickHistory, String> {
}
