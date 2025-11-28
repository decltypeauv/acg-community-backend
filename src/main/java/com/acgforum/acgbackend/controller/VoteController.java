package com.acgforum.acgbackend.controller;

import com.acgforum.acgbackend.entity.Topic;
import com.acgforum.acgbackend.entity.User;
import com.acgforum.acgbackend.entity.Vote;
import com.acgforum.acgbackend.repository.TopicRepository;
import com.acgforum.acgbackend.repository.VoteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vote")
public class VoteController {

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private TopicRepository topicRepository;

    // 投票接口
    @PostMapping("/toggle")
    public Map<String, Object> toggleVote(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 1. 检查登录
        User user = (User) session.getAttribute("user");
        if (user == null) {
            result.put("success", false);
            result.put("msg", "请先登录");
            return result;
        }

        Long topicId = Long.valueOf(payload.get("topicId").toString());
        int type = Integer.parseInt(payload.get("type").toString()); // 1 或 -1

        Topic topic = topicRepository.findById(topicId).orElse(null);
        if (topic == null) {
            result.put("success", false);
            result.put("msg", "帖子不存在");
            return result;
        }

        // 2. 检查这个人之前投过没
        Optional<Vote> existingVoteOpt = voteRepository.findByUserIdAndTopicId(user.getId(), topicId);
        
        // 初始化 voteCount (防止为 null)
        if (topic.getVoteCount() == null) topic.setVoteCount(0);

        if (existingVoteOpt.isPresent()) {
            // --- 情况 A：之前投过 ---
            Vote existingVote = existingVoteOpt.get();

            if (existingVote.getType() == type) {
                // A1. 之前投的一样 -> 说明想取消 (比如之前赞过，又点赞)
                voteRepository.delete(existingVote);
                topic.setVoteCount(topic.getVoteCount() - type); // 撤销分数
            } else {
                // A2. 之前投的不一样 -> 说明想改票 (比如之前踩，现在赞)
                // 此时分数变化是 2 倍 (比如 -1 变 +1，总分要 +2)
                topic.setVoteCount(topic.getVoteCount() - existingVote.getType() + type);
                existingVote.setType(type); // 更新状态
                voteRepository.save(existingVote);
            }
        } else {
            // --- 情况 B：之前没投过 ---
            Vote newVote = new Vote();
            newVote.setUser(user);
            newVote.setTopic(topic);
            newVote.setType(type);
            voteRepository.save(newVote);

            topic.setVoteCount(topic.getVoteCount() + type);
        }

        // 3. 保存最新的分数到帖子表
        topicRepository.save(topic);

        result.put("success", true);
        result.put("newScore", topic.getVoteCount());
        return result;
    }
}