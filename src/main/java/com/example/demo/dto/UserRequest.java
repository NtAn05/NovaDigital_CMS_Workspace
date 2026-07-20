package com.example.demo.dto;

public class UserRequest {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role;
    private Boolean enabled;

    public String getUsername()          { return username; }
    public void   setUsername(String v)  { this.username = v; }

    public String getFullName()          { return fullName; }
    public void   setFullName(String v)  { this.fullName = v; }

    public String getEmail()             { return email; }
    public void   setEmail(String v)     { this.email = v; }

    public String getPhone()             { return phone; }
    public void   setPhone(String v)     { this.phone = v; }

    public String getPassword()          { return password; }
    public void   setPassword(String v)  { this.password = v; }

    public String getRole()              { return role; }
    public void   setRole(String v)      { this.role = v; }

    public Boolean getEnabled()          { return enabled; }
    public void    setEnabled(Boolean v) { this.enabled = v; }
}
