package myamya.other.yajilin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class YajilinSolver {

	/**
	 * 方向を示す列挙型
	 */
	enum Direction {
	UP("u"), RIGHT("r"), DOWN("d"), LEFT("l");
		private final String str;

		Direction(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}

		public Direction opposite() {
			if (this == UP) {
				return DOWN;
			} else if (this == RIGHT) {
				return LEFT;
			} else if (this == DOWN) {
				return UP;
			} else if (this == LEFT) {
				return RIGHT;
			} else {
				return null;
			}
		}

		public static Direction getByStr(String str) {
			for (Direction one : Direction.values()) {
				if (one.toString().equals(str)) {
					return one;
				}
			}
			return null;
		}
	}

	enum MasuImpl implements Masu {
		/** 白マス */
		SPACE("　", new ArrayList<>(), false),
		/** 黒マス */
		BLACK("■", new ArrayList<>(), false),
		/** 黒にならないことが確定したマス */
		NOT_BLACK("・", new ArrayList<>(), false),
		/** */
		UP_RIGHT("└", Arrays.asList(new Direction[] { Direction.UP, Direction.RIGHT }), true),
		/** */
		UP_DOWN("│", Arrays.asList(new Direction[] { Direction.UP, Direction.DOWN }), true),
		/** */
		UP_LEFT("┘", Arrays.asList(new Direction[] { Direction.UP, Direction.LEFT }), true),
		/** */
		RIGHT_DOWN("┌", Arrays.asList(new Direction[] { Direction.RIGHT, Direction.DOWN }), true),
		/** */
		RIGHT_LEFT("─", Arrays.asList(new Direction[] { Direction.RIGHT, Direction.LEFT }), true),
		/** */
		DOWN_LEFT("┐", Arrays.asList(new Direction[] { Direction.DOWN, Direction.LEFT }), true),
		/** 外壁。 */
		WALL("＊", new ArrayList<>(), false);

		private final String str;
		private final List<Direction> targetDirection;
		private final boolean isPath;

		MasuImpl(String str, List<Direction> targetDirection, boolean isPath) {
			this.str = str;
			this.targetDirection = targetDirection;
			this.isPath = isPath;
		}

		@Override
		public String toString() {
			return str;
		}

		@Override
		public List<Direction> getTargetDirection() {
			return targetDirection;
		}

		@Override
		public boolean isPath() {
			return isPath;
		}

	}

	/**
	 * 1マスを示すクラス
	 */
	interface Masu {
		/**
		 * 自分が向いている方向リスト
		 */
		List<Direction> getTargetDirection();

		boolean isPath();
	}

	static class Position {

		private final int yIndex;
		private final int xIndex;

		public Position(int yIndex, int xIndex) {
			this.yIndex = yIndex;
			this.xIndex = xIndex;
		}

		public int getyIndex() {
			return yIndex;
		}

		public int getxIndex() {
			return xIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + xIndex;
			result = prime * result + yIndex;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Position other = (Position) obj;
			if (xIndex != other.xIndex)
				return false;
			if (yIndex != other.yIndex)
				return false;
			return true;
		}

	}

	/**
	 * 矢印のマス
	 */
	static class Arrow implements Masu {
		private final Direction direction;
		private final int count;

		public Arrow(Direction direction, int count) {
			this.direction = direction;
			this.count = count;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return direction.toString() + count;
		}

		@Override
		public List<Direction> getTargetDirection() {
			return new ArrayList<>();
		}

		@Override
		public boolean isPath() {
			return false;
		}

	}

	/**
	 * 盤面全体
	 */
	static class Field {
		private final Masu[][] masu;

		public Field(List<String> fieldStr) {
			int width = fieldStr.get(0).length() / 2;
			int height = fieldStr.size();
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				String line = fieldStr.get(yIndex);
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					String oneMashStr = line.substring(xIndex * 2, xIndex * 2 + 2);
					if (oneMashStr.equals("00")) {
						masu[yIndex][xIndex] = MasuImpl.SPACE;
					} else {
						masu[yIndex][xIndex] = new Arrow(Direction.getByStr(oneMashStr.substring(0, 1)),
								Integer.parseInt(oneMashStr.substring(1, 2)));
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
		}

		/**
		 * 盤面の文字列表現を返す
		*/
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Masu[] line : masu) {
				sb.append(System.lineSeparator());
				for (Masu masu : line) {
					sb.append(masu.toString());
				}
			}
			return sb.toString();
		}

		/**
		 * パズルが解けているか。
		 * スペースがない状態を解けているとみなす
		 */
		public boolean isSolved() {
			for (Masu[] line : masu) {
				for (Masu masu : line) {
					if (masu == MasuImpl.SPACE || masu == MasuImpl.NOT_BLACK) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 現在の盤面の状態がヤジリンのルール上問題ないかを調べる。
		 */
		public boolean isOk() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!oneIsOk(yIndex, xIndex)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 指定した座標がヤジリンのルール上問題ないかを調べる。
		 */
		private boolean oneIsOk(int yIndex, int xIndex) {
			if (yIndex < 0 || xIndex < 0 || yIndex >= getYLength() || xIndex >= getXLength()) {
				// はみ出してるマスは調査対象外
				return true;
			}
			// 各方向にあるマスをマッピング
			Masu nowMasu = masu[yIndex][xIndex];
			if (nowMasu instanceof Arrow) {
				// 矢印は調査対象外
				return true;
			}
			Map<Direction, Masu> masuMap = new HashMap<>();
			masuMap.put(Direction.UP, yIndex == 0 ? MasuImpl.WALL : masu[yIndex - 1][xIndex]);
			masuMap.put(Direction.RIGHT, xIndex == getXLength() - 1 ? MasuImpl.WALL : masu[yIndex][xIndex + 1]);
			masuMap.put(Direction.DOWN, yIndex == getYLength() - 1 ? MasuImpl.WALL : masu[yIndex + 1][xIndex]);
			masuMap.put(Direction.LEFT, xIndex == 0 ? MasuImpl.WALL : masu[yIndex][xIndex - 1]);
			if (nowMasu == MasuImpl.BLACK) {
				for (Entry<Direction, Masu> entry : masuMap.entrySet()) {
					if (entry.getValue() == MasuImpl.BLACK
							|| entry.getValue().getTargetDirection().contains(entry.getKey().opposite())) {
						// 黒マスは、隣接マスに黒マスや自分向きのマスがあってはならない
						return false;
					}
				}
				return true;
			} else if (nowMasu == MasuImpl.NOT_BLACK) {
				int cnt = 0;
				for (Entry<Direction, Masu> entry : masuMap.entrySet()) {
					if (entry.getValue() != MasuImpl.SPACE && entry.getValue() != MasuImpl.NOT_BLACK
							&& !entry.getValue().getTargetDirection().contains(entry.getKey().opposite())) {
						cnt++;
					}
				}
				// 黒マスでないマスは、隣接マスに向いたときに受け入れ不可能なマスが2個を超えてはならない
				if (cnt > 2) {
					return false;
				}
				return true;
			} else {
				for (Entry<Direction, Masu> entry : masuMap.entrySet()) {
					if (nowMasu.getTargetDirection().contains(entry.getKey())) {
						if (entry.getValue() != MasuImpl.SPACE && entry.getValue() != MasuImpl.NOT_BLACK
								&& !entry.getValue().getTargetDirection().contains(entry.getKey().opposite())) {
							// 自分が向いている方向にあるマスが自分向きまたはスペースでなければならない
							return false;
						}
					} else {
						if (entry.getValue().getTargetDirection().contains(entry.getKey().opposite())) {
							// 自分が向いていない方向にあるマスが自分向きであってはならない
							return false;
						}
					}
				}
				// 閉路チェック
				List<Position> root = new ArrayList<>();
				Position firstPosition = new Position(yIndex, xIndex);
				Position position = new Position(yIndex, xIndex);
				Direction from = nowMasu.getTargetDirection().get(0).opposite();
				while (true) {
					Masu rootingMasu = masu[position.getyIndex()][position.getxIndex()];
					if (!rootingMasu.isPath()) {
						return true;
					}
					Direction nextDirection = rootingMasu.getTargetDirection().get(0) != from
							? rootingMasu.getTargetDirection().get(0)
							: rootingMasu.getTargetDirection().get(1);
					from = nextDirection.opposite();
					if (nextDirection == Direction.UP) {
						position = new Position(position.getyIndex() - 1, position.getxIndex());
					} else if (nextDirection == Direction.RIGHT) {
						position = new Position(position.getyIndex(), position.getxIndex() + 1);
					} else if (nextDirection == Direction.DOWN) {
						position = new Position(position.getyIndex() + 1, position.getxIndex());
					} else if (nextDirection == Direction.LEFT) {
						position = new Position(position.getyIndex(), position.getxIndex() - 1);
					}
					root.add(position);
					if (firstPosition.equals(position)) {
						// 閉路が完成
						break;
					}
				}
				for (int y = 0; y < getYLength(); y++) {
					for (int x = 0; x < getXLength(); x++) {
						Masu checkMasu = masu[y][x];
						if (checkMasu.isPath()
								&& !root.contains(new Position(y, x))) {
							// 閉路に含まれない道があればアウト
							return false;
						}
					}
				}
				return true;
			}
		}

		/**
		 * 全てのマスを調査し、候補を確定する。
		 * もし、何も置けないマスを発見したら、falseを返す。
		 */
		public boolean serveyAll(int recursiveCnt) {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!serveyOne(recursiveCnt, yIndex, xIndex)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * あるマスを調査し、候補を確定する。
		 * もし、何も置けなかったらfalseを返す。
		 */
		private boolean serveyOne(int recursiveCnt, int yIndex, int xIndex) {
			if (yIndex < 0 || xIndex < 0 || yIndex >= getYLength() || xIndex >= getXLength()) {
				// はみ出してるマスは調査対象外
				return true;
			}
			Masu nowMasu = masu[yIndex][xIndex];
			if (nowMasu != MasuImpl.SPACE && nowMasu != MasuImpl.NOT_BLACK) {
				return true;
			}
			// 候補を1つずつ調査
			List<Masu> masuCand = new LinkedList<>(
					Arrays.asList(new MasuImpl[] { MasuImpl.BLACK, MasuImpl.UP_RIGHT, MasuImpl.UP_DOWN,
							MasuImpl.UP_LEFT, MasuImpl.RIGHT_DOWN, MasuImpl.RIGHT_LEFT, MasuImpl.DOWN_LEFT }));
			if (nowMasu == MasuImpl.NOT_BLACK) {
				masuCand.remove(MasuImpl.BLACK);
			}
			for (Iterator<Masu> iterator = masuCand.iterator(); iterator.hasNext();) {
				Masu masu = (Masu) iterator.next();
				Field virtual = new Field(this);
				virtual.masu[yIndex][xIndex] = masu;
				// ためしに入れてみてチェック
				if (!virtual.oneIsOk(yIndex, xIndex)) {
					iterator.remove();
					continue;
				}
				// 再帰調査をする場合
				if (recursiveCnt != 0) {
					// 置いたマスの周辺4マスに置けなくなるマスが発生する場合、候補から除外
					if (!virtual.serveyOne(recursiveCnt - 1, yIndex - 1, xIndex) ||
							!virtual.serveyOne(recursiveCnt - 1, yIndex, xIndex + 1) ||
							!virtual.serveyOne(recursiveCnt - 1, yIndex + 1, xIndex) ||
							!virtual.serveyOne(recursiveCnt - 1, yIndex, xIndex - 1)) {
						iterator.remove();
					}
				}
			}
			if (masuCand.size() == 0) {
				// 候補が0件の場合、このままでは正答にたどり着かない
				return false;
			}
			if (masuCand.size() == 1) {
				// 候補が1つに絞れたら確定する。
				masu[yIndex][xIndex] = masuCand.get(0);
				// 隣接するスペースは黒マスでないことが確定する
				if (masu[yIndex][xIndex].isPath()) {
					for (Direction direction : masu[yIndex][xIndex].getTargetDirection()) {
						if (direction == Direction.UP) {
							if (masu[yIndex - 1][xIndex] == MasuImpl.SPACE) {
								masu[yIndex - 1][xIndex] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.RIGHT) {
							if (masu[yIndex][xIndex + 1] == MasuImpl.SPACE) {
								masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.DOWN) {
							if (masu[yIndex + 1][xIndex] == MasuImpl.SPACE) {
								masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
							}
						} else if (direction == Direction.LEFT) {
							if (masu[yIndex][xIndex - 1] == MasuImpl.SPACE) {
								masu[yIndex][xIndex - 1] = MasuImpl.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		public boolean serveyArrow(int recursiveCnt) {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!(masu[yIndex][xIndex] instanceof Arrow)) {
						// 矢印以外であれば調査対象外
						continue;
					}
					Arrow arrow = (Arrow) masu[yIndex][xIndex];
					List<Position> searchPositionList = new ArrayList<>();
					if (arrow.getDirection() == Direction.UP) {
						for (int searchY = yIndex - 1; searchY >= 0; searchY--) {
							searchPositionList.add(new Position(searchY, xIndex));
						}
					} else if (arrow.getDirection() == Direction.RIGHT) {
						for (int searchX = xIndex + 1; searchX < getXLength(); searchX++) {
							searchPositionList.add(new Position(yIndex, searchX));
						}
					} else if (arrow.getDirection() == Direction.DOWN) {
						for (int searchY = yIndex + 1; searchY < getYLength(); searchY++) {
							searchPositionList.add(new Position(searchY, xIndex));
						}
					} else if (arrow.getDirection() == Direction.LEFT) {
						for (int searchX = xIndex - 1; searchX >= 0; searchX--) {
							searchPositionList.add(new Position(yIndex, searchX));
						}
					}
					List<Integer> fixedBlackPositionIndexList = new ArrayList<>();
					for (int i = 0; i < searchPositionList.size(); i++) {
						Position pos = searchPositionList.get(i);
						if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.BLACK) {
							fixedBlackPositionIndexList.add(i);
						}
					}
					// 考えられる黒マスの置き方を全て列挙
					List<Set<Integer>> combination = getCombination(searchPositionList.size(), arrow.getCount());
					for (Iterator<Set<Integer>> iterator = combination.iterator(); iterator.hasNext();) {
						Set<Integer> oneCombi = iterator.next();
						boolean isConflict = false;
						for (int idx : fixedBlackPositionIndexList) {
							if (!oneCombi.contains(idx)) {
								isConflict = true;
								break;
							}
						}
						// 既に決まった黒マスを含まない候補は除外
						if (isConflict) {
							iterator.remove();
							continue;
						}
						Field virtual = new Field(this);
						for (Integer idx : oneCombi) {
							int targetyIndex = searchPositionList.get(idx).getyIndex();
							int targetxIndex = searchPositionList.get(idx).getxIndex();
							if (virtual.masu[targetyIndex][targetxIndex] != MasuImpl.SPACE
									&& virtual.masu[targetyIndex][targetxIndex] != MasuImpl.BLACK) {
								isConflict = true;
								break;
							}
						}
						// 黒マスが置けない場所に黒マスを置こうとする候補は除外
						if (isConflict) {
							iterator.remove();
							continue;
						}
						for (int i = 0; i < searchPositionList.size(); i++) {
							int targetyIndex = searchPositionList.get(i).getyIndex();
							int targetxIndex = searchPositionList.get(i).getxIndex();
							if (oneCombi.contains(i)) {
								virtual.masu[targetyIndex][targetxIndex] = MasuImpl.BLACK;
							} else if (virtual.masu[targetyIndex][targetxIndex] == MasuImpl.SPACE) {
								virtual.masu[targetyIndex][targetxIndex] = MasuImpl.NOT_BLACK;
							}
						}

						for (int i = 0; i < searchPositionList.size(); i++) {
							int targetyIndex = searchPositionList.get(i).getyIndex();
							int targetxIndex = searchPositionList.get(i).getxIndex();
							// 仮置きしたうえでルール違反を調査
							if (!virtual.oneIsOk(targetyIndex, targetxIndex) ||
									!virtual.serveyOne(0, targetyIndex + 1, targetxIndex) ||
									!virtual.serveyOne(0, targetyIndex, targetxIndex + 1) ||
									!virtual.serveyOne(0, targetyIndex + 1, targetxIndex) ||
									!virtual.serveyOne(0, targetyIndex, targetxIndex - 1)) {
								isConflict = true;
								break;
							}
						}
						// ルール違反の候補は除外
						if (isConflict) {
							iterator.remove();
							continue;
						}
						// 再帰調査をする場合
						if (recursiveCnt != 0) {
							// 黒マスが置けなくなる組み合わせがあった場合候補から除外
							if (!virtual.serveyArrow(recursiveCnt - 1)) {
								iterator.remove();
							}
						}
					}
					if (combination.size() == 0) {
						return false;
					} else if (combination.size() == 1) {
						// 候補が1通りなので黒、黒以外がすべて確定
						for (int i = 0; i < searchPositionList.size(); i++) {
							Position pos = searchPositionList.get(i);
							if (combination.get(0).contains(i)) {
								masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.BLACK;
							} else if (masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.NOT_BLACK;
							}
						}
					} else {
						// 部分的に黒、黒以外が確定
						Set<Integer> candIdx = null;
						for (Set<Integer> idxSet : combination) {
							if (candIdx == null) {
								candIdx = new HashSet<>(idxSet);
							} else {
								candIdx.retainAll(idxSet);
							}
						}
						for (int i = 0; i < searchPositionList.size(); i++) {
							Position pos = searchPositionList.get(i);
							if (candIdx.contains(i)) {
								masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.BLACK;
							}
						}
						candIdx = null;
						for (Set<Integer> idxSet : combination) {
							if (candIdx == null) {
								candIdx = new HashSet<>(idxSet);
							} else {
								candIdx.addAll(idxSet);
							}
						}
						for (int i = 0; i < searchPositionList.size(); i++) {
							Position pos = searchPositionList.get(i);
							if (!candIdx.contains(i) && masu[pos.getyIndex()][pos.getxIndex()] == MasuImpl.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = MasuImpl.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * sizeからget個分取り出す組み合わせを列挙して返します。
		 */
		static List<Set<Integer>> getCombination(int size, int get) {
			List<Set<Integer>> result = new ArrayList<>();
			if (get != 0) {
				Field.addPod(size, get, new HashSet<>(), result, 0);
			} else {
				result.add(new HashSet<>());
			}
			return result;
		}

		/**
		 * 再帰処理用
		 */
		private static void addPod(int size, int get, Set<Integer> pod, List<Set<Integer>> finalPod, int startPos) {
			for (int i = startPos; i < size; i++) {
				if (pod.contains(i) || pod.contains(i - 1)) {
					continue;
				}
				pod = new HashSet<>(pod);
				pod.add(i);
				if (pod.size() < get) {
					addPod(size, get, pod, finalPod, i);
				} else {
					finalPod.add(pod);
				}
				pod = new HashSet<>(pod);
				pod.remove(i);
			}
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

	}

	/**
	 * コマンドライン向けインターフェースです
	 */
	public static void main(String[] args) throws InterruptedException {
		solve(Arrays.asList(args));
	}

	/**
	 * ヤジリンを解いて結果を標準出力に出します。
	 * argsには1行ごとに行の情報を渡します。
	 * 1マスは2文字で表されます。以下が例です。
	 * 00 ： 白マス
	 * u0 ： 上矢印の0
	 * r1 ： 右矢印の1
	 * d2 ： 下矢印の2
	 * l3 ： 左矢印の3
	 */
	public static void solve(List<String> args) {
		try {
			Field field = new Field(args);
			long startTime = System.nanoTime();
			System.out.println(field);
			int recursive = 0;
			String before;
			boolean invalid = false;
			while (!field.isSolved()) {
				System.out.println("recursive:" + recursive);
				before = field.toString();
				// 確定マスの調査
				if (!field.serveyAll(recursive + 2)) {
					invalid = true;
					break;
				}
				System.out.println(field);
				System.out.println();
				System.out.println("time:" + ((System.nanoTime() - startTime) / 1000000));
				// 矢印マスの調査
				if (!field.serveyArrow(recursive)) {
					invalid = true;
					break;
				}
				System.out.println(field);
				System.out.println();
				System.out.println("time:" + ((System.nanoTime() - startTime) / 1000000));
				// 進捗がなければ再帰回数を増やしてチャレンジ
				if (field.toString().equals(before)) {
					if (recursive >= 2) {
						// 再帰をある程度増やしてダメなら、ギブアップ…
						break;
					}
					recursive++;
				} else {
					recursive = 0;
				}
			}
			if (invalid) {
				System.out.println("なんかこの問題おかしくない？");
			} else if (field.isSolved()) {
				System.out.println("解けました！");
			} else {
				System.out.println("ギブアップ＞＜");
			}
		} catch (Exception e) {
			System.out.println("ありゃ？");
			e.printStackTrace();
		}
	}

}
