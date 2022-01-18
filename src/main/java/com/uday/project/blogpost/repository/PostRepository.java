package com.uday.project.blogpost.repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.uday.project.blogpost.model.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query(value = "SELECT * FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id " +
            "INNER JOIN tag t ON pt.tag_id = t.id where p.title like %?1% or p.author like %?1% or " +
            "p.content like %?1% or t.name like %?1%", nativeQuery = true)
    List<Post> findAllBySearch(String search);

    @Query(value = "SELECT * FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id INNER JOIN tag t ON " +
            "pt.tag_id = t.id where p.author like %?1% or t.name like %?1%", nativeQuery = true)
    List<Post> findAllBySearchForFilter(String search);

    @Query(value = "select DISTINCT(p.author) from post p", nativeQuery = true)
    List<String> findDistinctAuthors();

    Page<Post> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id" +
            " INNER JOIN tag t ON pt.tag_id = t.id where p.title like %?1% or p.author " +
            "like %?1% or p.content like %?1% or t.name like %?1% and p.id in " +
            "(SELECT post_id from post_tags where tag_id in (SELECT id from tag where tag.name in ?2))", nativeQuery = true)
    List<Post> findFiltersOnSearch(String search, String tag);

    @Query(value = "SELECT * FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id" +
            " INNER JOIN tag t ON pt.tag_id = t.id where (p.title like %?1% or p.author " +
            "like %?1% or p.content like %?1% or t.name like %?1%) and (p.id in " +
            "(SELECT p.id FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id INNER JOIN tag t ON " +
            "pt.tag_id = t.id where p.author like %?2% or t.name like %?2%))", nativeQuery = true)
    List<Post> findFilterOnSearch(String search, String tag);

    @Query(value = "SELECT * FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id INNER JOIN tag t ON " +
            "pt.tag_id = t.id where (p.author like %?1% or t.name like %?1%) and (p.created_at between ?2 and ?3)", nativeQuery = true)
    List<Post> findAllBySearchForFilterByDates(String filter, Date dateF, Date dateT);

    @Query(value = "SELECT * FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id " +
            "INNER JOIN tag t ON pt.tag_id = t.id where (p.title like %?1% or p.author " +
            "like %?1% or p.content like %?1% or t.name like %?1%) and (p.created_at between ?2 and ?3)", nativeQuery = true)
    List<Post> findBySearchKeywordBetweenDates(String search, Date dateF, Date dateT);

    @Query(value = "SELECT * FROM post where created_at between ?1 and ?2", nativeQuery = true)
    List<Post> findPostsBetweenDates(Date dateF, Date dateT);

    @Query(value = "SELECT * FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id" +
            " INNER JOIN tag t ON pt.tag_id = t.id where (p.title like %?1% or p.author " +
            "like %?1% or p.content like %?1% or t.name like %?1%) and (p.id in " +
            "(SELECT p.id FROM post p INNER JOIN post_tags pt ON p.id = pt.post_id INNER JOIN tag t ON " +
            "pt.tag_id = t.id where p.author like %?2% or t.name like %?2%)) and (p.created_at between ?3 and ?4)", nativeQuery = true)
    List<Post> findFilterOnSearchByDates(String search, String tag, Date dateF, Date dateT);
}
