package com.example.demo.repository;
import com.example.demo.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    Optional<SearchHistory> findByKeyword(String keyword);
    List<SearchHistory> findTop10ByOrderBySearchCountDesc();
}
