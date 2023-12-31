/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.trust.auth.condition.matcher;

import java.util.Objects;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author lwj
 * @since 2.0.0
 */
public class IpMatcher implements Matcher<String> {

    /**
     * Prefix length in CIDR case
     */
    private final int prefixLen;

    /**
     * Ip address to be matched
     */
    private final String ipBinaryString;

    public IpMatcher(int prefixLen, String ipBinaryString) {
        this.prefixLen = prefixLen;
        this.ipBinaryString = ip2BinaryString(ipBinaryString);
    }

    /**
     * @param ip dotted ip string,
     * @return
     */
    public static String ip2BinaryString(String ip) {
        try {
            String[] ips = ip.split("\\.");
            if (4 != ips.length) {
                RecordLog.error("Error ip={}", ip);
                return "";
            }
            long[] ipLong = new long[4];
            for (int i = 0; i < 4; ++i) {
                ipLong[i] = Long.parseLong(ips[i]);
                if (ipLong[i] < 0 || ipLong[i] > 255) {
                    RecordLog.error("Error ip={}", ip);
                    return "";
                }
            }
            return String.format("%32s", Long.toBinaryString((ipLong[0] << 24)
                + (ipLong[1] << 16)
                + (ipLong[2] << 8)
                + ipLong[3])).replace(" ", "0");
        } catch (Exception e) {
            RecordLog.error("Error ip={}", ip);
        }
        return "";
    }

    public boolean match(String object) {
        if (StringUtil.isEmpty(ipBinaryString)) {
            return false;
        }
        String ipBinary = ip2BinaryString(object);
        if (StringUtil.isEmpty(ipBinary)) {
            return false;
        }
        if (prefixLen <= 0) {
            return ipBinaryString.equals(ipBinary);
        }
        if (ipBinaryString.length() >= prefixLen && ipBinary.length() >= prefixLen) {
            return ipBinaryString.substring(0, prefixLen)
                .equals(ipBinary.substring(0, prefixLen));
        }
        return false;
    }

    public int getPrefixLen() {
        return prefixLen;
    }

    public String getIpBinaryString() {
        return ipBinaryString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IpMatcher ipMatcher = (IpMatcher) o;
        return prefixLen == ipMatcher.prefixLen && Objects.equals(ipBinaryString, ipMatcher.ipBinaryString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefixLen, ipBinaryString);
    }

    @Override
    public String toString() {
        return "IpMatcher{" +
            "prefixLen=" + prefixLen +
            ", ip='" + ipBinaryString + '\'' +
            '}';
    }
}
