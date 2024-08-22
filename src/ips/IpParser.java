package ips;

import java.nio.charset.StandardCharsets;

/**
 * Parse IP as a string. Optimized methods don't currently check for incorrect data!
 */
public class IpParser {

    public int ipToInt(String ip) {
        String[] split = ip.split("\\.");
        return (Integer.parseInt(split[0]) << 24) + (Integer.parseInt(split[1]) << 16) +
                (Integer.parseInt(split[2]) << 8) + Integer.parseInt(split[3]);
    }

    public void ipToOctetsFast(int[] nums, char[] chars, int start, int end) {
        nums[0] = nums[1] = nums[2] = nums[3] = 0;
        int cNo = start;
        int nNo = 0;
        for (; cNo < end; cNo++) {
            char c = chars[cNo];
            if (c != '.') {
                nums[nNo] = nums[nNo] * 10 + c - '0';
            } else {
                nNo++;
            }
        }
    }

    public void ipToOctetsFast(int[] nums, byte[] bytes, int start, int end) {
        //nums[0] = nums[1] = nums[2] = nums[3] = 0;
        nums[0] = bytes[start] - '0';

        int nNo = 0;
        for (int cNo = start + 1; cNo < end; cNo++) {
            byte c = bytes[cNo];
            if (c != (byte)'.') {
                nums[nNo] = nums[nNo] * 10 + c - '0';
            } else {
                nums[++nNo] = bytes[++cNo] - '0';
            }
        }
    }

    public void ipToOctetsSafe(int[] nums, byte[] bytes, int start, int end) {
        try {
            nums[0] = nums[1] = nums[2] = nums[3] = 0;
            int cNo = start;
            int nNo = 0;
            for (; cNo < end; cNo++) {
                byte c = bytes[cNo];
                if (c >= '0' && c <= '9') {
                    nums[nNo] = nums[nNo] * 10 + c - '0';
                    if (nums[nNo] > 255) {
                        throw new RuntimeException("Impossible octet " + nums[nNo] + "'");
                    }
                } else if (c == '.') {
                    nNo++;
                } else {
                    throw new RuntimeException("Unexpected character '" + c + "'");
                }
            }
        } catch (Exception e) {
            if (0 <= start && start <= end && end <= bytes.length) {
                throw new RuntimeException("Incorrect IP address '" +
                        new String(bytes, start, end - start, StandardCharsets.ISO_8859_1) + "'", e);
            } else {
                throw new RuntimeException("Incorrect bytes to parse IP address: bytes amount = " + bytes.length +
                        ", start = " + start + ", end = " + end, e);
            }
        }
    }
}
