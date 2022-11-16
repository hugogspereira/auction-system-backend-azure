package scc.model;

public class Login {

    private String nickname;
    private String pwd;

    public Login() {
    }

    public Login(String nickname, String pwd) {
        this.nickname = nickname;
        this.pwd = pwd;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}

