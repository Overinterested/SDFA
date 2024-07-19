package edu.sysu.pmglab.sdfa.merge.newMerge.order;

import edu.sysu.pmglab.gbc.genome.Chromosome;
import edu.sysu.pmglab.sdfa.SDFReader;
import edu.sysu.pmglab.sdfa.sv.SVCoordinate;
import edu.sysu.pmglab.sdfa.sv.UnifiedSV;

import java.io.IOException;
import java.util.Arrays;

public class LoserTree {
    boolean overFileThreshold; //
    static int numOfFileThreshold = 20;
    private final int[] tree;  // 败者树
    private final int numOfLeaf;  // 叶子结点数
    private final UnifiedSV[] leaves;  // 叶子结点
    private SDFReader[] readerArray; // 文件读取器数组
    private static final UnifiedSV MAX_RECORD = new UnifiedSV().setCoordinate(new SVCoordinate(Integer.MAX_VALUE, Integer.MAX_VALUE, Chromosome.unknown));

    /**
     * 构造函数，初始化败者树
     *
     * @param numOfLeaf     叶子结点数
     * @param files 文件路径数组
     */
    public LoserTree(int numOfLeaf, String[] files) throws IOException {
        this.numOfLeaf = numOfLeaf;
        if (numOfLeaf>numOfFileThreshold){
            overFileThreshold = true;
        }
        tree = new int[numOfLeaf];
        leaves = new UnifiedSV[numOfLeaf];
        readerArray = new SDFReader[numOfLeaf];
        for (int i = 0; i < numOfLeaf; i++) {
            readerArray[i] = new SDFReader(files[i]);
        }
        Arrays.fill(tree, -1);
    }

    /**
     * 初始化叶子结点
     *
     * @param index 叶子结点索引
     * @param value 叶子结点值
     */
    public void setLeaf(int index, UnifiedSV value) {
        leaves[index] = value;
    }

    /**
     * 构建败者树
     */
    public void build() {
        for (int i = 0; i < numOfLeaf; i++) {
            play(i);
        }
    }

    /**
     * 比赛函数，更新败者树
     *
     * @param leaf 新的叶子结点索引
     */
    private void play(int leaf) {
        int winner = leaf;
        int parent = (leaf + numOfLeaf) / 2;
        while (parent > 0) {
            if (leaves[winner].compareTo(leaves[tree[parent]]) > 0) {
                int temp = winner;
                winner = tree[parent];
                tree[parent] = temp;
            }
            parent /= 2;
        }
        tree[0] = winner;
    }

    /**
     * 获取当前败者树的胜者（即最小值）
     *
     * @return 胜者索引
     */
    public int getWinner() {
        return tree[0];
    }

    /**
     * 更新败者树
     *
     * @param index 需要更新的叶子结点索引
     * @param value 新值
     */
    public void update(int index, UnifiedSV value) {
        leaves[index] = value;
        play(index);
    }

    /**
     * 从文件读取下一个整数
     *
     * @param index 文件索引
     * @return 读取到的整数，文件结束时返回 Integer.MAX_VALUE
     * @throws IOException 如果读取时发生错误
     */
    public UnifiedSV readNextValue(int index) throws IOException {
        SDFReader sdfReader = readerArray[index];
        if (overFileThreshold){
            sdfReader.restart();
        }
        UnifiedSV sv = sdfReader.read();
        if (sv != null) {
            sdfReader.close();
            return sv;
        } else {
            return MAX_RECORD; // 文件结束
        }
    }

    /**
     * 关闭所有文件读取器
     *
     * @throws IOException 如果关闭时发生错误
     */
    public void closeReaders() throws IOException {
        for (SDFReader reader : readerArray) {
            reader.close();
        }
    }

    public static void main(String[] args) {
        String[] files = {"file1.txt", "file2.txt", "file3.txt", "file4.txt"};
        int k = files.length;

        try {
            LoserTree lt = new LoserTree(k, files);
            for (int i = 0; i < k; i++) {
                lt.setLeaf(i, lt.readNextValue(i));
            }

            lt.build();

            while (true) {
                int winnerIndex = lt.getWinner();
                UnifiedSV winnerValue = lt.leaves[winnerIndex];

                if (winnerValue == MAX_RECORD) {
                    break; // 所有文件均已读取完毕
                }

                // 输出当前最小值（即胜者）
                System.out.println(winnerValue);

                // 更新胜者对应的文件中的最小值
                lt.update(winnerIndex, lt.readNextValue(winnerIndex));
            }

            lt.closeReaders();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}