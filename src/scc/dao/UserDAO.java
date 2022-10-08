package scc.dao;

import scc.model.User;
import java.util.Arrays;

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
	private String[] channelIds;

	public UserDAO() {
	}

	public UserDAO( User u) {
		this(u.getNickname(), u.getName(), u.getPwd(), u.getPhotoId(), u.getChannelIds());
	}

	public UserDAO(String nickname, String name, String pwd, String photoId, String[] channelIds) {
		super();
		this.nickname = nickname;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.channelIds = channelIds;
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
	public String[] getChannelIds() {
		return channelIds == null ? new String[0] : channelIds ;
	}
	public void setChannelIds(String[] channelIds) {
		this.channelIds = channelIds;
	}
	public User toUser() {
		return new User( nickname, name, pwd, photoId, channelIds == null ? null : Arrays.copyOf(channelIds,channelIds.length));
	}
	@Override
	public String toString() {
		return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", nickname=" + nickname + ", name=" + name + ", pwd=" + pwd
				+ ", photoId=" + photoId + ", channelIds=" + Arrays.toString(channelIds) + "]";
	}

}
