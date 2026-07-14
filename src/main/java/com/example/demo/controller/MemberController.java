package com.example.demo.controller;

import com.example.demo.dto.MemberResponse;
import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<MemberResponse> getAllMembers() {
        return memberService.getAllMembers().stream()
                .map(m -> new MemberResponse(m.getId(), m.getName(), m.getRole(),
                        m.getAvatarUrl(), m.getFacebookUrl(), m.getGithubUrl(), m.getLinkedinUrl(),
                        m.getSkills(), m.getProjects()))
                .collect(Collectors.toList());
    }

    // ── CREATE ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member request) {
        Map<String, Object> error = new HashMap<>();
        if (request.getName() == null || request.getName().isBlank()) {
            error.put("message", "Member's name cannot empty ");
            return ResponseEntity.badRequest().body(error);
        }
        Member member = new Member();
        member.setName(request.getName().trim());
        if (request.getRole() != null && !request.getRole().isBlank()) {
            member.setRole(request.getRole().trim());
        }
        member.setAvatarUrl(request.getAvatarUrl());
        member.setFacebookUrl(request.getFacebookUrl());
        member.setGithubUrl(request.getGithubUrl());
        member.setLinkedinUrl(request.getLinkedinUrl());
        member.setSkills(request.getSkills());
        member.setProjects(request.getProjects());

        Member saved = memberRepository.save(member);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MemberResponse(saved.getId(), saved.getName(), saved.getRole(),
                        saved.getAvatarUrl(), saved.getFacebookUrl(), saved.getGithubUrl(), saved.getLinkedinUrl(),
                        saved.getSkills(), saved.getProjects()));
    }

    // ── UPDATE ───────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id, @RequestBody Member request) {
        Map<String, Object> error = new HashMap<>();

        if (id >= 1000000L) {
            Long userId = id - 1000000L;
            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isEmpty()) {
                error.put("message", "Cannot find member (user) with id = " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            User user = optionalUser.get();
            if (!"ROLE_MEMBER".equalsIgnoreCase(user.getRole()) && !"Team_Member".equalsIgnoreCase(user.getRole())) {
                error.put("message", "User with id = " + id + " is not a member.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Find or create dynamic member record in members table
            Member m = memberRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        Member memberObj = new Member();
                        memberObj.setUserId(userId);
                        memberObj.setRole("Team Member");
                        return memberObj;
                    });

            if (request.getName() != null && !request.getName().isBlank()) {
                m.setName(request.getName().trim());
            }
            if (request.getRole() != null && !request.getRole().isBlank()) {
                m.setRole(request.getRole().trim());
            }
            if (request.getAvatarUrl() != null) {
                m.setAvatarUrl(request.getAvatarUrl());
            }
            if (request.getFacebookUrl() != null) {
                m.setFacebookUrl(request.getFacebookUrl().trim().isEmpty() ? null : request.getFacebookUrl().trim());
            }
            if (request.getGithubUrl() != null) {
                m.setGithubUrl(request.getGithubUrl().trim().isEmpty() ? null : request.getGithubUrl().trim());
            }
            if (request.getLinkedinUrl() != null) {
                m.setLinkedinUrl(request.getLinkedinUrl().trim().isEmpty() ? null : request.getLinkedinUrl().trim());
            }
            if (request.getSkills() != null) {
                m.setSkills(request.getSkills().trim().isEmpty() ? null : request.getSkills().trim());
            }
            if (request.getProjects() != null) {
                m.setProjects(request.getProjects().trim().isEmpty() ? null : request.getProjects().trim());
            }

            Member saved = memberRepository.save(m);
            return ResponseEntity.ok(new MemberResponse(id, saved.getName(), saved.getRole(),
                    saved.getAvatarUrl(), saved.getFacebookUrl(), saved.getGithubUrl(), saved.getLinkedinUrl(),
                    saved.getSkills(), saved.getProjects()));
        }

        Optional<Member> optional = memberRepository.findById(id);
        if (optional.isEmpty()) {
            error.put("message", "Cannot find member with id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Member member = optional.get();
        if (request.getName()        != null && !request.getName().isBlank())   member.setName(request.getName().trim());
        if (request.getRole()        != null && !request.getRole().isBlank())   member.setRole(request.getRole().trim());
        if (request.getAvatarUrl()   != null) member.setAvatarUrl(request.getAvatarUrl());
        if (request.getFacebookUrl() != null) member.setFacebookUrl(request.getFacebookUrl());
        if (request.getGithubUrl()   != null) member.setGithubUrl(request.getGithubUrl());
        if (request.getLinkedinUrl() != null) member.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getSkills()      != null) member.setSkills(request.getSkills());
        if (request.getProjects()    != null) member.setProjects(request.getProjects());

        Member saved = memberRepository.save(member);
        return ResponseEntity.ok(new MemberResponse(saved.getId(), saved.getName(), saved.getRole(),
                saved.getAvatarUrl(), saved.getFacebookUrl(), saved.getGithubUrl(), saved.getLinkedinUrl(),
                saved.getSkills(), saved.getProjects()));
    }

    // ── DELETE ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        if (id >= 1000000L) {
            Long userId = id - 1000000L;
            if (!userRepository.existsById(userId)) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "Cannot find member (user) with id = " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            userRepository.deleteById(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Delete member successfully");
            return ResponseEntity.ok(result);
        }

        if (!memberRepository.existsById(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Cannot find member with id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        memberRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Delete member successfully");
        return ResponseEntity.ok(result);
    }
}
