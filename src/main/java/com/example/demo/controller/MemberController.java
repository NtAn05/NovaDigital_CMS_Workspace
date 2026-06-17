package com.example.demo.controller;

import com.example.demo.dto.MemberResponse;
import com.example.demo.entity.Member;
import com.example.demo.repository.MemberRepository;
import com.example.demo.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ── GET ALL ──────────────────────────────────────────
    @GetMapping
    public List<MemberResponse> getAllMembers() {
        return memberService.getAllMembers().stream()
                .map(m -> new MemberResponse(m.getId(), m.getName(), m.getRole(),
                        m.getAvatarUrl(), m.getFacebookUrl(), m.getGithubUrl(), m.getLinkedinUrl()))
                .collect(Collectors.toList());
    }

    // ── CREATE ───────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member request) {
        Map<String, Object> error = new HashMap<>();
        if (request.getName() == null || request.getName().isBlank()) {
            error.put("message", "Tên thành viên không được để trống");
            return ResponseEntity.badRequest().body(error);
        }
        if (request.getRole() == null || request.getRole().isBlank()) {
            error.put("message", "Chức vụ không được để trống");
            return ResponseEntity.badRequest().body(error);
        }

        Member member = new Member();
        member.setName(request.getName().trim());
        member.setRole(request.getRole().trim());
        member.setAvatarUrl(request.getAvatarUrl());
        member.setFacebookUrl(request.getFacebookUrl());
        member.setGithubUrl(request.getGithubUrl());
        member.setLinkedinUrl(request.getLinkedinUrl());

        Member saved = memberRepository.save(member);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MemberResponse(saved.getId(), saved.getName(), saved.getRole(),
                        saved.getAvatarUrl(), saved.getFacebookUrl(), saved.getGithubUrl(), saved.getLinkedinUrl()));
    }

    // ── UPDATE ───────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id, @RequestBody Member request) {
        Map<String, Object> error = new HashMap<>();
        Optional<Member> optional = memberRepository.findById(id);
        if (optional.isEmpty()) {
            error.put("message", "Không tìm thấy thành viên với id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        Member member = optional.get();
        if (request.getName()        != null && !request.getName().isBlank())   member.setName(request.getName().trim());
        if (request.getRole()        != null && !request.getRole().isBlank())   member.setRole(request.getRole().trim());
        if (request.getAvatarUrl()   != null) member.setAvatarUrl(request.getAvatarUrl());
        if (request.getFacebookUrl() != null) member.setFacebookUrl(request.getFacebookUrl());
        if (request.getGithubUrl()   != null) member.setGithubUrl(request.getGithubUrl());
        if (request.getLinkedinUrl() != null) member.setLinkedinUrl(request.getLinkedinUrl());

        Member saved = memberRepository.save(member);
        return ResponseEntity.ok(new MemberResponse(saved.getId(), saved.getName(), saved.getRole(),
                saved.getAvatarUrl(), saved.getFacebookUrl(), saved.getGithubUrl(), saved.getLinkedinUrl()));
    }

    // ── DELETE ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        if (!memberRepository.existsById(id)) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Không tìm thấy thành viên với id = " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        memberRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Đã xóa thành viên thành công");
        return ResponseEntity.ok(result);
    }
}
