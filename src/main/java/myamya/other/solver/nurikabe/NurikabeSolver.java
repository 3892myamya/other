package myamya.other.solver.nurikabe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Solver;
import myamya.other.solver.nurikabe.NurikabeSolver.Masu.NotBlackMasu;
import myamya.other.solver.nurikabe.NurikabeSolver.Masu.NumberMasu;

public class NurikabeSolver implements Solver {
	static class Position {

		@Override
		public String toString() {
			return "[y=" + yIndex + ", x=" +
					xIndex + "]";
		}

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
	 * 方向を示す列挙型
	 */
	enum Direction {
		UP("u", 1, "↑"), RIGHT("r", 4, "→"), DOWN("d", 2, "↓"), LEFT("l", 3, "←");
		private final String str;
		private final int num;
		private final String directString;

		Direction(String str, int num, String directString) {
			this.str = str;
			this.num = num;
			this.directString = directString;
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

		public static Direction getByNum(int num) {
			for (Direction one : Direction.values()) {
				if (one.num == num) {
					return one;
				}
			}
			return null;
		}

		public String getDirectString() {
			return directString;
		}
	}

	public static class Masu {
		/** 未確定マス */
		static final Masu SPACE = new Masu() {
			@Override
			public String toString() {
				return "　";
			}
		};

		/** 黒マス */
		static final Masu BLACK = new Masu() {
			@Override
			public String toString() {
				return "■";
			}
		};

		/** 外壁 */
		static final Masu WALL = new Masu() {
			@Override
			public String toString() {
				return "※";
			}
		};
		/** 島未確定白マス */
		static final Masu NOT_BLACK_UNFIXED = new Masu() {
			@Override
			public String toString() {
				return "？";
			}
		};

		/** 島確定済マス */
		static class NotBlackMasu extends Masu {
			private NotBlackMasu() {
				//
			}

			@Override
			public String toString() {
				return "・";
			}
		}

		static final Masu NOT_BLACK = new NotBlackMasu();

		/** 数字マス */
		static class NumberMasu extends NotBlackMasu {

			private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
			private static final String FULL_NUMS = "０１２３４５６７８９";

			private final int capacity;
			private final Set<Position> followerPosSet;
			private final Set<Position> separatedFollower;

			NumberMasu(int capacity, Set<Position> followerPosSet) {
				this.capacity = capacity;
				this.followerPosSet = followerPosSet;
				this.separatedFollower = new HashSet<>();
			}

			public int getCapacity() {
				return capacity;
			}

			public Set<Position> getFollowerPosSet() {
				return followerPosSet;
			}

			public Set<Position> getSeparatedFollower() {
				return separatedFollower;
			}

			@Override
			public String toString() {
				if (capacity > 99) {
					return "99";
				}
				String capacityStr = String.valueOf(capacity);
				int index = HALF_NUMS.indexOf(capacityStr);
				if (index >= 0) {
					return FULL_NUMS.substring(index / 2,
							index / 2 + 1);
				} else {
					return capacityStr;
				}
			}

		}
	}

	public static class Field {
		static final String NUMBERS = "0123456789abcdef";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		private final Masu[][] masu;

		public Masu[][] getMasu() {
			return masu;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					String str;
					if (ch == '-') {
						str = "" + param.charAt(i + 1) + param.charAt(i + 2);
						i++;
						i++;
					} else if (ch == '+') {
						str = "" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3);
						i++;
						i++;
						i++;
					} else {
						str = String.valueOf(ch);
					}
					Position pos = new Position(index /
							getXLength(), index % getXLength());

					masu[pos.getyIndex()][pos.getxIndex()] = new NumberMasu(Integer.parseInt(str, 16),
							new HashSet<>(Arrays.asList(new Position[] { pos })));
					index++;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (other.masu[yIndex][xIndex] instanceof NumberMasu) {
						NumberMasu originNumberMasu = (NumberMasu) other.masu[yIndex][xIndex];
						masu[yIndex][xIndex] = new NumberMasu(originNumberMasu.getCapacity(),
								new HashSet<>(originNumberMasu.getFollowerPosSet()));
					} else {
						masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					}
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		// マスを埋める。
		public boolean solve(int baseLevel, int recursive) {
			toBlackOrWhite();
			boolean isSkip = false;
			Set<Position> allNotBlackCand = new HashSet<Position>();
			Map<Position, List<Position>> notFixedCandMap = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] instanceof NumberMasu) {
						// 残り何マス領土があるか
						List<Map<Masu, Set<Position>>> areaCandList = new ArrayList<>();
						NumberMasu targetMasu = ((NumberMasu) masu[yIndex][xIndex]);
						Position pos = new Position(yIndex, xIndex);
						if (targetMasu.getCapacity() > baseLevel + targetMasu.getFollowerPosSet().size()) {
							isSkip = true;
						}
						Set<Set<Position>> alreadyCandSet = new HashSet<>();
						// 埋め方候補を算出
						setAreaCandList(pos, areaCandList, alreadyCandSet, targetMasu.getFollowerPosSet(),
								targetMasu.getCapacity(),
								baseLevel + targetMasu.getFollowerPosSet().size() > targetMasu.getCapacity()
										? targetMasu.getCapacity()
										: baseLevel + targetMasu.getFollowerPosSet().size());
						// 候補のチェック
						checkCandList(areaCandList);
						// 再帰処理による候補の絞り込み
						if (areaCandList.size() >= 1 && recursive > 0) {
							for (Iterator<Map<Masu, Set<Position>>> iterator = areaCandList.iterator(); iterator
									.hasNext();) {
								Map<Masu, Set<Position>> candSet = iterator.next();
								Field virtual = new Field(this);
								for (Entry<Masu, Set<Position>> entry : candSet.entrySet()) {
									for (Position candPos : entry.getValue()) {
										if (entry.getKey() == Masu.BLACK) {
											virtual.masu[candPos.getyIndex()][candPos.getxIndex()] = Masu.BLACK;
										} else if (entry.getKey() == Masu.NOT_BLACK) {
											virtual.masu[candPos.getyIndex()][candPos.getxIndex()] = Masu.NOT_BLACK;
											((NumberMasu) virtual.masu[yIndex][xIndex]).getFollowerPosSet()
													.add(candPos);
										}
									}
								}
								if (!virtual.solve(baseLevel, recursive - 1)) {
									iterator.remove();
								}
							}
						}
						if (areaCandList.size() == 0) {
							// 候補が0通りなら失敗
							return false;
						} else {
							// それ以外なら決まる範囲で確定
							targetMasu.getFollowerPosSet().clear();
							targetMasu.getFollowerPosSet().add(pos);
							Set<Position> blackSet = new HashSet<>();
							boolean isBlackFirst = true;
							Set<Position> notBlackSet = new HashSet<>();
							boolean isNotBlackFirst = true;
							for (Map<Masu, Set<Position>> candSet : areaCandList) {
								for (Entry<Masu, Set<Position>> entry : candSet.entrySet()) {
									if (entry.getKey() == Masu.BLACK) {
										// 黒マス候補になった位置をすべて記録
										if (isBlackFirst) {
											blackSet.addAll(entry.getValue());
											isBlackFirst = false;
										} else {
											blackSet.retainAll(entry.getValue());
										}
									} else if (entry.getKey() == Masu.NOT_BLACK) {
										// 白マス候補になった位置をすべて記録
										allNotBlackCand.addAll(entry.getValue());
										if (isNotBlackFirst) {
											notBlackSet.addAll(entry.getValue());
											isNotBlackFirst = false;
										} else {
											notBlackSet.retainAll(entry.getValue());
										}
										// 属する島が決まらないマスの親候補にも追加
										for (Position onePos : entry.getValue()) {
											if (masu[onePos.getyIndex()][onePos
													.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
												List<Position> notFixedCandList = notFixedCandMap.get(onePos);
												if (notFixedCandList == null) {
													notFixedCandList = new ArrayList<>();
													notFixedCandMap.put(onePos, notFixedCandList);
												}
												if (!notFixedCandList.contains(pos)) {
													notFixedCandList.add(pos);
												}
											}
										}
									}
								}
							}
							for (Position candPos : blackSet) {
								masu[candPos.getyIndex()][candPos.getxIndex()] = Masu.BLACK;
							}
							for (Position candPos : notBlackSet) {
								masu[candPos.getyIndex()][candPos.getxIndex()] = Masu.NOT_BLACK;
								targetMasu.getFollowerPosSet().add(candPos);
							}
						}
					}
				}
			}
			if (!isSkip) {
				for (Entry<Position, List<Position>> entry : notFixedCandMap.entrySet()) {
					// 遠距離マスの候補が確定
					if (entry.getValue().size() == 1) {
						((NumberMasu) masu[entry.getValue().get(0).getyIndex()][entry.getValue().get(0).getxIndex()])
								.getSeparatedFollower()
								.add(entry.getKey());
					}
				}
			}
			if (!isSkip) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						// どの島から伸びても届かないマスを黒化
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							Position pos = new Position(yIndex, xIndex);
							if (!allNotBlackCand.contains(pos)) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		private void toBlackOrWhite() {
			// 黒を置くと2x2のかたまりができる場所を白化
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						Position pos = new Position(yIndex, xIndex);
						Field virtual = new Field(this);
						virtual.masu[yIndex][xIndex] = Masu.BLACK;
						if (!virtual.checkPond(pos)) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK_UNFIXED;
						}
					}
				}
			}
			// 白を置くと分断される場所を黒化
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						Field virtual = new Field(this);
						virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK_UNFIXED;
						if (!virtual.checkContinue()) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}
			// 独立マスを黒化
			toBlackStandAlone();
			// 塗ると2つの島がつながる場所を黒化
			Set<NumberMasu> numberMasuSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] instanceof NumberMasu) {
						numberMasuSet.add((NumberMasu) masu[yIndex][xIndex]);
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						Position pos = new Position(yIndex, xIndex);
						Set<Position> nearPosSet = new HashSet<>();
						if (pos.getyIndex() != 0) {
							nearPosSet.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
						}
						if (pos.getxIndex() != getXLength() - 1) {
							nearPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() + 1));
						}
						if (pos.getyIndex() != getYLength() - 1) {
							nearPosSet.add(new Position(pos.getyIndex() + 1, pos.getxIndex()));
						}
						if (pos.getxIndex() != 0) {
							nearPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
						}
						for (Iterator<Position> iterator = nearPosSet.iterator(); iterator.hasNext();) {
							Position nearPos = iterator.next();
							if (!(masu[nearPos.getyIndex()][nearPos.getxIndex()] instanceof NotBlackMasu)) {
								iterator.remove();
							}
						}
						for (NumberMasu numberMasu : numberMasuSet) {
							Set<Position> checkSet = new HashSet<>(nearPosSet);
							checkSet.removeAll(numberMasu.getFollowerPosSet());
							checkSet.removeAll(numberMasu.getSeparatedFollower());
							if (checkSet.size() != 0 && checkSet.size() != nearPosSet.size()) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
		}

		// posから見て黒が2x2の塊になる場合falseを返す。
		private boolean checkPond(Position pos) {
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0) {
				// 左上あり
				if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK &&
						masu[pos.getyIndex() - 1][pos.getxIndex() - 1] == Masu.BLACK &&
						masu[pos.getyIndex() - 1][pos.getxIndex()] == Masu.BLACK &&
						masu[pos.getyIndex()][pos.getxIndex() - 1] == Masu.BLACK) {
					return false;
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength() - 1) {
				// 右上あり
				if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK
						&& masu[pos.getyIndex() - 1][pos.getxIndex() + 1] == Masu.BLACK &&
						masu[pos.getyIndex() - 1][pos.getxIndex()] == Masu.BLACK &&
						masu[pos.getyIndex()][pos.getxIndex() + 1] == Masu.BLACK) {
					return false;
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != getXLength() - 1) {
				// 右下あり
				if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK
						&& masu[pos.getyIndex() + 1][pos.getxIndex() + 1] == Masu.BLACK &&
						masu[pos.getyIndex() + 1][pos.getxIndex()] == Masu.BLACK &&
						masu[pos.getyIndex()][pos.getxIndex() + 1] == Masu.BLACK) {
					return false;
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != 0) {
				// 左下あり
				if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK
						&& masu[pos.getyIndex() + 1][pos.getxIndex() - 1] == Masu.BLACK &&
						masu[pos.getyIndex() + 1][pos.getxIndex()] == Masu.BLACK &&
						masu[pos.getyIndex()][pos.getxIndex() - 1] == Masu.BLACK) {
					return false;
				}
			}
			return true;
		}

		// 独立した白マスをすべて塗る。1マス以上塗れたらtrueを返す。
		private boolean toBlackStandAlone() {
			// 白マスを黒マスに阻まれるまで広げ、数字マスがなければぬる。
			Set<Position> alreadyServey = new HashSet<>();
			Set<Position> toBlack = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!alreadyServey.contains(pos) && masu[pos.getyIndex()][pos.getxIndex()] != Masu.BLACK) {
						Set<Position> standAlonePosSet = new HashSet<>();
						standAlonePosSet.add(pos);
						boolean containNumberMasu = setStandAlonePosSet(pos, standAlonePosSet,
								masu[pos.getyIndex()][pos.getxIndex()] instanceof NumberMasu);
						alreadyServey.addAll(new HashSet<>(standAlonePosSet));
						if (!containNumberMasu) {
							toBlack.addAll(new HashSet<>(standAlonePosSet));
						}
					}
				}
			}
			for (Position black : toBlack) {
				masu[black.getyIndex()][black.getxIndex()] = Masu.BLACK;
			}
			return !toBlack.isEmpty();

		}

		// posを起点に上下左右に白マスとなりうる場所をつなげていく。
		private boolean setStandAlonePosSet(Position pos, Set<Position> standAlonePosSet, boolean containNumberMasu) {
			boolean result = containNumberMasu;
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!standAlonePosSet.contains(nextPos) &&
						(masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK)) {
					standAlonePosSet.add(nextPos);
					result = setStandAlonePosSet(nextPos, standAlonePosSet,
							result || masu[nextPos.getyIndex()][nextPos.getxIndex()] instanceof NumberMasu);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!standAlonePosSet.contains(nextPos) &&
						(masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK)) {
					standAlonePosSet.add(nextPos);
					result = setStandAlonePosSet(nextPos, standAlonePosSet,
							result || masu[nextPos.getyIndex()][nextPos.getxIndex()] instanceof NumberMasu);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!standAlonePosSet.contains(nextPos) &&
						(masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK)) {
					standAlonePosSet.add(nextPos);
					result = setStandAlonePosSet(nextPos, standAlonePosSet,
							result || masu[nextPos.getyIndex()][nextPos.getxIndex()] instanceof NumberMasu);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!standAlonePosSet.contains(nextPos) &&
						(masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK)) {
					standAlonePosSet.add(nextPos);
					result = setStandAlonePosSet(nextPos, standAlonePosSet,
							result || masu[nextPos.getyIndex()][nextPos.getxIndex()] instanceof NumberMasu);
				}
			}
			return result;
		}

		// 黒マスが分断される場合Falseを返す。
		private boolean checkContinue() {
			// 最初に見つけた黒マス候補マスから広がりを調査し、
			// それ以降の黒マスが広がりに含まれてなければNG
			Set<Position> continuePosSet = new HashSet<>();
			boolean isFirstFlg = true;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (isFirstFlg) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							continuePosSet.add(pos);
							setContinuePosSet(pos, continuePosSet);
							isFirstFlg = false;
						}
					} else {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(pos)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		// posを起点に上下左右に黒マスとなりうるマスをつなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) &&
						(masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK ||
								masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) &&
						(masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK ||
								masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) &&
						(masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK ||
								masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) &&
						(masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK ||
								masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
		}

		private void setAreaCandList(Position targetPos, List<Map<Masu, Set<Position>>> areaCandList,
				Set<Set<Position>> alreadyCandSet, Set<Position> candSet, int capacity, int level) {
			// 既に探索済みルートなら中断
			for (Set<Position> alreadyCand : alreadyCandSet) {
				alreadyCand = new HashSet<>(alreadyCand);
				if (alreadyCand.size() == candSet.size()) {
					alreadyCand.removeAll(candSet);
					if (alreadyCand.isEmpty()) {
						return;
					}
				}
			}
			// 探索済ルートの記憶
			alreadyCandSet.add(new HashSet<>(candSet));
			for (Position pos : candSet) {
				// 自身に隣接する未確定白マスがあれば最初に加算しておく
				if (pos.getyIndex() != 0) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!candSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
						candSet = new HashSet<>(candSet);
						candSet.add(nextPos);
						setAreaCandList(targetPos, areaCandList, alreadyCandSet, candSet, capacity, level);
						candSet.remove(nextPos);
					}
				}
				if (pos.getxIndex() != getXLength() - 1) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!candSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
						candSet = new HashSet<>(candSet);
						candSet.add(nextPos);
						setAreaCandList(targetPos, areaCandList, alreadyCandSet, candSet, capacity, level);
						candSet.remove(nextPos);
					}
				}
				if (pos.getyIndex() != getYLength() - 1) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!candSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
						candSet = new HashSet<>(candSet);
						candSet.add(nextPos);
						setAreaCandList(targetPos, areaCandList, alreadyCandSet, candSet, capacity, level);
						candSet.remove(nextPos);
					}
				}
				if (pos.getxIndex() != 0) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!candSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
						candSet = new HashSet<>(candSet);
						candSet.add(nextPos);
						setAreaCandList(targetPos, areaCandList, alreadyCandSet, candSet, capacity, level);
						candSet.remove(nextPos);
					}
				}
			}
			if (candSet.size() == capacity || candSet.size() == level) {
				// 順番が違うだけで実質中身が同じリストを排除
				for (Map<Masu, Set<Position>> checkPosMap : areaCandList) {
					Set<Position> checkPosSet = new HashSet<>(checkPosMap.get(Masu.NOT_BLACK));
					checkPosSet.removeAll(candSet);
					if (checkPosSet.isEmpty()) {
						return;
					}
				}
				Set<Position> notBlackSet = new HashSet<>(candSet);
				// 自分の領土となるべきマスが含まれていない候補を除外
				Set<Position> checkSet = new HashSet<>(
						(((NumberMasu) masu[targetPos.getyIndex()][targetPos.getxIndex()]).getSeparatedFollower()));
				checkSet.removeAll(notBlackSet);
				if (!checkSet.isEmpty()) {
					return;
				}
				// 数字マス自身は除外
				notBlackSet.remove(targetPos);
				Set<Position> blackSet = new HashSet<>();
				if (capacity == level) {
					for (Position candPos : candSet) {
						if (candPos.getyIndex() != 0) {
							Position blackPos = new Position(candPos.getyIndex() - 1, candPos.getxIndex());
							if (!candSet.contains(blackPos)) {
								// 黒マスになる予定のマスが既に白マスになっていたら候補除外
								if (masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
									return;
								}
								blackSet.add(blackPos);
							}
						}
						if (candPos.getxIndex() != getXLength() - 1) {
							Position blackPos = new Position(candPos.getyIndex(), candPos.getxIndex() + 1);
							if (!candSet.contains(blackPos)) {
								if (masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
									return;
								}
								blackSet.add(blackPos);
							}
						}
						if (candPos.getyIndex() != getYLength() - 1) {
							Position blackPos = new Position(candPos.getyIndex() + 1, candPos.getxIndex());
							if (!candSet.contains(blackPos)) {
								if (masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
									return;
								}
								blackSet.add(blackPos);
							}
						}
						if (candPos.getxIndex() != 0) {
							Position blackPos = new Position(candPos.getyIndex(), candPos.getxIndex() - 1);
							if (!candSet.contains(blackPos)) {
								if (masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.NOT_BLACK_UNFIXED) {
									return;
								}
								blackSet.add(blackPos);
							}
						}
					}
				}
				Map<Masu, Set<Position>> masuMap = new HashMap<>();
				masuMap.put(Masu.BLACK, blackSet);
				masuMap.put(Masu.NOT_BLACK, notBlackSet);
				areaCandList.add(masuMap);
			} else if (candSet.size() < capacity && candSet.size() < level) {
				for (Position pos : candSet) {
					// 前後左右に候補マスを伸ばす
					if (pos.getyIndex() != 0) {
						Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
						setAreaCand(targetPos, areaCandList, alreadyCandSet, candSet, capacity, nextPos, level);
					}
					if (pos.getxIndex() != getXLength() - 1) {
						Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
						setAreaCand(targetPos, areaCandList, alreadyCandSet, candSet, capacity, nextPos, level);
					}
					if (pos.getyIndex() != getYLength() - 1) {
						Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
						setAreaCand(targetPos, areaCandList, alreadyCandSet, candSet, capacity, nextPos, level);
					}
					if (pos.getxIndex() != 0) {
						Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
						setAreaCand(targetPos, areaCandList, alreadyCandSet, candSet, capacity, nextPos, level);
					}
				}
			}

		}

		private void setAreaCand(Position targetPos, List<Map<Masu, Set<Position>>> areaCandList,
				Set<Set<Position>> alreadyCandSet, Set<Position> candSet,
				int capacity, Position nextPos, int level) {
			if (!candSet.contains(nextPos) &&
					(masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
				// まだ候補に追加していないマスで空白マスのみ進行
				if (nextPos.getyIndex() != 0) {
					// 隣が別の島の領土の場合は候補外
					Position nearPos = new Position(nextPos.getyIndex() - 1, nextPos.getxIndex());
					if (!candSet.contains(nearPos)
							&& masu[nearPos.getyIndex()][nearPos.getxIndex()] instanceof NotBlackMasu) {
						return;
					}
				}
				if (nextPos.getxIndex() != getXLength() - 1) {
					Position nearPos = new Position(nextPos.getyIndex(), nextPos.getxIndex() + 1);
					if (!candSet.contains(nearPos)
							&& masu[nearPos.getyIndex()][nearPos.getxIndex()] instanceof NotBlackMasu) {
						return;
					}
				}
				if (nextPos.getyIndex() != getYLength() - 1) {
					Position nearPos = new Position(nextPos.getyIndex() + 1, nextPos.getxIndex());
					if (!candSet.contains(nearPos)
							&& masu[nearPos.getyIndex()][nearPos.getxIndex()] instanceof NotBlackMasu) {
						return;
					}
				}
				if (nextPos.getxIndex() != 0) {
					Position nearPos = new Position(nextPos.getyIndex(), nextPos.getxIndex() - 1);
					if (!candSet.contains(nearPos)
							&& masu[nearPos.getyIndex()][nearPos.getxIndex()] instanceof NotBlackMasu) {
						return;
					}
				}
				candSet = new HashSet<>(candSet);
				candSet.add(nextPos);
				setAreaCandList(targetPos, areaCandList, alreadyCandSet, candSet, capacity, level);
				candSet.remove(nextPos);
			}

		}

		// 置いた結果、ルール違反になるかどうかをチェック
		private void checkCandList(List<Map<Masu, Set<Position>>> areaCandList) {
			for (Iterator<Map<Masu, Set<Position>>> iterator = areaCandList.iterator(); iterator.hasNext();) {
				Map<Masu, Set<Position>> candSet = iterator.next();
				Field virtual = new Field(this);
				for (Entry<Masu, Set<Position>> entry : candSet.entrySet()) {
					for (Position candPos : entry.getValue()) {
						virtual.masu[candPos.getyIndex()][candPos.getxIndex()] = entry.getKey();
					}
				}
				if (!virtual.check()) {
					iterator.remove();
					continue;
				}
			}
		}

		// 今の状態がぬりかべのルール上問題ないかチェック
		private boolean check() {
			// 池ができてないかチェック
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!checkPond(pos)) {
						return false;
					}
				}
			}
			Field virtual = new Field(this);
			if (virtual.toBlackStandAlone()) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (!virtual.checkPond(new Position(yIndex, xIndex))) {
							return false;
						}
					}
				}
			}
			if (!checkContinue()) {
				return false;
			}
			return true;
		}

		// スペースがない状態を解けたとみなす
		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE || masu[yIndex][xIndex] == Masu.NOT_BLACK_UNFIXED) {
						return false;
					}
				}
			}
			return true;
		}

	}

	private final Field field;

	public NurikabeSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		new NurikabeSolver(height, width, param).solve();
	}

	@Override
	public String solve() {
		try {
			long start = System.nanoTime();
			int maxCapacity = 0;
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.masu[yIndex][xIndex] instanceof NumberMasu) {
						NumberMasu targetMasu = ((NumberMasu) field.masu[yIndex][xIndex]);
						if (maxCapacity < targetMasu.getCapacity()) {
							maxCapacity = targetMasu.getCapacity();
						}
					}
				}
			}
			System.out.println(field);
			int baseLevel = 1;
			boolean isTimeLimit = false;
			while (!field.isSolved()) {
				if ((System.nanoTime() - start) / 1000000 > 30000) {
					isTimeLimit = true;
					break;
				}
				String bfr = field.toString();
				System.out.println("baseLevel:" + baseLevel);
				if (!field.solve(baseLevel, baseLevel >= maxCapacity ? 1 : 0)) {
					System.out.println(field);
					System.out.println((System.nanoTime() - start) / 1000000 + "ms.");
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				if (bfr.equals(field.toString())) {
					baseLevel++;
				}
				System.out.println(field);
			}
			System.out.println(field);
			System.out.println((System.nanoTime() - start) / 1000000 + "ms.");
			if (!isTimeLimit) {
				return "解けました";
			} else {
				return "時間切れです。途中経過を返します。";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "解いている途中で予期せぬエラーが発生しました。";
		}
	}
}