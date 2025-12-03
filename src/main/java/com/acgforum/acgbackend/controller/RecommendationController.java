package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.Topic;
import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.entity.Vote;
import com.acgforum.acgbackend.repository.TopicRepository;
import com.acgforum.acgbackend.repository.VoteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
public class RecommendationController {

    @Autowired private VoteRepository voteRepository;
    @Autowired private TopicRepository topicRepository;

    @GetMapping("/sidebar")
    public Map<String, Object> getSidebarRecommendations(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("user");

        List<Topic> recommendedTopics;
        String reason = "Global Hot"; // 推荐理由

        if (user != null) {
            // 1. 既然登录了，看看他最近赞过什么 (取最近 20 条点赞)
            List<Vote> recentVotes = voteRepository.findByUserIdAndTypeOrderByIdDesc(user.getId(), 1);

            if (!recentVotes.isEmpty()) {
                // 2. 算法核心：统计他最喜欢的分类
                // 这是一个简单的 Java Stream 统计逻辑
                Map<String, Long> categoryCount = recentVotes.stream()
                        .map(vote -> vote.getTopic().getCategory()) // 拿到分类名
                        .filter(cat -> cat != null)
                        .collect(Collectors.groupingBy(cat -> cat, Collectors.counting())); // 统计出现次数

                // 3. 找出次数最多的那个分类
                String favoriteCategory = categoryCount.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);

                if (favoriteCategory != null) {
                    // 4. 推荐该分类下的热门帖
                    recommendedTopics = topicRepository.findTop5ByCategoryOrderByVoteCountDesc(favoriteCategory);
                    reason = "Because you like " + favoriteCategory;
                } else {
                    recommendedTopics = topicRepository.findTop5ByOrderByVoteCountDesc();
                }
            } else {
                // 登录了但没投过票 -> 推全站热门
                recommendedTopics = topicRepository.findTop5ByOrderByVoteCountDesc();
            }
        } else {
            // 没登录 -> 推全站热门
            recommendedTopics = topicRepository.findTop5ByOrderByVoteCountDesc();
        }

        result.put("success", true);
        result.put("title", reason);
        result.put("list", recommendedTopics);
        return result;
    }
}