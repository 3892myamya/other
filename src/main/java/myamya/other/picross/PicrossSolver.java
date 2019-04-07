package myamya.other.picross;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PicrossSolver {
	enum MASU {
		SPACE("　"), BLACK("■"), BAN("×");
		String str;

		MASU(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	static class Field {
		private final MASU[][] masu;
		private final List<List<Integer>> horizonalHints;
		private final List<List<Integer>> verticalHints;

		Field(int ySize, int xSize, List<List<Integer>> horizonalHints, List<List<Integer>> verticalHints) {
			masu = new MASU[ySize][xSize];
			for (int y = 0; y < getYlength(); y++) {
				for (int x = 0; x < getXlength(); x++) {
					masu[y][x] = MASU.SPACE;
				}
			}
			this.horizonalHints = horizonalHints;
			this.verticalHints = verticalHints;
		}

		public Field(Field field) {
			masu = new MASU[field.getYlength()][field.getXlength()];
			for (int y = 0; y < getYlength(); y++) {
				for (int x = 0; x < getXlength(); x++) {
					masu[y][x] = field.masu[y][x];
				}
			}
			this.horizonalHints = field.horizonalHints;
			this.verticalHints = field.verticalHints;
		}

		List<MASU> getHorizonalLine(int y) {
			return Arrays.asList(masu[y]);
		}

		List<MASU> getVerticalLine(int x) {
			List<MASU> result = new ArrayList<>();
			for (int y = 0; y < masu.length; y++) {
				result.add(masu[y][x]);
			}
			return result;
		}

		void setHorizonalLine(int y, List<MASU> result) {
			for (int x = 0; x < masu[y].length; x++) {
				masu[y][x] = result.get(x);
			}
		}

		void setVerticalLine(int x, List<MASU> result) {
			for (int y = 0; y < masu.length; y++) {
				masu[y][x] = result.get(y);
			}
		}

		List<Integer> getHorizonalHint(int y) {
			return horizonalHints.get(y);
		}

		List<Integer> getVerticalHint(int x) {
			return verticalHints.get(x);
		}

		int getYlength() {
			return masu.length;
		}

		int getXlength() {
			return masu[0].length;
		}

		public boolean solved() {
			for (int y = 0; y < getYlength(); y++) {
				for (int x = 0; x < getXlength(); x++) {
					if (masu[y][x] == MASU.SPACE) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int y = 0; y < getYlength(); y++) {
				for (int x = 0; x < getXlength(); x++) {
					sb.append(masu[y][x].toString());
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public void setResults(List<List<MASU>> yLineResult, List<List<MASU>> xLineResult) {
			for (int y = 0; y < yLineResult.size(); y++) {
				for (int x = 0; x < yLineResult.get(y).size(); x++) {
					if (yLineResult.get(y).get(x) != MASU.SPACE) {
						masu[y][x] = yLineResult.get(y).get(x);
					}
				}
			}
			for (int x = 0; x < xLineResult.size(); x++) {
				for (int y = 0; y < xLineResult.get(x).size(); y++) {
					if (xLineResult.get(x).get(y) != MASU.SPACE) {
						masu[y][x] = xLineResult.get(x).get(y);
					}
				}
			}
		}

		public int getAllBlackCnt() {
			int result = 0;
			for (List<Integer> horizonalHint : horizonalHints) {
				for (int oneHint : horizonalHint) {
					result = result + oneHint;
				}
			}
			return result;
		}

		public int getSolvedBlackCount() {
			int result = 0;
			for (int y = 0; y < getYlength(); y++) {
				for (int x = 0; x < getXlength(); x++) {
					if (masu[y][x] == MASU.BLACK) {
						result++;
					}
				}
			}
			return result;
		}
	}

	public static Field makeField(List<String> elements) {
		int ySize = Integer.parseInt(elements.remove(0));
		int xSize = Integer.parseInt(elements.remove(0));
		List<List<Integer>> horizonalHints = new ArrayList<>();
		for (int i = 0; i < ySize; i++) {
			List<String> horizonalHintStr = Arrays.asList(elements.remove(0).split(","));
			List<Integer> horizonalHint = new ArrayList<>();
			for (String oneHintStr : horizonalHintStr) {
				if (!oneHintStr.equals("0")) {
					horizonalHint.add(Integer.parseInt(oneHintStr));
				}
			}
			horizonalHints.add(horizonalHint);
		}
		List<List<Integer>> verticalHints = new ArrayList<>();
		for (int i = 0; i < xSize; i++) {
			List<String> verticalHintStr = Arrays.asList(elements.remove(0).split(","));
			List<Integer> verticalHint = new ArrayList<>();
			for (String oneHintStr : verticalHintStr) {
				verticalHint.add(Integer.parseInt(oneHintStr));
			}
			verticalHints.add(verticalHint);

		}
		return new Field(ySize, xSize, horizonalHints, verticalHints);
	}

	public static void main(String[] args) throws InterruptedException {
		Field field = makeField(new ArrayList<>(Arrays.asList(args)));
		String result = solve(field);
		if (result != null) {
			System.out.println(result);
		} else {
			System.out.println("ちょっと無理でした...");
		}
	}

	static int serveyCnt = 0;

	private static String solve(Field field) throws InterruptedException {
		String before;
		while (!field.solved()) {
			// Thread.sleep(5);
			serveyCnt++;
			System.out.println(serveyCnt + "回目の調査開始:" + field.getSolvedBlackCount() + "/" + field.getAllBlackCnt());
			System.out.println(field.toString());
			before = field.toString();
			List<List<MASU>> yLineResult = new ArrayList<>();
			for (int y = 0; y < field.getYlength(); y++) {
				List<MASU> result = solveOneLine(field.getHorizonalLine(y), field.getHorizonalHint(y));
				if (result == null) {
					return null;
				}
				yLineResult.add(result);
			}
			List<List<MASU>> xLineResult = new ArrayList<>();
			for (int x = 0; x < field.getXlength(); x++) {
				List<MASU> result = solveOneLine(field.getVerticalLine(x), field.getVerticalHint(x));
				if (result == null) {
					return null;
				}
				xLineResult.add(result);
			}
			field.setResults(yLineResult, xLineResult);
			if (field.toString().equals(before)) {
				boolean isAdvance = false;
				for (int y = 0; y < field.getYlength(); y++) {
					for (int x = 0; x < field.getXlength(); x++) {
						if (field.masu[y][x] == MASU.SPACE) {
							Field virtual = new Field(field);
							virtual.masu[y][x] = MASU.BLACK;
							String result = solve(virtual);
							if (result == null) {
								field.masu[y][x] = MASU.BAN;
								isAdvance = true;
								break;
							} else {
								return result;
							}
						}
					}
					if (isAdvance) {
						break;
					}
				}
				if (!isAdvance) {
					return null;
				}
			}
		}
		return serveyCnt + "回目でクリア！:" + field.getSolvedBlackCount() + "/" + field.getAllBlackCnt()
				+ System.lineSeparator()
				+ field.toString().replaceAll("×", "　");

	}

	/**
	 * 1列解く。満たす解がなかったらnullを返す。
	 */
	private static List<MASU> solveOneLine(List<MASU> line, List<Integer> hints) {
		List<List<MASU>> candMasuList = new ArrayList<>();
		makeCombination(line.size(), candMasuList, new ArrayList<>(), hints);
		List<MASU> result = line;
		List<MASU> wk = null;
		for (List<MASU> candMasu : candMasuList) {
			boolean flgSkip = false;
			for (int i = 0; i < candMasu.size(); i++) {
				if (result.get(i) != MASU.SPACE &&
						result.get(i) != candMasu.get(i)) {
					flgSkip = true;
					break;
				}
			}
			if (flgSkip) {
				continue;
			}
			if (wk == null) {
				wk = new ArrayList<>(candMasu);
			} else {
				for (int i = 0; i < wk.size(); i++) {
					if (wk.get(i) != candMasu.get(i)) {
						wk.set(i, MASU.SPACE);
					}
				}
			}
		}
		return wk;
	}

	private static void makeCombination(int size, List<List<MASU>> candMasuList, List<MASU> candMasu,
			List<Integer> numbers) {
		if (numbers.size() == 0) {
			for (int i = 0; i < size; i++) {
				candMasu.add(MASU.BAN);
			}
			candMasuList.add(new ArrayList<>(candMasu));
			return;
		}
		int total = numbers.size() - 1;
		for (int num : numbers) {
			total = total + num;
		}
		for (int topBan = 0; topBan < size - total + 1; topBan++) {
			for (int i = 0; i < topBan; i++) {
				candMasu.add(MASU.BAN);
			}
			int num = numbers.remove(0);
			for (int i = 0; i < num; i++) {
				candMasu.add(MASU.BLACK);
			}
			if (numbers.size() != 0) {
				candMasu.add(MASU.BAN);
				makeCombination(size - num - topBan - 1,
						candMasuList, candMasu, numbers);
				for (int i = 0; i < topBan + num + 1; i++) {
					candMasu.remove(candMasu.size() -
							1);
				}
			} else {
				for (int i = 0; i < size - num - topBan; i++) {
					candMasu.add(MASU.BAN);
				}
				candMasuList.add(new ArrayList<>(candMasu));
				for (int i = 0; i < size; i++) {
					candMasu.remove(candMasu.size() -
							1);
				}
			}
			numbers.add(0, num);
		}
	}
}