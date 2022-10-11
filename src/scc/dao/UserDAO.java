package scc.dao;

import scc.model.User;

/**
 * Represents a User, as stored in the database
 */
public class UserDAO {

	private String _rid;
	private String _ts;
	private String nickname;
	private String name;
	private String pwd;
	private String photoId;

	public UserDAO() {
	}

	public UserDAO( User u) {
		this(u.getNickname(), u.getName(), u.getPwd(), u.getPhotoId());
	}

	public UserDAO(String nickname, String name, String pwd, String photoId) {
		super();
		this.nickname = nickname;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
	}

	public String get_rid() {
		return _rid;
	}
	public void set_rid(String _rid) {
		this._rid = _rid;
	}
	public String get_ts() {
		return _ts;
	}
	public void set_ts(String _ts) {
		this._ts = _ts;
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
	public User toUser() {
		return new User( nickname, name, pwd, photoId);
	}
	@Override
	public String toString() {
		return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", nickname=" + nickname + ", name=" + name + ", pwd=" + pwd
				+ ", photoId=" + photoId + "]";
	}

}
