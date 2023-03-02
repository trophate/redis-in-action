package com.trophate.redisinaction.controller;

import com.trophate.redisinaction.common.Result;
import com.trophate.redisinaction.dto.ArticleDTO;
import com.trophate.redisinaction.dto.Page;
import com.trophate.redisinaction.dto.VoteDTO;
import com.trophate.redisinaction.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 保存
     *
     * @param dto 文章数据
     * @return Result
     */
    @PostMapping
    public Result save(@RequestBody ArticleDTO dto) {
        articleService.save(dto);
        return Result.success();
    }

    /**
     * 支持
     *
     * @param dto 投票数据
     * @return Result
     */
    @PostMapping("/support")
    public Result support(@RequestBody VoteDTO dto) {
        articleService.support(dto);
        return Result.success();
    }

    /**
     * 反对
     *
     * @param dto 投票数据
     * @return Result
     */
    @PostMapping("/against")
    public Result against(@RequestBody VoteDTO dto) {
        articleService.against(dto);
        return Result.success();
    }

    /**
     * 基于投票排名获取分页列表
     * 
     * @param page 分页参数
     * @return Result
     */
    @GetMapping("/byVote")
    public Result getPageByVote(Page page) {
        return Result.success().setData(articleService.getPageByVote(page));
    }

    /**
     * 基于创建时间获取分页列表
     *
     * @param page 分页参数
     * @return Result
     */
    @GetMapping("/byTime")
    public Result getPageByTime(Page page) {
        return Result.success().setData(articleService.getPageByTime(page));
    }

    /**
     * 分组
     *
     * @param articleId 文章id
     * @param groupName 组名
     * @return Result
     */
    @PostMapping("/{id}/group")
    public Result group(@PathVariable("id") Integer articleId, String groupName) {
        articleService.group(articleId, groupName);
        return Result.success();
    }

    /**
     * 基于投票排名获取分组分页列表
     *
     * @param page 分页参数
     * @param groupName 组名
     * @return Result
     */
    @GetMapping("/byGroupAndVote")
    public Result getPageByVote(Page page, String groupName) {
        return Result.success().setData(articleService.getPageByVote(page, groupName));
    }

    /**
     * 基于创建时间获取分组分页列表
     *
     * @param page 分页参数
     * @param groupName 组名
     * @return Result
     */
    @GetMapping("/byGroupAndTime")
    public Result getPageByTime(Page page, String groupName) {
        return Result.success().setData(articleService.getPageByTime(page, groupName));
    }
}
