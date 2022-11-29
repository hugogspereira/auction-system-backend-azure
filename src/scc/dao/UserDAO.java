package scc.dao;

import scc.model.User;

/**
 * Represents a User, as stored in the database
 */
public class UserDAO {

	//private String _rid;
	//private String _ts;
	private String _id;		//it's the nickname
	private String name;
	private String pwd;
	private String photoId;

	public UserDAO() {
	}

	public UserDAO( User u) {
		this(u.getNickname(), u.getName(), u.getPwd(), u.getPhotoId());
	}

	public UserDAO(String _id, String name, String pwd, String photoId) {
		super();
		this._id = _id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
	}

	/*
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
	 */

	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
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
		return new User(_id, name, pwd, photoId);
	}
	@Override
	public String toString() {
		return "UserDAO [_id=" + _id + ", name=" + name + ", pwd=" + pwd
				+ ", photoId=" + photoId + "]";
	}

}
