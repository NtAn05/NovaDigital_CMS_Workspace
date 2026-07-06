package com.example.demo.service;

import com.example.demo.entity.Member;
import com.example.demo.entity.User;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Member> getAllMembers() {
        List<Member> list = new ArrayList<>(memberRepository.findAll());
        
        List<User> users = userRepository.findAll();
        for (User u : users) {
            String role = u.getRole();
            if ("ROLE_MEMBER".equalsIgnoreCase(role) || "Team_Member".equalsIgnoreCase(role)) {
                Member m = new Member();
                m.setId(1000000L + u.getId());
                m.setName(u.getFullName() != null && !u.getFullName().isBlank() ? u.getFullName() : u.getUsername());
                m.setRole("Team Member");
                m.setAvatarUrl("");
                m.setFacebookUrl(null);
                m.setGithubUrl(null);
                m.setLinkedinUrl(null);
                list.add(m);
            }
        }
        return list;
    }
}
