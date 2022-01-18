package com.uday.project.blogpost.api;

import com.uday.project.blogpost.model.AuthRequest;
import com.uday.project.blogpost.model.Post;
import com.uday.project.blogpost.repository.PostRepository;
import com.uday.project.blogpost.service.PostService;
import com.uday.project.blogpost.service.TagService;
import com.uday.project.blogpost.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
public class RestApiController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostService postService;

    @Autowired
    TagService tagService;

    @GetMapping("/posts")
    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    @GetMapping("/post/{id}")
    public Post findPostById(@PathVariable int id) {
        return postRepository.findById(id).get();
    }

    @GetMapping("/")
    public List<Post> dashboard(@RequestParam(value = "search", required = false) String search, @RequestParam(value = "filters", required = false) List<String> filters, @RequestParam(value = "pageNo", required = false) Integer pageNo, @RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo, @RequestParam(value = "sortField", required = false) String sortField, @RequestParam(value = "sortDirection", required = false) String sortDirection, Model model) {
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
                System.out.println("entered search of ");
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

    @PostMapping("/post")
    public String createPost(@RequestBody Post post) {
        postRepository.save(post);
        return "Post created successfully";
    }

    @PutMapping("/post/{id}")
    public String updatePost(@RequestBody Post post, @PathVariable(value = "id") int id) {
        postRepository.save(post);
        return "Post updated successfully";
    }

    @DeleteMapping("/post/{id}")
    public String deletePost(@PathVariable(value = "id") int id) {
        System.out.println("Entered delete");
        postRepository.deleteById(id);
        return "Post deleted successfully";
    }

    public List<Post> paginationForAllPosts(int pageNo, String sortField, String sortDirection, Model model) {
        int pageSize = 10;
        Page<Post> page = postService.findPaginated(pageNo, pageSize, sortField, sortDirection);
        List<Post> posts = page.getContent();
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("reverseSortDirection", sortDirection.equals("asc") ? "desc" : "asc");
        model.addAttribute("posts", posts);
        return posts;
    }

    public List<Post> PaginatedForFilteredPosts(List<Post> f, @PathVariable int pageNo, @RequestParam("sortField") String sortField, @RequestParam("sortDirection") String sortDirection, Model model) {
        int pageSize = 10;
        String reverseSortDirection = sortDirection.equals("asc") ? "desc" : "asc";
        List<Post> tempList = new ArrayList<>(f);
        if (sortField.equalsIgnoreCase("publishedAt")) {
            if (sortDirection.equalsIgnoreCase("asc")) {
                Collections.sort(tempList);
            } else {
                Collections.sort(tempList, Collections.reverseOrder());
            }
        }
        PagedListHolder<Post> pageList = postService.findPaginatedForFilters(tempList, pageNo, pageSize, sortField, sortDirection);
        List<Post> postsFiltered;
        if (pageNo == 1) {
            pageList.setPageSize(pageSize);
            pageList.setPage(0);
            postsFiltered = pageList.getPageList();
        } else {
            pageList.setPageSize(pageSize);
            pageList.setPage(pageNo - 1);
            postsFiltered = pageList.getPageList();
            System.out.println("hello");
        }
        model.addAttribute("totalPages", pageList.getPageCount());
        return postsFiltered;
    }

    @PostMapping("/authenticate")
    public String generateToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        } catch (Exception ex) {
            throw new Exception("inavalid username/password");
        }
        return jwtUtil.generateToken(authRequest.getEmail());
    }
}
