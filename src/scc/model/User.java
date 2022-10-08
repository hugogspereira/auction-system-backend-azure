package scc.model;

import java.util.Arrays;

/**
 * Represents a User, as returned to the clients
 */
public class User {

	private String nickname;
	private String name;
	private String pwd;
	private String photoId;
	private String[] channelIds;

	public User(String nickname, String name, String pwd, String photoId, String[] channelIds) {
		super();
		this.nickname = nickname;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.channelIds = channelIds;
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
	@Override
	public String toString() {
		return "User [nickname=" + nickname + ", name=" + name + ", pwd=" + pwd + ", photoId=" + photoId + ", channelIds="
				+ Arrays.toString(channelIds) + "]";
	}

}
