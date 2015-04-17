/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aixforce.web.utils;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author rkzhang
 *
 */
public abstract class StringUtils extends org.apache.commons.lang3.StringUtils {
	
	public static List<byte[]> splitString(String data, int len){
		byte[] all = data.getBytes();
		List<byte[]> list = new ArrayList<byte[]>();
		for(int i = 0; i < all.length; i = i + len){
			int end = i + len;
			if(end >= all.length){
				end = all.length;
			}
			byte[] temp = ArrayUtils.subarray(all, i, end);
			list.add(temp);
			
		}
		return list;
	}
	
	public static List<byte[]> splitByteArray(byte[] all, int len){
		List<byte[]> list = new ArrayList<byte[]>();
		for(int i = 0; i < all.length; i = i + len){
			int end = i + len;
			if(end >= all.length){
				end = all.length;
			}
			byte[] temp = ArrayUtils.subarray(all, i, end);
			list.add(temp);
			
		}
		return list;
	}
	
	public static void main(String[] args) {
		
		String aaa = "麦当劳表示，由于会对所有新的供应商及其工厂进行严格审核，包括：风险和土地使用的审核、灌溉与用水管理系统、饲料、土壤添加剂和杀虫剂的使用；人员卫生、田间卫生到工作环境；田间异物控制等，因此恢复蔬菜供应需要较长时间。上海门店恢复时间暂定于9月12日左右，预计9月第三周之后，全国麦当劳餐厅的蔬菜供应将恢复正常。";
		System.out.println(aaa.getBytes().length);
		List<byte[]> byteList =  splitString(aaa, 117);
		int i = 0;
		for(byte[] b : byteList){
			System.out.println(b.length);
			i = i + b.length;
		}
		System.out.println(i);
	}
}
