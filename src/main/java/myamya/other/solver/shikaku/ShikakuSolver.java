package myamya.other.solver.shikaku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class ShikakuSolver implements Solver {

	public enum Wall {
		NOT_EXISTS("　"), EXISTS("□");

		String str;

		Wall(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	/**
	 * 四角の情報
	 */
	public static class Sikaku {
		private final Position leftUp;
		private final Position rightDown;

		public Position getLeftUp() {
			return leftUp;
		}

		public Position getRightDown() {
			return rightDown;
		}

		/**
		 * 自分とotherの領域がかぶっている場合trueを返す。
		 */
		public boolean isDuplicate(Sikaku other) {
			if (this.rightDown.getyIndex() < other.leftUp.getyIndex()) {
				return false;
			}
			if (this.rightDown.getxIndex() < other.leftUp.getxIndex()) {
				return false;
			}
			if (this.leftUp.getyIndex() > other.rightDown.getyIndex()) {
				return false;
			}
			if (this.leftUp.getxIndex() > other.rightDown.getxIndex()) {
				return false;
			}
			return true;
			// 自分の上が相手の下より上、かつ、自分の右が相手の左より右
			// または、自分の左上が、相手の左上と右下の間にあれば、重複している
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((leftUp == null) ? 0 : leftUp.hashCode());
			result = prime * result + ((rightDown == null) ? 0 : rightDown.hashCode());
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
			Sikaku other = (Sikaku) obj;
			if (leftUp == null) {
				if (other.leftUp != null)
					return false;
			} else if (!leftUp.equals(other.leftUp))
				return false;
			if (rightDown == null) {
				if (other.rightDown != null)
					return false;
			} else if (!rightDown.equals(other.rightDown))
				return false;
			return true;
		}

		public Sikaku(Position leftUp, Position rightDown) {
			super();
			this.leftUp = leftUp;
			this.rightDown = rightDown;
		}

		@Override
		public String toString() {
			return "[leftUp=" + leftUp + ", rightDown=" + rightDown + "]";
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 4;

		private final int height;
		private final int width;
		// 同一グループに属するマスの情報
		public final List<Room> rooms;
		// roomCandInfo 部屋ごとの切り分け方の候補
		public final LinkedHashMap<Room, Set<Sikaku>> roomCand;

		public int getYLength() {
			return height;
		}

		public int getXLength() {
			return width;
		}

		public List<Room> getRooms() {
			return rooms;
		}

		public LinkedHashMap<Room, Set<Sikaku>> getRoomCand() {
			return roomCand;
		}


		public Field(int height, int width, String param) {
			this.height = height;
			this.width = width;
			int index = 0;
			rooms = new ArrayList<>();
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					if (ch == '-') {
						capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
						i++;
						i++;
					} else if (ch == '+') {
						capacity = Integer.parseInt(
								"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
								16);
						i++;
						i++;
						i++;
					} else {
						capacity = Integer.parseInt(String.valueOf(ch), 16);
					}
					Position pos = new Position(index / getXLength(), index % getXLength());
					rooms.add(new Room(capacity, pos));
					index++;
				}
			}
			// 部屋の切り方の候補をあらかじめ決めておき、その候補を順次減らす方法を取る。
			// 部屋の切り方の候補はそう多くはならないので。
			roomCand = new LinkedHashMap<>();
			for (Room room : rooms) {
				Set<Integer> sikakuHeightSet = new HashSet<>();
				for (int i = 1; i <= room.getCapacity(); i++) {
					if (room.getCapacity() % i == 0) {
						sikakuHeightSet.add(i);
					}
				}
				Set<Sikaku> sikakuSet = new HashSet<>();
				for (int sikakuHeight : sikakuHeightSet) {
					int sikakuWidth = room.getCapacity() / sikakuHeight;
					int minY = room.getPivot().getyIndex() - sikakuHeight + 1 < 0 ? 0
							: room.getPivot().getyIndex() - sikakuHeight + 1;
					int maxY = room.getPivot().getyIndex() + sikakuHeight > height ? height - sikakuHeight
							: room.getPivot().getyIndex();
					int minX = room.getPivot().getxIndex() - sikakuWidth + 1 < 0 ? 0
							: room.getPivot().getxIndex() - sikakuWidth + 1;
					int maxX = room.getPivot().getxIndex() + sikakuWidth > width ? width - sikakuWidth
							: room.getPivot().getxIndex();
					for (int y = minY; y <= maxY; y++) {
						for (int x = minX; x <= maxX; x++) {
							Sikaku sikaku = new Sikaku(new Position(y, x),
									new Position(y + sikakuHeight - 1, x + sikakuWidth - 1));
							boolean addSikaku = true;
							// 他の部屋のpivotが含まれる候補をあらかじめ除外する。
							for (Room otherRoom : rooms) {
								if (room != otherRoom) {
									if (sikaku.getLeftUp().getxIndex() <= otherRoom.pivot.getxIndex()
											&& sikaku.getRightDown().getxIndex() >= otherRoom.pivot.getxIndex()
											&& sikaku.getLeftUp().getyIndex() <= otherRoom.pivot.getyIndex()
											&& sikaku.getRightDown().getyIndex() >= otherRoom.pivot.getyIndex()) {
										addSikaku = false;
										break;
									}
								}
							}
							if (addSikaku) {
								sikakuSet.add(sikaku);
							}
						}
					}
				}
				roomCand.put(room, sikakuSet);
			}
		}

		public Field(Field other) {
			height = other.height;
			width = other.width;
			rooms = other.rooms;
			roomCand = new LinkedHashMap<>();
			for (Entry<Room, Set<Sikaku>> entry : other.roomCand.entrySet()) {
				roomCand.put(entry.getKey(), new HashSet<>(entry.getValue()));
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			Wall[][] yokoWall = new Wall[height][width - 1];
			Wall[][] tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
				}
			}
			for (Set<Sikaku> sikakuSet : roomCand.values()) {
				if (sikakuSet.size() == 1) {
					Sikaku sikaku = (Sikaku) sikakuSet.toArray()[0];
					for (int yIndex = sikaku.getLeftUp().getyIndex(); yIndex <= sikaku.getRightDown()
							.getyIndex(); yIndex++) {
						if (sikaku.getLeftUp().getxIndex() > 0) {
							yokoWall[yIndex][sikaku.getLeftUp().getxIndex() - 1] = Wall.EXISTS;
						}
						if (sikaku.getRightDown().getxIndex() < width - 1) {
							yokoWall[yIndex][sikaku.getRightDown().getxIndex()] = Wall.EXISTS;
						}
					}
					for (int xIndex = sikaku.getLeftUp().getxIndex(); xIndex <= sikaku.getRightDown()
							.getxIndex(); xIndex++) {
						if (sikaku.getLeftUp().getyIndex() > 0) {
							tateWall[sikaku.getLeftUp().getyIndex() - 1][xIndex] = Wall.EXISTS;
						}
						if (sikaku.getRightDown().getyIndex() < height - 1) {
							tateWall[sikaku.getRightDown().getyIndex()][xIndex] = Wall.EXISTS;
						}
					}
				}
			}

			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean writeNumber = false;
					for (Room room : rooms) {
						if (room.getPivot().equals(new Position(yIndex, xIndex))) {
							if (room.getCapacity() > 99) {
								sb.append("99");
							}
							String capacityStr = String.valueOf(room.getCapacity());
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(capacityStr);
							}
							writeNumber = true;
							break;
						}
					}
					if (!writeNumber) {
						sb.append("　");
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex]);
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex]);
						if (xIndex != getXLength() - 1) {
							sb.append("□");
						}
					}
					sb.append("□");
					sb.append(System.lineSeparator());
				}
			}
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (Set<Sikaku> sikakuSet : roomCand.values()) {
				sb.append(sikakuSet.size() + ":");
			}
			return sb.toString();
		}

		public boolean isSolved() {
			for (Set<Sikaku> sikakuSet : roomCand.values()) {
				if (sikakuSet.size() != 1) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 各部屋の他の部屋とかぶる候補を消す。
		 * 候補の数が0になってしまったらfalseを返す。
		 */
		public boolean roomSolve() {
			for (Entry<Room, Set<Sikaku>> entry : roomCand.entrySet()) {
				for (Iterator<Sikaku> iterator = entry.getValue().iterator(); iterator.hasNext();) {
					Sikaku sikaku = iterator.next();
					boolean removeFlg = false;
					for (Entry<Room, Set<Sikaku>> anotherEntry : roomCand.entrySet()) {
						if (entry.getKey() != anotherEntry.getKey()) {
							boolean retainFlg = false;
							for (Sikaku anotherSikaku : anotherEntry.getValue()) {
								if (!sikaku.isDuplicate(anotherSikaku)) {
									// ある部屋について、かぶらない置き方が1通りでもあれば残す。
									retainFlg = true;
									break;
								}
							}
							if (!retainFlg) {
								// いずれかの部屋でかぶらない置き方が1通りも無ければ候補から除外。
								removeFlg = true;
								break;
							}
						}
					}
					if (removeFlg) {
						iterator.remove();
					}
				}
				if (entry.getValue().isEmpty()) {
					return false;
				}
			}
			return true;
		}


	}

	public static class Room {
		@Override
		public String toString() {
			return "Room [capacity=" + capacity + ", pivot=" + pivot + "]";
		}

		// 白マスが何マスあるか。
		private final int capacity;
		// 部屋に属するマスの集合
		private final Position pivot;

		public Room(int capacity, Position pivot) {
			this.capacity = capacity;
			this.pivot = pivot;
		}

		public int getCapacity() {
			return capacity;
		}

		public Position getPivot() {
			return pivot;
		}

	}

	// easy
	// http://pzv.jp/p.html?shikaku/10/10/g8h8g6r4jar4i5v8i6r9j6r6g4h-10g
	// http://pzv.jp/p.html?shikaku/10/10/haj2i6l8j4h2l4h4i7n35n9i2h3l9h2j4l8i4j4h
	// http://pzv.jp/p.html?shikaku/45/30/g-10h9j34i4i5j3jah6h2k5zi6i5zo4k-12s-14h6h9j62icn3l6j3zi8g8hah3l5l4lfh3hci6kcl2l7ick433q5s3i4i2h6zh2t3l-12k2l5r2g3gah2h-1a59t6lckci6i2i5zg3l3jbn6o54394i2g6g2p2m6j7j2j2g4zg4i92h3iam24326i6kc5528n2l22h4h2g9zh3m2zg6i43nfgan44n6g8n63i3zgcm6zh3g2h-18h83l2n63234k8ia5224m3i6h39i3zg5g5j4j4j4m6pcg4g4i45435o8n3j4l8zg2i3ici7k9l3t926h4h2g6g4r6l9kfl3t-12zh9h6i5i4s2qc64k3i2l4l6k4iah4hel3l2l3h4h-1cg2zi4j3lfnci4-14j3hfh2s3k5zo9i5zi2k3h3h8j5jfi4i84j-10h4g

	// hard
	// http://pzv.jp/p.html?shikaku/15/15/k2i5nfm3o9g6m8q8l2ickaick5i8h3o9y4o9h6i5k4i4kcicl4q8m5g4o4mcn5iak
	// http://pzv.jp/p.html?shikaku/17/17/zm-1cw-12m-10ock4h1g4y8g1scn6lfp-18s-14pcl8n2s1gcy9g1hck-1eo-10m5wczm

	// veryhard
	// http://pzv.jp/p.html?shikaku/15/15/3l6l5i4g4i7g5k3j6j3i5q5k6g6g2g4k7q2k6g2g2g6j6g6o6g6j3g2g2g3k7q7k3g6g4g6k4q5i4j6j6k6g3i2g6i6l6l5
	// http://pzv.jp/p.html?shikaku/12/12/j6g3j6g9v8h9k9i8v3k7j44j5k6v4i9k9hav9g8j2g6j
	private final Field field;

	public ShikakuSolver(int height, int width, String param) {
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
		System.out.println(new ShikakuSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		int difficulty = -1;
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!solveAndCheck(field)
					|| (!befStr.equals(field.getStateDump()) && !solveAndCheck(field))) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			if (!field.isSolved() && difficulty == -1) {
				difficulty = 0;
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				difficulty = difficulty <= recursiveCnt ? recursiveCnt + 1 : difficulty;
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) +
				"ms.");
		System.out.println("難易度:" + difficulty + 1);
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByVal(difficulty + 1 > 3 ? 3 : difficulty + 1).toString();
	}

	/**
	 * 各種チェックを1セット実行
	 * @param recursive
	 */
	private static boolean solveAndCheck(Field field) {
		if (!field.roomSolve()) {
			return false;
		}
		return true;
	}

	/**
	 * 仮置きして調べる
	 */
	private static boolean candSolve(Field field, int recursive) {
		for (Entry<Room, Set<Sikaku>> entry : field.roomCand.entrySet()) {
			if (entry.getValue().size() > 1) {
				for (Iterator<Sikaku> iterator = entry.getValue().iterator(); iterator.hasNext();) {
					Sikaku sikaku = iterator.next();
					if (!oneCandSolve(field, entry.getKey(), sikaku, recursive)) {
						iterator.remove();
					}
				}
				if (entry.getValue().size() == 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 * @param field
	 */
	private static boolean oneCandSolve(Field field, Room room, Sikaku candSikaku, int recursive) {
		Field virtual = new Field(field);
		Set<Sikaku> sikakuSet = new HashSet<>();
		sikakuSet.add(candSikaku);
		virtual.roomCand.put(room, sikakuSet);
		String befStr = virtual.getStateDump();
		boolean allowCandSikaku = solveAndCheck(virtual)
				&& (befStr.equals(virtual.getStateDump()) || solveAndCheck(virtual));
		if (allowCandSikaku && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowCandSikaku = false;
			}
		}
		return allowCandSikaku;
	}
}
