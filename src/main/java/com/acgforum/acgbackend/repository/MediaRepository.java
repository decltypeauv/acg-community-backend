package com.acgforum.acgbackend.repository;

import com.acgforum.acgbackend.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    // 以后可能会用到：查找某个用户上传的所有文件
    List<Media> findByUploaderId(Long userId);
    
    // 倒序查找所有文件 (最新的在前面)
    List<Media> findAllByOrderByUploadTimeDesc();
}