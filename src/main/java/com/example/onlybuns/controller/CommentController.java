package com.example.onlybuns.controller;

import com.example.onlybuns.dtos.CommentCreationDto;
import com.example.onlybuns.dtos.CommentReadDto;
import com.example.onlybuns.model.Comment;
import com.example.onlybuns.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{postId}")
    public List<CommentReadDto> getPostComments(@PathVariable Long postId) {
        return commentService.getPostComments(postId);
    }

    @PostMapping
    public Comment addComment(@RequestBody CommentCreationDto comment) {
        return commentService.addComment(comment);
    }

}
