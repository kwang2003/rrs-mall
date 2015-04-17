package com.rrs.arrivegift.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yea01 on 2014/9/11.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings("serial")
public class ShopGiftConfig implements Serializable {

	@Getter
	@Setter
	private Long id;

	@Getter
	@Setter
	private Long userid;

	@Getter
	@Setter
	private Long shopid;

	@Getter
	@Setter
	private String weekday;

	@Getter
	@Setter
	private Weekday weekdayBean;

	@Getter
	@Setter
	private String amstart;

	@Getter
	@Setter
	private String amend;

	@Getter
	@Setter
	private String pmstart;

	@Getter
	@Setter
	private String pmend;

	@Getter
	@Setter
	private int enable;

	@Getter
	@Setter
	private Date createdat;

	@Getter
	@Setter
	private Date updatedat;
}
