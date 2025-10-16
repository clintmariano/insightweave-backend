package com.insightweave.repository;

import com.insightweave.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
        select d from Document d
        where (:q is null or lower(d.title) like lower(concat('%', :q, '%'))
                       or lower(d.content) like lower(concat('%', :q, '%')))
    """)
    Page<Document> search(@Param("q") String q, Pageable pageable);
}
