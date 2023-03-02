package com.trophate.redisinaction.service;

import com.trophate.redisinaction.dto.ArticleDTO;
import com.trophate.redisinaction.dto.Page;
import com.trophate.redisinaction.dto.VoteDTO;
import com.trophate.redisinaction.enums.VoteType;
import com.trophate.redisinaction.utils.NumberGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ArticleService {

    private final int SCORE_CONSTANT = 43200;

    private final RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    public ArticleService(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(ArticleDTO dto) {
        long now = System.currentTimeMillis();
        int articleId = NumberGenerator.getArticleId();
        String articleKey = "article:" + articleId;
        // 文章详情
        redisTemplate.opsForHash().put(articleKey, "title", dto.getTitle());
        redisTemplate.opsForHash().put(articleKey, "link", dto.getLink());
        redisTemplate.opsForHash().put(articleKey, "poster", dto.getPosterId());
        redisTemplate.opsForHash().put(articleKey, "time", now);
        redisTemplate.opsForHash().put(articleKey, "votes", 0);
        // 时间排序
        redisTemplate.opsForZSet().add("time:", articleKey, now);
        // 分值排序
        redisTemplate.opsForZSet().add("score:", articleKey, now);
        // 支持票记录
        String supportKey = "support:" + articleId;
        String posterKey = "poster:" + dto.getPosterId();
        redisTemplate.opsForSet().add(supportKey, posterKey);
        redisTemplate.expire(supportKey, 7, TimeUnit.DAYS);
        // 反对票记录
        String againstKey = "against:" + articleId;
        redisTemplate.opsForSet().add(againstKey, posterKey);
        redisTemplate.expire(againstKey, 7, TimeUnit.DAYS);
    }

    public void support(VoteDTO dto) {
        String articleKey = "article:" + dto.getArticleId();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusWeeks(1);
        Double nowTimestamp = ((Long) now.toInstant(ZoneOffset.ofHours(8)).toEpochMilli()).doubleValue();
        String supportKey = "support:" + dto.getArticleId();
        String userKey = "user:" + dto.getVoterId();
        if (redisTemplate.opsForZSet().score("time:", articleKey).compareTo(nowTimestamp) > 0
                && !redisTemplate.opsForSet().isMember(supportKey, userKey) && !voteChange(dto, VoteType.SUPPORT.getCode())) {
            redisTemplate.opsForSet().add(supportKey, userKey);
            redisTemplate.opsForZSet().incrementScore("score:", articleKey, SCORE_CONSTANT);
            redisTemplate.opsForHash().increment(articleKey, "votes", 1);
        }
    }

    public void against(VoteDTO dto) {
        String articleKey = "article:" + dto.getArticleId();
        LocalDateTime now = LocalDateTime.now();
        now = now.minusWeeks(1);
        Double nowTimestamp = ((Long) now.toInstant(ZoneOffset.ofHours(8)).toEpochMilli()).doubleValue();
        String againstKey = "against:" + dto.getArticleId();
        String userKey = "user:" + dto.getVoterId();
        if (redisTemplate.opsForZSet().score("time:", articleKey).compareTo(nowTimestamp) > 0
                && !redisTemplate.opsForSet().isMember(againstKey, userKey) && !voteChange(dto, VoteType.AGAINST.getCode())) {
            redisTemplate.opsForSet().add(againstKey, userKey);
            redisTemplate.opsForZSet().incrementScore("score:", articleKey, -SCORE_CONSTANT);
            redisTemplate.opsForHash().increment(articleKey, "votes", 1);
        }
    }

    private boolean voteChange(VoteDTO dto, int voteType) {
        String supportKey = "support:" + dto.getArticleId();
        String againstKey = "against:" + dto.getArticleId();
        String userKey = "user:" + dto.getVoterId();
        String articleKey = "article:" + dto.getArticleId();
        boolean res;
        if (voteType == VoteType.SUPPORT.getCode()) {
            res = redisTemplate.opsForSet().move(againstKey, userKey, supportKey);
            if (res) {
                redisTemplate.opsForZSet().incrementScore("score:", articleKey, SCORE_CONSTANT * 2);
            }
        } else {
            res = redisTemplate.opsForSet().move(supportKey, userKey, againstKey);
            if (res) {
                redisTemplate.opsForZSet().incrementScore("score:", articleKey, -SCORE_CONSTANT * 2);
            }
        }
        return res;
    }

    public Set<?> getPageByVote(Page page) {
        return redisTemplate.opsForZSet().reverseRange("score:", page.getStartIndex(), page.getEndIndex());
    }

    public Set<?> getPageByTime(Page page) {
        return redisTemplate.opsForZSet().reverseRange("time:", page.getStartIndex(), page.getEndIndex());
    }

    public void group(Integer articleId, String groupName) {
        redisTemplate.opsForSet().add("group:" + groupName, "article:" + articleId);
    }

    public Set<?> getPageByVote(Page page, String groupName) {
        String groupScoreKey = "score:" + groupName;
        if (!redisTemplate.hasKey(groupScoreKey)) {
            redisTemplate.opsForZSet().intersectAndStore("score:", "group:" + groupName, groupScoreKey);
            redisTemplate.expire(groupScoreKey, 10, TimeUnit.SECONDS);
        }
        return redisTemplate.opsForZSet().reverseRange(groupScoreKey, page.getStartIndex(), page.getEndIndex());
    }

    public Set<?> getPageByTime(Page page, String groupName) {
        String groupTimeKey = "time:" + groupName;
        if (!redisTemplate.hasKey(groupTimeKey)) {
            redisTemplate.opsForZSet().intersectAndStore("time:", "group:" + groupName, groupTimeKey);
            redisTemplate.expire(groupTimeKey, 10, TimeUnit.SECONDS);
        }
        return redisTemplate.opsForZSet().reverseRange(groupTimeKey, page.getStartIndex(), page.getEndIndex());
    }
}
