package com.acgforum.acgbackend.repository;

import com.acgforum.acgbackend.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    // 获取所有话题，按时间倒序（最新的在最上面）
    List<Topic> findAllByOrderByCreatedAtDesc();
    // 【新增】只查找特定分类的帖子
    List<Topic> findByCategoryOrderByCreatedAtDesc(String category);
     // 【新增】搜索功能：标题包含 keyword (忽略大小写)
    List<Topic> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);
    
    // 【新增】Top 榜：按票数倒序查所有
    List<Topic> findAllByOrderByVoteCountDesc();
    
    // 【新增】分类下的 Top 榜
    List<Topic> findByCategoryOrderByVoteCountDesc(String category);
    // 【新增】根据作者ID查询所有帖子
    List<Topic> findByAuthorIdOrderByCreatedAtDesc(Long userId);
    // 【新增】推荐查询：找某个分类下分数最高的帖子，取前 5 个
    List<Topic> findTop5ByCategoryOrderByVoteCountDesc(String category);
    
    // 【新增】全局热门：不限分类，找分数最高的 5 个 (给游客看)
    List<Topic> findTop5ByOrderByVoteCountDesc();
}