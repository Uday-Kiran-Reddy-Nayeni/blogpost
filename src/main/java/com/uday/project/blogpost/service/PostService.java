package com.uday.project.blogpost.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import com.uday.project.blogpost.model.Post;
import com.uday.project.blogpost.model.Tag;
import com.uday.project.blogpost.repository.PostRepository;
import com.uday.project.blogpost.repository.TagRepository;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TagRepository tagRepository;

    public void savePost(Post post, String helperTags) {
        String[] tagsNames = helperTags.split(",");
        for (String tagName : tagsNames) {
            if (tagRepository.findByName(tagName.toLowerCase().trim()) == null) {
                Tag tag = new Tag();
                tag.setName(tagName.toLowerCase().trim());
                tag.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
                if (tag.getCreatedAt() == null) {
                    tag.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                }
                post.getTags().add(tag);
                tag.getPosts().add(post);
            } else {
                Tag tag = tagRepository.findByName(tagName.toLowerCase().trim());
                tag.setName(tagName.toLowerCase().trim());
                post.getTags().add(tag);
            }
        }
        post.setPublishedAt(Timestamp.valueOf(LocalDateTime.now()));
        post.setPublished(true);
        post.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        post.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        postRepository.save(post);
    }

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public void deletePost(int id) {
        postRepository.deleteById(id);
    }

    public Post readPost(int id) {
        Optional<Post> post = postRepository.findById(id);
        return post.get();
    }

    public Post updatePost(int id) {
        Optional<Post> post = postRepository.findById(id);
        post.get().setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return post.get();
    }

    public Page<Post> findPaginated(int pageNo, int pageSize, String sortField, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        return this.postRepository.findAll(pageable);
    }

    public PagedListHolder<Post> findPaginatedForFilters(List<Post> posts, int pageNo, int pageSize, String sortField, String sortDirection) {
        boolean bool;
        if (sortDirection.equalsIgnoreCase("asc")) {
            bool = true;
        } else {
            bool = false;
        }
        System.out.println(bool);
        MutableSortDefinition mutableSort = new MutableSortDefinition(sortField, true, false);
        PagedListHolder<Post> pageList = new PagedListHolder<Post>(posts, mutableSort);
        return pageList;
    }

    public List<Post> findBySearchKeyword(String search) {
        List<Post> searchResults = postRepository.findAllBySearch(search);
        Set<Post> uniquePosts = new HashSet<>(searchResults);
        List<Post> posts = new ArrayList<>(uniquePosts);
        return posts;
    }

    public List<Post> findBySearchKeywordForFilters(String search) {
        List<Post> searchResults = postRepository.findAllBySearchForFilter(search);
        Set<Post> uniquePosts = new HashSet<>(searchResults);
        List<Post> posts = new ArrayList<>(uniquePosts);
        return posts;
    }

    public List<Post> findFilterOnSearch(String search, List<String> tags) {
        Set filteredPosts = new HashSet();
        for (String tag : tags) {
            filteredPosts.addAll(postRepository.findFilterOnSearch(search, tag));
        }
        return new ArrayList<>(filteredPosts);
    }

    public List<String> findAllAuthors() {
        return postRepository.findDistinctAuthors();
    }

    public List<Post> findAllBySearchForFilter(List<String> filters) {
        Set posts = new HashSet();
        for (String filter : filters) {
            posts.addAll(postRepository.findAllBySearchForFilter(filter));
        }
        return new ArrayList<>(posts);
    }

    public List<Post> findFiltersBetweenDates(List<String> filters, Date dateF, Date dateT) {
        System.out.println("filtering on filters by dates service");
        System.out.println("" + dateF);
        Set posts = new HashSet();
        for (String filter : filters) {
            posts.addAll(postRepository.findAllBySearchForFilterByDates(filter, dateF, dateT));
        }
        System.out.println(posts);
        return new ArrayList<>(posts);
    }

    public List<Post> findBySearchKeywordBetweenDates(String search, Date dateF, Date dateT) {
        return postRepository.findBySearchKeywordBetweenDates(search, dateF, dateT);
    }

    public List<Post> findPostsBetweenDates(Date dateF, Date dateT) {
        return postRepository.findPostsBetweenDates(dateF, dateT);
    }

    public List<Post> findFilterOnSearchByDates(String search, List<String> filters, Date dateF, Date dateT) {
        Set posts = new HashSet<>();
        for (String tag : filters) {
            posts.addAll(postRepository.findFilterOnSearchByDates(search, tag, dateF, dateT));
        }
        return new ArrayList<>(posts);
    }
}
