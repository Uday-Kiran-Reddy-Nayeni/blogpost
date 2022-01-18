package com.uday.project.blogpost.controller;

import com.uday.project.blogpost.model.Comment;
import com.uday.project.blogpost.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CommentController {
    @Autowired
    private CommentService commentService;

    @RequestMapping("/addComment/{postId}")
    public String addComment(@ModelAttribute("comment") Comment comment, @PathVariable(value = "postId") int id) {
        commentService.saveComment(comment, id);
        return "redirect:/read/" + id;
    }

    @RequestMapping("/read/delete/{postId}/{id}")
    public String deleteComment(@PathVariable(value = "id") int id, @PathVariable(value = "postId") int postId) {
        commentService.deleteComment(id);
        return "redirect:/read/" + postId;
    }

    @RequestMapping("/addComment/updateComment/{id}")
    public String updateComment(@PathVariable(value = "id") int id, @ModelAttribute Comment comment) {
        int postId = commentService.getComment(id).getPost().getId();
        commentService.saveComment(comment, postId);
        return "redirect:/read/" + postId;
    }

    @RequestMapping("/updateCommentPage/{id}")
    public String updateCommentPage(Model model, @PathVariable int id) {
        Comment comment = commentService.getComment(id);
        model.addAttribute("comment", comment);
        return "updateComment";
    }
}
