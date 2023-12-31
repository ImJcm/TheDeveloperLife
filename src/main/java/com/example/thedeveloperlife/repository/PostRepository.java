package com.example.thedeveloperlife.repository;

import com.example.thedeveloperlife.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByModifiedAtDesc();
    List<Post> findAllByCategory_IdOrderByModifiedAtDesc(Long category_id);
}