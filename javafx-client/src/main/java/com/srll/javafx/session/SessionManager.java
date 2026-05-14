package com.srll.javafx.session;

import com.srll.javafx.http.dto.AuthResponse;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String jwtToken;
    private Long userId;
    private String username;
    private String role;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void setSession(AuthResponse auth) {
        this.jwtToken  = auth.token();
        this.userId    = auth.userId();
        this.username  = auth.username();
        this.role      = auth.role();
    }

    public void clearSession() {
        this.jwtToken  = null;
        this.userId    = null;
        this.username  = null;
        this.role      = null;
    }

    public boolean isLoggedIn() {
        return jwtToken != null && !jwtToken.isBlank();
    }

    public String getAuthHeader() {
        return "Bearer " + jwtToken;
    }

    public String getJwtToken()  { return jwtToken;  }
    public Long   getUserId()    { return userId;     }
    public String getUsername()  { return username;   }
    public String getRole()      { return role;       }
}
