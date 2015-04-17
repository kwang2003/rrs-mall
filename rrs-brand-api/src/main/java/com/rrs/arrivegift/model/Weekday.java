package com.rrs.arrivegift.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by yea01 on 2014/9/11.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings("serial")
public class Weekday implements Serializable {


	@Getter
	@Setter
	private String mon;
		
	@Getter		
	@Setter
	private String tue;

	@Getter
	@Setter
	private String wed;

	@Getter
	@Setter
	private String thu;		

	@Getter
	@Setter
	private String fri;

	@Getter
	@Setter
	private String sat;
	
	@Getter
	@Setter
	private String sun;
}
