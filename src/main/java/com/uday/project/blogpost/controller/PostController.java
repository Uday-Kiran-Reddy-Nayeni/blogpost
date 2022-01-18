package com.uday.project.blogpost.controller;

import java.text.SimpleDateFormat;
import java.util.*;

import com.uday.project.blogpost.repository.PostRepository;
import com.uday.project.blogpost.security.MyUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.uday.project.blogpost.model.Comment;
import com.uday.project.blogpost.model.Post;
import com.uday.project.blogpost.service.CommentService;
import com.uday.project.blogpost.service.PostService;
import com.uday.project.blogpost.service.TagService;

@Controller
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private TagService tagService;
    @Autowired
    private PostRepository postRepository;

    @RequestMapping("/")
    public String dashboard(@RequestParam(value = "search", required = false) String search,
                            @RequestParam(value = "filters", required = false) List<String> filters,
                            @RequestParam(value = "pageNo", required = false) Integer pageNo,
                            @RequestParam(value = "dateFrom", required = false) String dateFrom,
                            @RequestParam(value = "dateTo", required = false) String dateTo,
                            @RequestParam(value = "sortField", required = false) String sortField,
                            @RequestParam(value = "sortDirection", required = false) String sortDirection, Model model) {
        if (pageNo == null) {
            pageNo = 1;
        }
        if (sortField == null) {
            sortField = "author";
        }
        if (sortDirection == null) {
            sortDirection = "asc";
        }
        if (filters == null && search == null && (dateFrom == null || dateTo == null)) {
            return paginationForAllPosts(pageNo, sortField, sortDirection, model);
        } else if (search == null && (filters != null || (dateFrom != null && dateTo != null))) {
            if (filters == null) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date dateF = null;
                Date dateT = null;
                try {
                    dateF = format.parse(dateFrom);
                    dateT = format.parse(dateTo);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return PaginatedForFilteredPosts(postService.findPostsBetweenDates(dateF, dateT), pageNo, sortField, sortDirection, model);
            } else if (filters != null && (dateFrom == null || dateTo == null)) {
                postService.findAllBySearchForFilter(filters);
                return PaginatedForFilteredPosts(postService.findAllBySearchForFilter(filters), pageNo, sortField, sortDirection, model);

            } else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date dateF = null;
                Date dateT = null;
                try {
                    dateF = format.parse(dateFrom);
                    dateT = format.parse(dateTo);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                postService.findFiltersBetweenDates(filters, dateF, dateT);
                return PaginatedForFilteredPosts(postService.findFiltersBetweenDates(filters, dateF, dateT), pageNo, sortField, sortDirection, model);
            }
        } else if (search != null && filters == null) {
            if (dateFrom == null || dateTo == null) {
                postService.findBySearchKeyword(search);
                return PaginatedForFilteredPosts(postService.findBySearchKeyword(search), pageNo, sortField, sortDirection, model);
            } else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date dateF = null;
                Date dateT = null;
                try {
                    dateF = format.parse(dateFrom);
                    dateT = format.parse(dateTo);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                System.out.println("entered here");
                return PaginatedForFilteredPosts(postService.findBySearchKeywordBetweenDates(search, dateF, dateT), pageNo, sortField, sortDirection, model);
            }
        } else {
            if (dateFrom == null || dateTo == null) {
                postService.findFilterOnSearch(search, filters);
                return PaginatedForFilteredPosts(postService.findFilterOnSearch(search, filters), pageNo, sortField, sortDirection, model);
            } else {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date dateF = null;
                Date dateT = null;
                try {
                    dateF = format.parse(dateFrom);
                    dateT = format.parse(dateTo);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return PaginatedForFilteredPosts(postService.findFilterOnSearchByDates(search, filters, dateF, dateT), pageNo, sortField, sortDirection, model);
            }
        }
    }

    @RequestMapping("/addPost")
    public String addPost(Model model) {
        MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = myUserDetails.getName();
        Post post = new Post();
        post.setAuthor(userName);
        model.addAttribute("post", post);
        return "addPost";
    }

    @RequestMapping("/read/{id}")
    public String readPost(@PathVariable(value = "id") int id, Model model) {
        Post post = postService.readPost(id);
        Comment comment = new Comment();
        String username = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {

            username = getCurrentUsername();
        }
        model.addAttribute("username", username);
        model.addAttribute("post", post);
        model.addAttribute("tag", tagService.findTagByPostId(id));
        model.addAttribute("comment", comment);
        List<Comment> comments = commentService.allComments(id);
        model.addAttribute("comments", comments);
        return "readPost";
    }

    @RequestMapping("/savePost")
    public String savePost(@ModelAttribute("post") Post post, @RequestParam("helperTags") String helperTags) {
        postService.savePost(post, helperTags);
        return "redirect:/";
    }

    @RequestMapping("/delete/{id}")
    public String deletePost(@PathVariable(value = "id") int id) {
        postService.deletePost(id);
        return "redirect:/";
    }

    @RequestMapping("/update/{id}")
    public String updatePost(@PathVariable(value = "id") int id, Model model) {
        Post post = postService.updatePost(id);
        String tags = tagService.findTagByPostId(id);
        model.addAttribute("helperTags", tags);
        model.addAttribute("post", post);
        return "addPost";
    }

    @GetMapping("/dashboard/{pageNo}")
    public String paginationForAllPosts(@PathVariable(value = "pageNo") int pageNo,
                                        @RequestParam("sortField") String sortField,
                                        @RequestParam("sortDirection") String sortDirection, Model model) {
        String username = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {

            username = getCurrentUsername();
        }
        int pageSize = 10;
        Page<Post> page = postService.findPaginated(pageNo, pageSize, sortField, sortDirection);
        List<Post> posts = page.getContent();
        model.addAttribute("admin", "uday");
        model.addAttribute("username", username);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        if (!model.containsAttribute("posts")) {
            model.addAttribute("posts", posts);
        }
        model.addAttribute("tags", tagService.findAllTags());
        model.addAttribute("authors", postService.findAllAuthors());
        return "dashboard";
    }

    @RequestMapping("/search")
    public String getPostBySearch(@RequestParam("search") String search, Model model) {
        model.addAttribute("search", search);
        List<Post> posts = postService.findBySearchKeyword(search);
        model.addAttribute("posts", posts);

        model.addAttribute("tags", tagService.findAllTags());
        model.addAttribute("authors", postService.findAllAuthors());
        return "dashboard";
    }

    @GetMapping("/filter/{pageNo}")
    public String PaginatedForFilteredPosts(List<Post> posts,
                                            @PathVariable int pageNo,
                                            @RequestParam("sortField") String sortField,
                                            @RequestParam("sortDirection") String sortDirection, Model model) {
        int pageSize = 10;
        List<Post> tempList = new ArrayList<>(posts);
        if (sortField.equalsIgnoreCase("publishedAt")) {
            if (sortDirection.equalsIgnoreCase("asc")) {
                Collections.sort(tempList);
            } else {
                Collections.sort(tempList, Collections.reverseOrder());
            }
        }
        PagedListHolder<Post> pageList = postService.
                findPaginatedForFilters(tempList, pageNo, pageSize, sortField, sortDirection);
        List<Post> postsFiltered;
        if (pageNo == 1) {
            pageList.setPageSize(pageSize);
            pageList.setPage(0);
            postsFiltered = pageList.getPageList();
            System.out.println("for page no 1");
        } else {
            pageList.setPageSize(pageSize);
            pageList.setPage(pageNo - 1);
            postsFiltered = pageList.getPageList();
            System.out.println("hello");
        }
        String username = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            username = getCurrentUsername();
        }
        model.addAttribute("admin", "uday");
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", pageList.getPageCount());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("posts", postsFiltered);
        model.addAttribute("tags", tagService.findAllTags());
        model.addAttribute("authors", postService.findAllAuthors());
        model.addAttribute("username", username);
        return "filterPage";
    }

    @RequestMapping("/reset")
    public String reset() {
        return "redirect:/";
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((MyUserDetails) principal).getName();
        }
        return String.valueOf(principal);
    }
}


