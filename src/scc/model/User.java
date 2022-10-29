package scc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a User, as returned to the clients
 */
public class User {

	private String nickname;
	private String name;
	private String pwd;
	private String photoId;

	public User() {

	}

	public User(@JsonProperty("nickname") String nickname, @JsonProperty("name") String name, @JsonProperty("pwd") String pwd, @JsonProperty("photoId") String photoId) {
		super();
		this.nickname = nickname;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
	}

	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
	@Override
	public String toString() {
		return "User [nickname=" + nickname + ", name=" + name + ", pwd=" + pwd + ", photoId=" + photoId + "]";
	}

}
