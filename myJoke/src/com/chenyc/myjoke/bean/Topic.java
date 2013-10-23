package com.chenyc.myjoke.bean;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "topic")
public class Topic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Topic() {
	}

	@DatabaseField(id = true, useGetSet = true, columnName = "id")
	private Integer id;

	@DatabaseField(useGetSet = true, columnName = "title")
	private String title;

	@DatabaseField(useGetSet = true, columnName = "description")
	private String description;

	@DatabaseField(useGetSet = true, columnName = "small_img")
	@SerializedName("small_img")
	private String smallImg;

	@DatabaseField(useGetSet = true, columnName = "big_img")
	@SerializedName("big_img")
	private String bigImg;

	@DatabaseField(useGetSet = true, columnName = "time")
	private long time;

	@DatabaseField(useGetSet = true, columnName = "type")
	private String type;

	private boolean isFav;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSmallImg() {
		return smallImg;
	}

	public void setSmallImg(String smallImg) {
		this.smallImg = smallImg;
	}

	public String getBigImg() {
		return bigImg;
	}

	public void setBigImg(String bigImg) {
		this.bigImg = bigImg;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isFav() {
		return isFav;
	}

	public void setFav(boolean isFav) {
		this.isFav = isFav;
	}

}
