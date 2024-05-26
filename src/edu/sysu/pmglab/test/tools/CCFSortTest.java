package edu.sysu.pmglab.test.tools;

import edu.sysu.pmglab.ccf.toolkit.CCFSorter;
import edu.sysu.pmglab.sdfa.sv.SVCoordinate;

import java.io.IOException;

/**
 * @author Wenjie Peng
 * @create 2024-05-20 00:44
 * @description
 */
public class CCFSortTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        long l = System.currentTimeMillis();
        new CCFSorter<SVCoordinate.CoordinateInterval>(
                "/Users/wenjiepeng/Desktop/SV/data/ukbb/ukbb2.sdf",
                "/Users/wenjiepeng/Desktop/SV/data/ukbb/ukbb3.sdf")
                .setComparableFields("Location::Coordinate")
                .setElementMapper(record -> SVCoordinate.CoordinateInterval.decode(record.get(0)),
                        SVCoordinate.CoordinateInterval::getPos)
                .setThreads(1)
                .setRefinedBucketSize((int)Math.sqrt(1744))
                .silent(false)
                .submit();
        System.out.println(System.currentTimeMillis() - l);
    }

    static class IntArrayTest implements Comparable<IntArrayTest> {
        int[] b;

        public IntArrayTest(int[] b) {
            this.b = b;
        }

        @Override
        public int compareTo(IntArrayTest o) {
            int status = Integer.compare(b[0], o.b[0]);
            if (status == 0) {
                status = Integer.compare(b[1], o.b[1]);
                return status == 0 ? Integer.compare(b[2], o.b[2]) : status;
            }
            return status;
        }
    }
}
