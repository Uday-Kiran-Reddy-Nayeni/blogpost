package com.uday.project.blogpost.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uday.project.blogpost.model.Tag;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag findByName(String name);

    @Query(value = "SELECT name from tag t inner join post_tags pt on " +
            "t.id=pt.tag_id where pt.post_id=?1", nativeQuery = true)
    List<String> findTagByPostId(int id);
}
