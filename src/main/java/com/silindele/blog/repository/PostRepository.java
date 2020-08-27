package com.silindele.blog.repository;

import com.silindele.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT * FROM Post p ORDER BY p.modified_date DESC LIMIT = ?1 ", nativeQuery = true)
    LinkedList<Post> retrieveLatest(int number);

    @Modifying
    @Query("update Post p set p.title = ?1, p.content = ?2, p.modifiedDate = ?3 where p.id = ?4")
    void updatePostById(String title, String content, Date modifiedDate, Long userId);

    @Query("select new Post(p.id, p.title) from Post p where p.active = false order by p.modifiedDate desc")
    List<Post> retrieveNonActive();

    @Modifying
    @Query("update Post p set p.active = ?1 where p.id = ?2")
    void publishPostById(boolean active, Long userId);


    @Query("select new Post(p.id, p.title) from Post p where p.active = true order by p.modifiedDate desc")
    Page<Post> retrieveAll(Pageable pageable);

    @Override
    void deleteById(Long aLong);
}
