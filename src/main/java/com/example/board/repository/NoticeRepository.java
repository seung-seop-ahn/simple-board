package com.example.board.repository;

import com.example.board.entity.Notice;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n WHERE n.createdDate >= :createdDate AND n.isDeleted = false ORDER BY n.createdDate")
    List<Notice> findAllByCreatedDate(@Param("createdDate") LocalDateTime createdDate);
}
