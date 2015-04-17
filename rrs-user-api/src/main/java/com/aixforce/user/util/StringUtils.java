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

package com.aixforce.user.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author rkzhang
 *
 */
public abstract class StringUtils {

	public static List<byte[]> splitString(String data, int len) {
		byte[] all = data.getBytes();
		List<byte[]> list = new ArrayList<byte[]>();
		for (int i = 0; i < all.length; i = i + len) {
			int end = i + len;
			if (end >= all.length) {
				end = all.length;
			}
			byte[] temp = ArrayUtils.subarray(all, i, end);
			list.add(temp);

		}
		return list;
	}

	public static List<byte[]> splitByteArray(byte[] all, int len) {
		List<byte[]> list = new ArrayList<byte[]>();
		for (int i = 0; i < all.length; i = i + len) {
			int end = i + len;
			if (end >= all.length) {
				end = all.length;
			}
			byte[] temp = ArrayUtils.subarray(all, i, end);
			list.add(temp);

		}
		return list;
	}

}
