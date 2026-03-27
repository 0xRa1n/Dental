package model;

public class User {

    private String username;
    private String email;
    private String password;
    private String full_name;  
    private String role;

   
    public User(String username, String email, String password, String full_name, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.full_name = full_name;
        this.role = role;
    }

    
    public String getName() {
        return full_name;
    }

    
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getRole() {
        return role;
    }

    
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setRole(String role) {
        this.role = role;
    }
}