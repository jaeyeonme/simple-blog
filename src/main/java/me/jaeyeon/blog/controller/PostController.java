package me.jaeyeon.blog.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.jaeyeon.blog.annotation.AuthenticationRequired;
import me.jaeyeon.blog.annotation.CurrentMember;
import me.jaeyeon.blog.dto.PostReq;
import me.jaeyeon.blog.dto.PostResponse;
import me.jaeyeon.blog.service.BlogPostService;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final BlogPostService postService;

    @AuthenticationRequired
    @PostMapping
    public ResponseEntity<Void> createPost(@RequestBody @Valid PostReq postReq,
        @CurrentMember Long memberId) {
        postService.createPost(postReq, memberId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPostsWithKeyword(
        @RequestParam(value = "keyword", defaultValue = "") String keyword,
        @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return new ResponseEntity<>(postService.searchPostsWithKeyword(keyword, pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable("id") Long id) {
        PostResponse postResponse = postService.getPostById(id);
        return new ResponseEntity<>(postResponse, HttpStatus.OK);
    }

    @AuthenticationRequired
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(@RequestBody @Valid PostReq postReq,
        @PathVariable("id") Long id, @CurrentMember Long memberId) {
        postService.updatePost(postReq, id, memberId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @AuthenticationRequired
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id, @CurrentMember Long memberId) {
        postService.deletePostById(id, memberId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
