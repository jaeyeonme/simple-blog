package me.jaeyeon.readitdomain.post.service;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import me.jaeyeon.readitdomain.member.domain.Member;
import me.jaeyeon.readitdomain.post.domain.DailyPostCountRequest;
import me.jaeyeon.readitdomain.post.domain.Post;
import me.jaeyeon.readitdomain.post.domain.PostCreate;
import me.jaeyeon.readitdomain.post.domain.PostUpdate;
import me.jaeyeon.readitdomain.post.infrastructure.PostCountPerDate;

public interface PostUseCase {

	void createPost(PostCreate postCreate, Member author);

	Page<Post> searchPostsWithKeyword(String keyword, Pageable pageable);

	Post getPostById(Long id);

	void updatePost(PostUpdate postUpdate, Long id, Member author);

	void deletePostById(Long id, Member author);
	Page<Post> getPostsByAuthorId(Long memberId, Pageable pageable);
	List<PostCountPerDate> countPostsByMemberAndDateRange(Long memberId, LocalDate startDate, LocalDate endDate);
}
