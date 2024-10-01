package com.example.board.repository;

import com.example.board.entity.Advertisment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisment, Long> {
}
